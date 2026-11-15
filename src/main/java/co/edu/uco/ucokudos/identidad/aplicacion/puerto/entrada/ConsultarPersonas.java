package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Puerto de entrada del módulo de identidad para que los demás módulos consulten personas. Nunca
 * entrega el correo ni el celular de nadie (D-16).
 */
public interface ConsultarPersonas {

	/**
	 * Busca una persona que tenga la cuenta activa.
	 *
	 * @param id identificador de la persona
	 * @return la persona, o vacío si no existe o su cuenta no está activa
	 */
	Optional<PersonaActiva> buscarActiva(UUID id);

	/**
	 * Las personas con la cuenta activa, ordenadas por nombre: a quiénes se les puede dar un Kudo.
	 */
	List<PersonaActiva> listarActivas();

	/**
	 * Los nombres de varias personas de una sola vez, activas o no, para mostrar lo que ya publicaron.
	 *
	 * @return un nombre por cada identificador que exista; los que no existan no aparecen
	 */
	Map<UUID, String> nombresDe(Set<UUID> ids);

}
