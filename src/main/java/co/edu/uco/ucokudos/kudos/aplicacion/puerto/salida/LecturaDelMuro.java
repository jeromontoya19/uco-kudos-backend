package co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: el muro tal como está guardado, del Kudo más reciente al más antiguo.
 */
public interface LecturaDelMuro {

	/**
	 * @param desde    cuántos Kudos saltar desde el más reciente
	 * @param cantidad cuántos traer como máximo
	 */
	List<KudoLeido> leer(int desde, int cantidad);

	/**
	 * Un Kudo del muro con los identificadores de sus personas; los nombres los pone el caso de uso.
	 *
	 * @param id           identificador del Kudo
	 * @param autor        quién lo dio
	 * @param destinatario quién lo recibió
	 * @param categoria    nombre de la categoría
	 * @param contenido    el texto
	 * @param publicadoEn  cuándo se publicó
	 */
	record KudoLeido(UUID id, UUID autor, UUID destinatario, String categoria, String contenido,
			Instant publicadoEn) {
	}

}
