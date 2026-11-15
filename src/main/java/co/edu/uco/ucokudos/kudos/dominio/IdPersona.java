package co.edu.uco.ucokudos.kudos.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una persona, visto desde el módulo de Kudos: el autor o el destinatario. La
 * persona en sí vive en el módulo de identidad.
 */
public record IdPersona(UUID valor) {

	public IdPersona {
		Objects.requireNonNull(valor, "valor");
	}

}
