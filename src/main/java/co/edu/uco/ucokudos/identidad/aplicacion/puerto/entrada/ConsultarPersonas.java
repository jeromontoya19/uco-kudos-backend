package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada del módulo de identidad para que los demás módulos consulten personas.
 */
public interface ConsultarPersonas {

	/**
	 * Busca una persona que tenga la cuenta activa.
	 *
	 * @param id identificador de la persona
	 * @return la persona, o vacío si no existe o su cuenta no está activa
	 */
	Optional<PersonaActiva> buscarActiva(UUID id);

}
