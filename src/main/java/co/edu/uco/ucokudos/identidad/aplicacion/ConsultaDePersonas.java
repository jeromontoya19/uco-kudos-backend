package co.edu.uco.ucokudos.identidad.aplicacion;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida.RepositorioDePersonas;
import co.edu.uco.ucokudos.identidad.dominio.Persona;

/**
 * Responde a los otros módulos qué personas tienen la cuenta activa.
 */
public final class ConsultaDePersonas implements ConsultarPersonas {

	private final RepositorioDePersonas personas;

	public ConsultaDePersonas(RepositorioDePersonas personas) {
		this.personas = Objects.requireNonNull(personas, "personas");
	}

	@Override
	public Optional<PersonaActiva> buscarActiva(UUID id) {
		return personas.buscarPorId(id)
			.filter(Persona::activa)
			.map(persona -> new PersonaActiva(persona.id(), persona.nombre()));
	}

}
