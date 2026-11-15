package co.edu.uco.ucokudos.kudos.infraestructura.persistencia;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeKudos;
import co.edu.uco.ucokudos.kudos.dominio.Kudo;

/**
 * Los Kudos en PostgreSQL, con Spring Data JDBC. Inserta de forma explícita porque el identificador
 * lo pone el dominio, no la base.
 */
@Repository
class RepositorioDeKudosJdbc implements RepositorioDeKudos {

	private final JdbcAggregateOperations jdbc;

	RepositorioDeKudosJdbc(JdbcAggregateOperations jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void guardar(Kudo kudo) {
		jdbc.insert(FilaDeKudo.desde(kudo));
	}

}
