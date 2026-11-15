package co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * Un Kudo recién publicado cuyo destinatario hay que avisar.
 *
 * @param kudo         identificador del Kudo
 * @param autor        quién lo publicó
 * @param destinatario a quién avisar
 * @param categoria    nombre de la categoría del Kudo
 */
public record KudoPorAvisar(UUID kudo, UUID autor, UUID destinatario, String categoria) {

	public KudoPorAvisar {
		Objects.requireNonNull(kudo, "kudo");
		Objects.requireNonNull(autor, "autor");
		Objects.requireNonNull(destinatario, "destinatario");
		Objects.requireNonNull(categoria, "categoria");
	}

}
