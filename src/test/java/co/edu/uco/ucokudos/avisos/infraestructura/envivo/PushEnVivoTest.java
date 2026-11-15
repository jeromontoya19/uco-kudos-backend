package co.edu.uco.ucokudos.avisos.infraestructura.envivo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.MensajeDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.ResultadoDelEnvio;
import co.edu.uco.ucokudos.avisos.dominio.Canal;

class PushEnVivoTest {

	private static final UUID ESTUDIANTE = UUID.randomUUID();

	private static final MensajeDeAviso MENSAJE = new MensajeDeAviso(UUID.randomUUID(), UUID.randomUUID(),
			new MensajeDeAviso.Destinatario(ESTUDIANTE, "Estudiante", "estudiante@example.com", "+570000000007"),
			"Docente", "Empatía");

	private final ConexionesEnVivo conexiones = new ConexionesEnVivo();

	private final PushEnVivo push = new PushEnVivo(conexiones);

	@Test
	void sin_la_aplicacion_abierta_el_push_se_omite() {
		assertThat(push.canal()).isEqualTo(Canal.PUSH);
		assertThat(push.enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.OMITIDO);
	}

	@Test
	void con_la_aplicacion_abierta_el_push_llega() {
		conexiones.conectar(ESTUDIANTE);

		assertThat(push.enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.ENTREGADO);
	}

	@Test
	void una_conexion_cerrada_deja_de_contar() {
		conexiones.conectar(ESTUDIANTE).complete();
		conexiones.mantenerVivas();

		assertThat(push.enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.OMITIDO);
	}

}
