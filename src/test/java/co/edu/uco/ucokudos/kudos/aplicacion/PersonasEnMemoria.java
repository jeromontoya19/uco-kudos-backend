package co.edu.uco.ucokudos.kudos.aplicacion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;

/**
 * Doble del puerto de entrada del módulo de identidad, para probar los casos de uso de Kudos sin él.
 */
final class PersonasEnMemoria implements ConsultarPersonas {

	private final Map<UUID, String> nombres = new HashMap<>();

	private final Set<UUID> inactivas = new HashSet<>();

	PersonasEnMemoria con(UUID id, String nombre) {
		nombres.put(id, nombre);
		return this;
	}

	void desactivar(UUID id) {
		inactivas.add(id);
	}

	@Override
	public Optional<PersonaActiva> buscarActiva(UUID id) {
		return estaActiva(id) ? Optional.of(new PersonaActiva(id, nombres.get(id))) : Optional.empty();
	}

	@Override
	public List<PersonaActiva> listarActivas() {
		return nombres.keySet().stream().filter(this::estaActiva).map(id -> new PersonaActiva(id, nombres.get(id))).toList();
	}

	@Override
	public Map<UUID, String> nombresDe(Set<UUID> ids) {
		return ids.stream().filter(nombres::containsKey).collect(Collectors.toMap(id -> id, nombres::get));
	}

	private boolean estaActiva(UUID id) {
		return nombres.containsKey(id) && !inactivas.contains(id);
	}

}
