package co.edu.uco.ucokudos.avisos.infraestructura.canales;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * La configuración de WhatsApp Cloud API de Meta (ADR-020, SPK-01). El token viene de un secreto, nunca
 * del repositorio.
 *
 * @param token       el token de acceso de Meta
 * @param idTelefono  el identificador del número que envía (Phone number ID)
 * @param plantilla   la plantilla aprobada, con {{1}} destinatario, {{2}} autor y {{3}} categoría
 * @param idioma      el idioma de la plantilla, por ejemplo {@code es_CO}
 * @param versionApi  la versión de la Graph API, por ejemplo {@code v25.0}
 */
@ConfigurationProperties("ucokudos.avisos.whatsapp")
public record PropiedadesDeWhatsApp(String token, String idTelefono, String plantilla, String idioma,
		String versionApi) {

	boolean estaConfigurado() {
		return PropiedadesDeCorreo.tieneTexto(token) && PropiedadesDeCorreo.tieneTexto(idTelefono);
	}

}
