package co.edu.uco.ucokudos.kudos.aplicacion;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeCategorias;
import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;

/**
 * Doble del repositorio de categorías.
 */
final class CategoriasEnMemoria implements RepositorioDeCategorias {

	private final Map<IdCategoria, Categoria> categorias = new LinkedHashMap<>();

	CategoriasEnMemoria con(Categoria categoria) {
		categorias.put(categoria.id(), categoria);
		return this;
	}

	@Override
	public Optional<Categoria> buscarPorId(IdCategoria id) {
		return Optional.ofNullable(categorias.get(id));
	}

	@Override
	public List<Categoria> listarActivas() {
		return categorias.values()
			.stream()
			.filter(Categoria::activa)
			.sorted(Comparator.comparing(Categoria::nombre))
			.toList();
	}

}
