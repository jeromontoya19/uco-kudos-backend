package co.edu.uco.ucokudos.avisos.aplicacion;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;

/**
 * Doble del outbox, en memoria.
 */
final class AvisosEnMemoria implements RepositorioDeAvisos {

	private final Map<UUID, Aviso> avisos = new LinkedHashMap<>();

	@Override
	public void guardarTodos(List<Aviso> nuevos) {
		nuevos.forEach(aviso -> avisos.put(aviso.id(), aviso));
	}

	@Override
	public List<UUID> listosParaEnviar(Instant ahora, int limite) {
		return avisos.values()
			.stream()
			.filter(aviso -> aviso.estaListo(ahora))
			.sorted(Comparator.comparing(Aviso::proximoIntentoEn))
			.limit(limite)
			.map(Aviso::id)
			.toList();
	}

	@Override
	public Optional<Aviso> buscarPorId(UUID id) {
		return Optional.ofNullable(avisos.get(id));
	}

	@Override
	public void actualizar(Aviso aviso) {
		avisos.put(aviso.id(), aviso);
	}

	List<Aviso> todos() {
		return List.copyOf(avisos.values());
	}

}
