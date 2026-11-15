package co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucokudos.avisos.dominio.Aviso;

/**
 * Puerto de salida: dónde quedan los avisos (el outbox).
 */
public interface RepositorioDeAvisos {

	void guardarTodos(List<Aviso> avisos);

	/**
	 * Los avisos pendientes cuyo próximo intento ya llegó, del más atrasado al más reciente.
	 *
	 * @param limite cuántos como máximo, para que una tanda no se alargue
	 */
	List<UUID> listosParaEnviar(Instant ahora, int limite);

	Optional<Aviso> buscarPorId(UUID id);

	void actualizar(Aviso aviso);

}
