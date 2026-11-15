package co.edu.uco.ucokudos.kudos.infraestructura.persistencia;

import java.util.Optional;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeCategorias;
import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;

/**
 * Las categorías en PostgreSQL, con Spring Data JDBC.
 */
@Repository
class RepositorioDeCategoriasJdbc implements RepositorioDeCategorias {

	private final JdbcAggregateOperations jdbc;

	RepositorioDeCategoriasJdbc(JdbcAggregateOperations jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<Categoria> buscarPorId(IdCategoria id) {
		return Optional.ofNullable(jdbc.findById(id.valor(), FilaDeCategoria.class)).map(FilaDeCategoria::aDominio);
	}

}
