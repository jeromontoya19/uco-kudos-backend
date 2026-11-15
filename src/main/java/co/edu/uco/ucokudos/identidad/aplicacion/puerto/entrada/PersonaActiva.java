package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * Lo que otro módulo puede saber de una persona con la cuenta activa: su identificador y su nombre.
 * Ni el correo ni el celular salen de aquí (D-16).
 *
 * @param id     identificador de la persona
 * @param nombre nombre para mostrar
 */
public record PersonaActiva(UUID id, String nombre) {

	public PersonaActiva {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nombre, "nombre");
	}

}
