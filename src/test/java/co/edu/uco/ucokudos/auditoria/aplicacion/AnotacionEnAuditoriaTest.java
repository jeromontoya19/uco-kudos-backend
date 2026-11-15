package co.edu.uco.ucokudos.auditoria.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.HechoAuditable;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.Resultado;
import co.edu.uco.ucokudos.auditoria.dominio.RegistroDeAuditoria;

class AnotacionEnAuditoriaTest {

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	@Test
	void registra_el_hecho_con_su_fecha_y_su_resultado() {
		List<RegistroDeAuditoria> bitacora = new ArrayList<>();
		AnotacionEnAuditoria anotacion = new AnotacionEnAuditoria(bitacora::add, Clock.fixed(AHORA, ZoneOffset.UTC));
		UUID actor = UUID.randomUUID();
		UUID kudo = UUID.randomUUID();

		anotacion.registrar(new HechoAuditable("KUDO_PUBLICADO", actor, "KUDO", kudo, Resultado.EXITO));

		assertThat(bitacora).singleElement().satisfies(registro -> {
			assertThat(registro.ocurridoEn()).isEqualTo(AHORA);
			assertThat(registro.accion()).isEqualTo("KUDO_PUBLICADO");
			assertThat(registro.actor()).isEqualTo(actor);
			assertThat(registro.tipoRecurso()).isEqualTo("KUDO");
			assertThat(registro.idRecurso()).isEqualTo(kudo);
			assertThat(registro.resultado()).isEqualTo("EXITO");
		});
	}

	@Test
	void un_registro_necesita_saber_que_paso() {
		UUID id = UUID.randomUUID();

		assertThatThrownBy(() -> new RegistroDeAuditoria(id, AHORA, " ", id, "KUDO", id, "EXITO"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("accion");
	}

}
