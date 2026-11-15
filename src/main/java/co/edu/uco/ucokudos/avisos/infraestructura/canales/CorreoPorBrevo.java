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
 * El correo, por la API HTTP de Brevo (ADR-019). Solo texto plano: así ningún nombre puede inyectar
 * HTML en el mensaje.
 */
public final class CorreoPorBrevo implements CanalDeAviso {

	/** La raíz de la API de Brevo. */
	public static final String API = "https://api.brevo.com";

	private static final Logger REGISTRO = LoggerFactory.getLogger(CorreoPorBrevo.class);

	private final RestClient brevo;

	private final PropiedadesDeCorreo propiedades;

	public CorreoPorBrevo(RestClient brevo, PropiedadesDeCorreo propiedades) {
		this.brevo = brevo;
		this.propiedades = propiedades;
	}

	@Override
	public Canal canal() {
		return Canal.CORREO;
	}

	@Override
	public ResultadoDelEnvio enviar(MensajeDeAviso mensaje) {
		if (!propiedades.estaConfigurado()) {
			REGISTRO.warn("El correo no está configurado: el aviso {} queda para reintento", mensaje.aviso());
			return ResultadoDelEnvio.FALLIDO;
		}
		try {
			brevo.post()
				.uri("/v3/smtp/email")
				.header("api-key", propiedades.llave())
				.contentType(MediaType.APPLICATION_JSON)
				.body(cuerpo(mensaje))
				.retrieve()
				.toBodilessEntity();
			return ResultadoDelEnvio.ENTREGADO;
		}
		catch (RestClientException error) {
			REGISTRO.warn("Brevo no aceptó el correo del aviso {}: {}", mensaje.aviso(), error.getMessage());
			return ResultadoDelEnvio.FALLIDO;
		}
	}

	private Map<String, Object> cuerpo(MensajeDeAviso mensaje) {
		MensajeDeAviso.Destinatario destinatario = mensaje.destinatario();
		return Map.of("sender", Map.of("name", propiedades.nombre(), "email", propiedades.remitente()), "to",
				List.of(Map.of("email", destinatario.correo(), "name", destinatario.nombre())), "subject",
				mensaje.autor() + " te dio un Kudo en UCO Kudos", "textContent", texto(mensaje));
	}

	private String texto(MensajeDeAviso mensaje) {
		String saludo = """
				Hola, %s:

				%s te reconoció con un Kudo en la categoría «%s».
				""".formatted(mensaje.destinatario().nombre(), mensaje.autor(), mensaje.categoria());
		return PropiedadesDeCorreo.tieneTexto(propiedades.urlDelFront())
				? saludo + "\nMíralo en UCO Kudos: " + propiedades.urlDelFront() + "\n" : saludo;
	}

}
