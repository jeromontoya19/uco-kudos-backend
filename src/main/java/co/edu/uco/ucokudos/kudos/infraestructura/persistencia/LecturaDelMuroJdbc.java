package co.edu.uco.ucokudos.kudos.infraestructura.persistencia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.LecturaDelMuro;

/**
 * El muro con una sola consulta SQL: el modelo de lectura no necesita armar agregados (ADR-004 le pone
 * la caché encima). Usa el índice por fecha de publicación de la migración V3.
 */
@Repository
class LecturaDelMuroJdbc implements LecturaDelMuro {

	private static final String CONSULTA = """
			SELECT k.id, k.autor, k.destinatario, c.nombre AS categoria, k.contenido, k.publicado_en
			FROM kudos.kudo k
			JOIN kudos.categoria c ON c.id = k.categoria
			ORDER BY k.publicado_en DESC, k.id
			LIMIT :cantidad OFFSET :desde
			""";

	private final JdbcClient jdbc;

	LecturaDelMuroJdbc(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public List<KudoLeido> leer(int desde, int cantidad) {
		return jdbc.sql(CONSULTA)
			.param("cantidad", cantidad)
			.param("desde", desde)
			.query((fila, numero) -> kudoDe(fila))
			.list();
	}

	private static KudoLeido kudoDe(ResultSet fila) throws SQLException {
		return new KudoLeido(fila.getObject("id", UUID.class), fila.getObject("autor", UUID.class),
				fila.getObject("destinatario", UUID.class), fila.getString("categoria"), fila.getString("contenido"),
				fila.getObject("publicado_en", OffsetDateTime.class).toInstant());
	}

}
