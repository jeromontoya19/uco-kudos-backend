package co.edu.uco.ucokudos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;

/**
 * El paso 1 y el paso 5 del flujo obligatorio contra un PostgreSQL real: el Kudo queda guardado, con
 * sus tres avisos pendientes (ADR-003) y su registro de auditoría, que la base no deja cambiar (ADR-008).
 */
@SpringBootTest
@Import(BaseDeDatosDePrueba.class)
class PublicarKudoIT {

	/** La docente y el estudiante de prueba de la migración V2. */
	static final UUID DOCENTE = UUID.fromString("00000000-0000-0000-0000-000000000006");

	static final UUID ESTUDIANTE = UUID.fromString("00000000-0000-0000-0000-000000000007");

	@Autowired
	private PublicarKudo publicarKudo;

	@Autowired
	private JdbcClient jdbc;

	@Test
	void publicar_guarda_el_kudo_sus_tres_avisos_pendientes_y_su_auditoria() {
		KudoPublicado publicado = publicar("Gracias por ayudar a tus compañeros");

		assertThat(jdbc.sql("SELECT contenido FROM kudos.kudo WHERE id = ?").param(publicado.id()).query(String.class).single())
			.isEqualTo("Gracias por ayudar a tus compañeros");
		assertThat(jdbc.sql("SELECT canal || ':' || estado FROM avisos.aviso WHERE kudo = ? ORDER BY canal")
			.param(publicado.id())
			.query(String.class)
			.list()).containsExactly("CORREO:PENDIENTE", "PUSH:PENDIENTE", "WHATSAPP:PENDIENTE");
		assertThat(jdbc.sql("SELECT accion || ':' || resultado FROM auditoria.registro WHERE id_recurso = ? AND actor = ?")
			.param(publicado.id())
			.param(DOCENTE)
			.query(String.class)
			.list()).containsExactly("KUDO_PUBLICADO:EXITO");
	}

	@Test
	void la_base_no_deja_cambiar_ni_borrar_la_auditoria() {
		KudoPublicado publicado = publicar("Gracias por tu paciencia");

		assertThatThrownBy(() -> jdbc.sql("UPDATE auditoria.registro SET resultado = 'FALLO' WHERE id_recurso = ?")
			.param(publicado.id())
			.update()).isInstanceOf(DataAccessException.class).hasMessageContaining("solo inserción");
		assertThatThrownBy(() -> jdbc.sql("DELETE FROM auditoria.registro WHERE id_recurso = ?")
			.param(publicado.id())
			.update()).isInstanceOf(DataAccessException.class).hasMessageContaining("solo inserción");
	}

	private KudoPublicado publicar(String contenido) {
		UUID empatia = jdbc.sql("SELECT id FROM kudos.categoria WHERE nombre = 'Empatía'").query(UUID.class).single();
		return publicarKudo.publicar(new SolicitudDePublicacion(DOCENTE, ESTUDIANTE, empatia, contenido));
	}

}
