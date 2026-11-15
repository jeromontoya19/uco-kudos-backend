package co.edu.uco.ucokudos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;

/**
 * La publicación es una sola transacción (ADR-003): si el último paso falla, tampoco quedan el Kudo ni
 * sus avisos. Así nunca hay un Kudo sin avisos ni avisos de un Kudo que no existe.
 */
@SpringBootTest
@Import(BaseDeDatosDePrueba.class)
class PublicacionAtomicaIT {

	@MockitoBean
	private RegistrarEnAuditoria auditoria;

	@Autowired
	private PublicarKudo publicarKudo;

	@Autowired
	private JdbcClient jdbc;

	@Test
	void si_falla_la_auditoria_no_queda_ni_el_kudo_ni_sus_avisos() {
		doThrow(new IllegalStateException("La auditoría no respondió")).when(auditoria).registrar(any());
		UUID empatia = jdbc.sql("SELECT id FROM kudos.categoria WHERE nombre = 'Empatía'").query(UUID.class).single();
		SolicitudDePublicacion solicitud = new SolicitudDePublicacion(PublicarKudoIT.DOCENTE, PublicarKudoIT.ESTUDIANTE,
				empatia, "Este Kudo no debe quedar a medias");

		assertThatThrownBy(() -> publicarKudo.publicar(solicitud)).isInstanceOf(IllegalStateException.class);

		assertThat(jdbc.sql("SELECT count(*) FROM kudos.kudo").query(Long.class).single()).isZero();
		assertThat(jdbc.sql("SELECT count(*) FROM avisos.aviso").query(Long.class).single()).isZero();
	}

}
