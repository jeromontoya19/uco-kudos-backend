package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para el módulo de avisos: el correo y el celular de una persona, para avisarle de
 * un Kudo. Está separado de {@link ConsultarPersonas} para que ningún otro módulo vea esos datos.
 */
public interface ConsultarContactos {

	/**
	 * @return el contacto de la persona, o vacío si no existe
	 */
	Optional<Contacto> contactoDe(UUID persona);

}
