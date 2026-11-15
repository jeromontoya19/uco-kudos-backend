package co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import co.edu.uco.ucokudos.identidad.dominio.Persona;

/**
 * Puerto de salida: dónde viven las personas.
 */
public interface RepositorioDePersonas {

	Optional<Persona> buscarPorId(UUID id);

	/**
	 * @param sujeto el «sub» de la cuenta en el IdP
	 */
	Optional<Persona> buscarPorSujeto(String sujeto);

	/** Las personas activas, ordenadas por nombre. */
	List<Persona> listarActivas();

	/** Las personas con esos identificadores, activas o no; las que no existan no aparecen. */
	List<Persona> buscarPorIds(Set<UUID> ids);

}
