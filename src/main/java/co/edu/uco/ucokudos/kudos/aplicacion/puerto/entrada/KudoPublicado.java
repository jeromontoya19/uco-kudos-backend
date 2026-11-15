package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * El resultado de publicar: el Kudo ya quedó guardado, y sus avisos, pendientes.
 *
 * @param id          identificador del Kudo
 * @param publicadoEn cuándo se publicó
 */
public record KudoPublicado(UUID id, Instant publicadoEn) {

	public KudoPublicado {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(publicadoEn, "publicadoEn");
	}

}
