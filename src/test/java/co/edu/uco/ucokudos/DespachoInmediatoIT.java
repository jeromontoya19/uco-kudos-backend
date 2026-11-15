package co.edu.uco.ucokudos;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;

/**
 * Los avisos salen apenas se publica el Kudo, sin esperar la vuelta periódica del despachador (ESC-14).
 * Aquí la vuelta es de una hora: si los avisos se intentan en segundos, fue el despacho inmediato.
 */
@SpringBootTest(properties = { "ucokudos.avisos.despachador.activo=true",
		"ucokudos.avisos.despachador.intervalo=PT1H" })
@Import(BaseDeDatosDePrueba.class)
class DespachoInmediatoIT {

	private static final Duration ESPERA_MAXIMA = Duration.ofSeconds(5);

	@Autowired
	private PublicarKudo publicarKudo;

	@Autowired
	private JdbcClient jdbc;

	@Test
	void los_avisos_se_intentan_apenas_se_publica_el_kudo() {
		UUID empatia = jdbc.sql("SELECT id FROM kudos.categoria WHERE nombre = 'Empatía'").query(UUID.class).single();
		KudoPublicado publicado = publicarKudo.publicar(new SolicitudDePublicacion(PublicarKudoIT.DOCENTE,
				PublicarKudoIT.ESTUDIANTE, empatia, "Gracias por la tutoría del sábado"));

		await().atMost(ESPERA_MAXIMA).until(() -> avisosSinIntentar(publicado.id()) == 0);
	}

	private long avisosSinIntentar(UUID kudo) {
		return jdbc.sql("SELECT count(*) FROM avisos.aviso WHERE kudo = ? AND intentos = 0")
			.param(kudo)
			.query(Long.class)
			.single();
	}

}
