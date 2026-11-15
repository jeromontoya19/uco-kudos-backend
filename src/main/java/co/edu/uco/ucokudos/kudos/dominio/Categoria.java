package co.edu.uco.ucokudos.kudos.dominio;

import java.util.Objects;

/**
 * Una categoría de Kudos. Solo las activas sirven para publicar.
 *
 * @param id     identificador de la categoría
 * @param nombre nombre para mostrar, por ejemplo «Empatía»
 * @param activa si se puede usar para publicar
 */
public record Categoria(IdCategoria id, String nombre, boolean activa) {

	public Categoria {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nombre, "nombre");
	}

}
