package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * Lo que llega para publicar un Kudo. El autor sale de la sesión, nunca del cuerpo de la petición.
 *
 * @param autor        quién publica
 * @param destinatario a quién reconoce
 * @param categoria    en qué categoría
 * @param contenido    el texto; si viene vacío, el dominio lo rechaza
 */
public record SolicitudDePublicacion(UUID autor, UUID destinatario, UUID categoria, String contenido) {

	public SolicitudDePublicacion {
		Objects.requireNonNull(autor, "autor");
		Objects.requireNonNull(destinatario, "destinatario");
		Objects.requireNonNull(categoria, "categoria");
	}

}
