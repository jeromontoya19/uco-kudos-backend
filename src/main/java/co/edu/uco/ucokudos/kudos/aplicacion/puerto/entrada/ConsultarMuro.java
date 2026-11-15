package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

import java.util.List;

/**
 * Puerto de entrada: el muro de Kudos, por páginas y del más reciente al más antiguo (RF-004).
 */
public interface ConsultarMuro {

	/** Cuántos Kudos trae una página si no se pide otra cantidad. */
	int TAMANO_POR_DEFECTO = 20;

	/** El tope de Kudos por página, para que nadie pida el muro entero de una vez. */
	int TAMANO_MAXIMO = 50;

	/**
	 * @param pagina número de página, desde 0; uno negativo se toma como 0
	 * @param tamano cuántos Kudos por página; se ajusta entre 1 y {@link #TAMANO_MAXIMO}
	 */
	List<KudoDelMuro> pagina(int pagina, int tamano);

}
