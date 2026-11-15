package co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida;

import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucokudos.identidad.dominio.Persona;

/**
 * Puerto de salida: dónde viven las personas.
 */
public interface RepositorioDePersonas {

	Optional<Persona> buscarPorId(UUID id);

}
