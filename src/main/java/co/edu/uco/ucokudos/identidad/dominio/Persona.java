package co.edu.uco.ucokudos.identidad.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * Una persona de la comunidad UCO, con su único rol activo.
 *
 * @param id      identificador de la persona
 * @param nombre  nombre para mostrar
 * @param correo  correo institucional, por donde recibe los avisos
 * @param celular número celular, por donde recibe los avisos de WhatsApp
 * @param rol     su rol activo
 * @param activa  si la cuenta está activa
 */
public record Persona(UUID id, String nombre, String correo, String celular, Rol rol, boolean activa) {

	public Persona {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(nombre, "nombre");
		Objects.requireNonNull(correo, "correo");
		Objects.requireNonNull(celular, "celular");
		Objects.requireNonNull(rol, "rol");
	}

}
