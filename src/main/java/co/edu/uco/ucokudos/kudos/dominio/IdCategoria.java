package co.edu.uco.ucokudos.kudos.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador de una categoría de Kudos.
 */
public record IdCategoria(UUID valor) {

	public IdCategoria {
		Objects.requireNonNull(valor, "valor");
	}

}
