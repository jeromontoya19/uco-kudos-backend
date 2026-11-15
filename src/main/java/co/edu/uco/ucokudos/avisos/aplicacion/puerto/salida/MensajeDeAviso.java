package co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida;

import java.util.Objects;
import java.util.UUID;

/**
 * Todo lo que un canal necesita para avisarle al destinatario de un Kudo.
 *
 * @param aviso        identificador del aviso, para los registros
 * @param kudo         identificador del Kudo
 * @param destinatario a quién avisar y cómo
 * @param autor        nombre de quien dio el Kudo
 * @param categoria    nombre de la categoría
 */
public record MensajeDeAviso(UUID aviso, UUID kudo, Destinatario destinatario, String autor, String categoria) {

	public MensajeDeAviso {
		Objects.requireNonNull(aviso, "aviso");
		Objects.requireNonNull(kudo, "kudo");
		Objects.requireNonNull(destinatario, "destinatario");
		Objects.requireNonNull(autor, "autor");
		Objects.requireNonNull(categoria, "categoria");
	}

	/**
	 * @param id      identificador de la persona
	 * @param nombre  nombre para el saludo
	 * @param correo  correo institucional
	 * @param celular número celular con indicativo
	 */
	public record Destinatario(UUID id, String nombre, String correo, String celular) {
	}

}
