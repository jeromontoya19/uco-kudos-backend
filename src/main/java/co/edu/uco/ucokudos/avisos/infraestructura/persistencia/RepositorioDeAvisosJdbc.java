package co.edu.uco.ucokudos.avisos.infraestructura.persistencia;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;

/**
 * Los avisos en PostgreSQL. La búsqueda de los listos usa el índice parcial de pendientes de la
 * migración V4.
 */
@Repository
class RepositorioDeAvisosJdbc implements RepositorioDeAvisos {

	private static final String LISTOS = """
			SELECT id FROM avisos.aviso
			WHERE estado = 'PENDIENTE' AND proximo_intento_en <= :ahora
			ORDER BY proximo_intento_en
			LIMIT :limite
			""";

	private final JdbcAggregateOperations agregados;

	private final JdbcClient jdbc;

	RepositorioDeAvisosJdbc(JdbcAggregateOperations agregados, JdbcClient jdbc) {
		this.agregados = agregados;
		this.jdbc = jdbc;
	}

	@Override
	public void guardarTodos(List<Aviso> avisos) {
		avisos.stream().map(FilaDeAviso::desde).forEach(agregados::insert);
	}

	@Override
	public List<UUID> listosParaEnviar(Instant ahora, int limite) {
		return jdbc.sql(LISTOS)
			.param("ahora", OffsetDateTime.ofInstant(ahora, ZoneOffset.UTC))
			.param("limite", limite)
			.query(UUID.class)
			.list();
	}

	@Override
	public Optional<Aviso> buscarPorId(UUID id) {
		return Optional.ofNullable(agregados.findById(id, FilaDeAviso.class)).map(FilaDeAviso::aDominio);
	}

	@Override
	public void actualizar(Aviso aviso) {
		agregados.update(FilaDeAviso.desde(aviso));
	}

}
