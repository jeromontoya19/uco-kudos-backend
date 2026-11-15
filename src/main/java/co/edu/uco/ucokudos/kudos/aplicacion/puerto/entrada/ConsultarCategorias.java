package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

import java.util.List;

/**
 * Puerto de entrada: las categorías que se pueden elegir al publicar.
 */
public interface ConsultarCategorias {

	/** Las categorías activas, ordenadas por nombre. */
	List<CategoriaVisible> activas();

}
