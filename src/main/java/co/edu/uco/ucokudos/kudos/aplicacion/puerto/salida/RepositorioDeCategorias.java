package co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida;

import java.util.Optional;

import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;

/**
 * Puerto de salida: dónde viven las categorías.
 */
public interface RepositorioDeCategorias {

	Optional<Categoria> buscarPorId(IdCategoria id);

}
