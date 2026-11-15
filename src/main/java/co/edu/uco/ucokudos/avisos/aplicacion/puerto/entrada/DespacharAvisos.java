package co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada del despachador (ADR-003): envía los avisos pendientes por su canal. Lo llama una
 * tarea programada dentro de la propia API.
 */
public interface DespacharAvisos {

	/** Los avisos que ya se pueden intentar, en una tanda acotada. */
	List<UUID> listosParaEnviar();

	/**
	 * Intenta un aviso: lo envía por su canal, guarda su nuevo estado y registra el intento en la
	 * auditoría. Si el aviso ya no está listo, no hace nada.
	 */
	void despachar(UUID aviso);

}
