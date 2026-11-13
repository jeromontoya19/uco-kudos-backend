package co.edu.uco.ucokudos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * Comprueba los dos endpoints que usan la plataforma y el monitor: la salud (ESC-07) y la versión
 * desplegada, que confirma la vuelta atrás (ESC-15).
 */
@SpringBootTest(properties = "APP_VERSION=prueba")
@AutoConfigureMockMvc
class SaludIT {

	@Autowired
	private MockMvcTester mvc;

	@Test
	void la_salud_responde_UP() {
		assertThat(mvc.get().uri("/actuator/health"))
			.hasStatusOk()
			.bodyJson().extractingPath("$.status").isEqualTo("UP");
	}

	@Test
	void las_sondas_de_vida_y_de_disponibilidad_responden_UP() {
		assertThat(mvc.get().uri("/actuator/health/liveness"))
			.hasStatusOk()
			.bodyJson().extractingPath("$.status").isEqualTo("UP");
		assertThat(mvc.get().uri("/actuator/health/readiness"))
			.hasStatusOk()
			.bodyJson().extractingPath("$.status").isEqualTo("UP");
	}

	@Test
	void la_informacion_muestra_la_version_desplegada() {
		assertThat(mvc.get().uri("/actuator/info"))
			.hasStatusOk()
			.bodyJson().extractingPath("$.app.version").isEqualTo("prueba");
	}

	@Test
	void la_salud_no_expone_detalles_internos() {
		assertThat(mvc.get().uri("/actuator/health"))
			.hasStatusOk()
			.bodyJson().doesNotHavePath("$.components");
	}

}
