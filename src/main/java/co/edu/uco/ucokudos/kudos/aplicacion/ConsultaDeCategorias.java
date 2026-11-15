package co.edu.uco.ucokudos.kudos.aplicacion;

import java.util.List;
import java.util.Objects;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.CategoriaVisible;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarCategorias;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeCategorias;

/**
 * Lista las categorías activas para el formulario de publicar.
 */
public final class ConsultaDeCategorias implements ConsultarCategorias {

	private final RepositorioDeCategorias categorias;

	public ConsultaDeCategorias(RepositorioDeCategorias categorias) {
		this.categorias = Objects.requireNonNull(categorias, "categorias");
	}

	@Override
	public List<CategoriaVisible> activas() {
		return categorias.listarActivas()
			.stream()
			.map(categoria -> new CategoriaVisible(categoria.id().valor(), categoria.nombre()))
			.toList();
	}

}
