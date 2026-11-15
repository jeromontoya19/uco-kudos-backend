package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * Una categoría que se puede elegir al publicar.
 *
 * @param id     identificador de la categoría
 * @param nombre nombre para mostrar
 */
public record CategoriaVisible(UUID id, String nombre) {

	public CategoriaVisible {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nombre, "nombre");
	}

}
