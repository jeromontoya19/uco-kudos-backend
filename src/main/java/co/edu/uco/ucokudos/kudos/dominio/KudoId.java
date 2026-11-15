package co.edu.uco.ucokudos.kudos.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de un Kudo.
 */
public record KudoId(UUID valor) {

	public KudoId {
		Objects.requireNonNull(valor, "valor");
	}

	public static KudoId nuevo() {
		return new KudoId(UUID.randomUUID());
	}

}
