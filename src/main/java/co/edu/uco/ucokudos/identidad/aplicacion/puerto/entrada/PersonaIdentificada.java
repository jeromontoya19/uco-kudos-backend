package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * La persona que corresponde a una sesión, con su rol activo.
 *
 * @param id     identificador de la persona
 * @param nombre nombre para mostrar
 * @param rol    su rol activo, por ejemplo {@code DOCENTE}
 */
public record PersonaIdentificada(UUID id, String nombre, String rol) {

	public PersonaIdentificada {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nombre, "nombre");
		Objects.requireNonNull(rol, "rol");
	}

}
