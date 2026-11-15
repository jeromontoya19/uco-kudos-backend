package co.edu.uco.ucokudos.identidad.infraestructura.persistencia;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida.RepositorioDePersonas;
import co.edu.uco.ucokudos.identidad.dominio.Persona;

/**
 * Las personas en PostgreSQL, con Spring Data JDBC.
 */
@Repository
class RepositorioDePersonasJdbc implements RepositorioDePersonas {

	private final JdbcAggregateOperations jdbc;

	RepositorioDePersonasJdbc(JdbcAggregateOperations jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<Persona> buscarPorId(UUID id) {
		return Optional.ofNullable(jdbc.findById(id, FilaDePersona.class)).map(FilaDePersona::aDominio);
	}

}
