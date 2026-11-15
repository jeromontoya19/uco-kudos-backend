package co.edu.uco.ucokudos.avisos.infraestructura.persistencia;

import java.util.List;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;

/**
 * Los avisos pendientes en PostgreSQL, con Spring Data JDBC.
 */
@Repository
class RepositorioDeAvisosJdbc implements RepositorioDeAvisos {

	private final JdbcAggregateOperations jdbc;

	RepositorioDeAvisosJdbc(JdbcAggregateOperations jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void guardarTodos(List<Aviso> avisos) {
		avisos.stream().map(FilaDeAviso::desde).forEach(jdbc::insert);
	}

}
