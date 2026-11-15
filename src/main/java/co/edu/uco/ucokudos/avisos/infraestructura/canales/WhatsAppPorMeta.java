package co.edu.uco.ucokudos.avisos.infraestructura.canales;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.CanalDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.MensajeDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.ResultadoDelEnvio;
import co.edu.uco.ucokudos.avisos.dominio.Canal;

/**
 * WhatsApp por la Cloud API de Meta, con la plantilla aprobada {@code kudo_recibido} (ADR-020, SPK-01).
 * Las plantillas son la única forma de escribirle primero a alguien; sus tres variables son el
 * destinatario, el autor y la categoría.
 */
public final class WhatsAppPorMeta implements CanalDeAviso {

	/** La raíz de la Graph API de Meta. */
	public static final String API = "https://graph.facebook.com";

	private static final Logger REGISTRO = LoggerFactory.getLogger(WhatsAppPorMeta.class);

	private final RestClient meta;

	private final PropiedadesDeWhatsApp propiedades;

	public WhatsAppPorMeta(RestClient meta, PropiedadesDeWhatsApp propiedades) {
		this.meta = meta;
		this.propiedades = propiedades;
	}

	@Override
	public Canal canal() {
		return Canal.WHATSAPP;
	}

	@Override
	public ResultadoDelEnvio enviar(MensajeDeAviso mensaje) {
		if (!propiedades.estaConfigurado()) {
			REGISTRO.warn("WhatsApp no está configurado: el aviso {} queda para reintento", mensaje.aviso());
			return ResultadoDelEnvio.FALLIDO;
		}
		try {
			meta.post()
				.uri("/{version}/{telefono}/messages", propiedades.versionApi(), propiedades.idTelefono())
				.headers(encabezados -> encabezados.setBearerAuth(propiedades.token()))
				.contentType(MediaType.APPLICATION_JSON)
				.body(cuerpo(mensaje))
				.retrieve()
				.toBodilessEntity();
			return ResultadoDelEnvio.ENTREGADO;
		}
		catch (RestClientException error) {
			REGISTRO.warn("Meta no aceptó el WhatsApp del aviso {}: {}", mensaje.aviso(), error.getMessage());
			return ResultadoDelEnvio.FALLIDO;
		}
	}

	private Map<String, Object> cuerpo(MensajeDeAviso mensaje) {
		List<Map<String, String>> variables = List.of(texto(mensaje.destinatario().nombre()), texto(mensaje.autor()),
				texto(mensaje.categoria()));
		return Map.of("messaging_product", "whatsapp", "to", soloDigitos(mensaje.destinatario().celular()), "type",
				"template", "template",
				Map.of("name", propiedades.plantilla(), "language", Map.of("code", propiedades.idioma()), "components",
						List.of(Map.of("type", "body", "parameters", variables))));
	}

	private static Map<String, String> texto(String valor) {
		return Map.of("type", "text", "text", valor);
	}

	/** Meta espera el número con indicativo y sin «+» ni espacios. */
	static String soloDigitos(String celular) {
		return celular.replaceAll("\\D", "");
	}

}
