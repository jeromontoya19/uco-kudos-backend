package co.edu.uco.ucokudos.identidad.aplicacion;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.IdentificarPersona;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaIdentificada;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida.RepositorioDePersonas;
import co.edu.uco.ucokudos.identidad.dominio.Persona;

/**
 * Responde quién es quién: a los otros módulos, qué personas están activas y cómo se llaman; a la
 * seguridad, qué persona corresponde a una cuenta del IdP. Nunca entrega correos ni celulares (D-16).
 */
public final class ConsultaDePersonas implements ConsultarPersonas, IdentificarPersona {

	private final RepositorioDePersonas personas;

	public ConsultaDePersonas(RepositorioDePersonas personas) {
		this.personas = Objects.requireNonNull(personas, "personas");
	}

	@Override
	public Optional<PersonaActiva> buscarActiva(UUID id) {
		return personas.buscarPorId(id).filter(Persona::activa).map(ConsultaDePersonas::comoActiva);
	}

	@Override
	public List<PersonaActiva> listarActivas() {
		return personas.listarActivas().stream().map(ConsultaDePersonas::comoActiva).toList();
	}

	@Override
	public Map<UUID, String> nombresDe(Set<UUID> ids) {
		if (ids.isEmpty()) {
			return Map.of();
		}
		return personas.buscarPorIds(ids).stream().collect(Collectors.toUnmodifiableMap(Persona::id, Persona::nombre));
	}

	@Override
	public Optional<PersonaIdentificada> porSujeto(String sujeto) {
		return personas.buscarPorSujeto(sujeto)
			.filter(Persona::activa)
			.map(persona -> new PersonaIdentificada(persona.id(), persona.nombre(), persona.rol().name()));
	}

	private static PersonaActiva comoActiva(Persona persona) {
		return new PersonaActiva(persona.id(), persona.nombre());
	}

}
