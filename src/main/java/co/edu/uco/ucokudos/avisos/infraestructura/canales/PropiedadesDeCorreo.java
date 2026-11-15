package co.edu.uco.ucokudos.avisos.infraestructura.canales;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * La configuración del correo por Brevo (ADR-019). La llave viene de un secreto, nunca del repositorio.
 *
 * @param llave       la API key de Brevo
 * @param remitente   el correo remitente, verificado en Brevo
 * @param nombre      el nombre del remitente que ve el destinatario
 * @param urlDelFront la URL pública del front, para el enlace del mensaje; puede ir vacía
 */
@ConfigurationProperties("ucokudos.avisos.correo")
public record PropiedadesDeCorreo(String llave, String remitente, String nombre, String urlDelFront) {

	boolean estaConfigurado() {
		return tieneTexto(llave) && tieneTexto(remitente);
	}

	static boolean tieneTexto(String valor) {
		return valor != null && !valor.isBlank();
	}

}
