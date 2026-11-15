package co.edu.uco.ucokudos.identidad.infraestructura.persistencia;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Sort;
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

	@Override
	public Optional<Persona> buscarPorSujeto(String sujeto) {
		return jdbc.findOne(query(where("sujetoIdp").is(sujeto)), FilaDePersona.class).map(FilaDePersona::aDominio);
	}

	@Override
	public List<Persona> listarActivas() {
		return aDominio(jdbc.findAll(query(where("activa").isTrue()).sort(Sort.by("nombre")), FilaDePersona.class));
	}

	@Override
	public List<Persona> buscarPorIds(Set<UUID> ids) {
		return aDominio(jdbc.findAllById(ids, FilaDePersona.class));
	}

	private static List<Persona> aDominio(Iterable<FilaDePersona> filas) {
		return StreamSupport.stream(filas.spliterator(), false).map(FilaDePersona::aDominio).toList();
	}

}
