package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * Cómo avisarle a una persona. Solo lo usa el módulo de avisos para enviar; ninguna respuesta de la API
 * lo expone (D-16).
 *
 * @param id      identificador de la persona
 * @param nombre  nombre para el saludo
 * @param correo  correo institucional
 * @param celular número celular, con indicativo (por ejemplo {@code +57…})
 */
public record Contacto(UUID id, String nombre, String correo, String celular) {

	public Contacto {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nombre, "nombre");
		Objects.requireNonNull(correo, "correo");
		Objects.requireNonNull(celular, "celular");
	}

}
