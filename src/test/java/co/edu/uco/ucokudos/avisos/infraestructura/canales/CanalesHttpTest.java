package co.edu.uco.ucokudos.avisos.infraestructura.canales;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.MensajeDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.ResultadoDelEnvio;
import co.edu.uco.ucokudos.avisos.dominio.Canal;

/**
 * Los adaptadores de Brevo y de Meta contra un servidor HTTP simulado: qué piden y cómo traducen cada
 * respuesta. Nunca llaman a los proveedores reales.
 */
class CanalesHttpTest {

	private static final MensajeDeAviso MENSAJE = new MensajeDeAviso(UUID.randomUUID(), UUID.randomUUID(),
			new MensajeDeAviso.Destinatario(UUID.randomUUID(), "Estudiante", "estudiante@example.com", "+57 300 000 0007"),
			"Docente", "Empatía");

	private static final PropiedadesDeCorreo CORREO = new PropiedadesDeCorreo("llave-de-prueba", "kudos@example.com",
			"UCO Kudos", "https://front.example.test");

	private static final PropiedadesDeWhatsApp WHATSAPP = new PropiedadesDeWhatsApp("token-de-prueba", "123456",
			"kudo_recibido", "es_CO", "v25.0");

	@Test
	void el_correo_llama_a_brevo_con_su_llave_y_un_texto_para_el_destinatario() {
		RestClient.Builder cliente = RestClient.builder().baseUrl(CorreoPorBrevo.API);
		MockRestServiceServer brevo = MockRestServiceServer.bindTo(cliente).build();
		brevo.expect(requestTo(CorreoPorBrevo.API + "/v3/smtp/email"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header("api-key", "llave-de-prueba"))
			.andExpect(jsonPath("$.to[0].email").value("estudiante@example.com"))
			.andExpect(jsonPath("$.sender.email").value("kudos@example.com"))
			.andExpect(jsonPath("$.subject").value("Docente te dio un Kudo en UCO Kudos"))
			.andExpect(jsonPath("$.textContent").value(containsString("«Empatía»")))
			.andExpect(jsonPath("$.textContent").value(containsString("https://front.example.test")))
			.andRespond(withStatus(HttpStatus.CREATED));
		CorreoPorBrevo correo = new CorreoPorBrevo(cliente.build(), CORREO);

		assertThat(correo.canal()).isEqualTo(Canal.CORREO);
		assertThat(correo.enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.ENTREGADO);
		brevo.verify();
	}

	@Test
	void si_brevo_falla_el_correo_queda_fallido_sin_lanzar_errores() {
		RestClient.Builder cliente = RestClient.builder().baseUrl(CorreoPorBrevo.API);
		MockRestServiceServer.bindTo(cliente).build()
			.expect(requestTo(CorreoPorBrevo.API + "/v3/smtp/email"))
			.andRespond(withServerError());

		assertThat(new CorreoPorBrevo(cliente.build(), CORREO).enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.FALLIDO);
	}

	@Test
	void sin_llave_el_correo_no_llama_a_nadie_y_queda_fallido() {
		RestClient.Builder cliente = RestClient.builder().baseUrl(CorreoPorBrevo.API);
		MockRestServiceServer brevo = MockRestServiceServer.bindTo(cliente).build();
		PropiedadesDeCorreo sinLlave = new PropiedadesDeCorreo("", "kudos@example.com", "UCO Kudos", "");

		assertThat(new CorreoPorBrevo(cliente.build(), sinLlave).enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.FALLIDO);
		brevo.verify();
	}

	@Test
	void whatsapp_usa_la_plantilla_aprobada_con_sus_tres_variables() {
		RestClient.Builder cliente = RestClient.builder().baseUrl(WhatsAppPorMeta.API);
		MockRestServiceServer meta = MockRestServiceServer.bindTo(cliente).build();
		meta.expect(requestTo(WhatsAppPorMeta.API + "/v25.0/123456/messages"))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header("Authorization", "Bearer token-de-prueba"))
			.andExpect(jsonPath("$.to").value("573000000007"))
			.andExpect(jsonPath("$.template.name").value("kudo_recibido"))
			.andExpect(jsonPath("$.template.language.code").value("es_CO"))
			.andExpect(jsonPath("$.template.components[0].parameters[0].text").value("Estudiante"))
			.andExpect(jsonPath("$.template.components[0].parameters[1].text").value("Docente"))
			.andExpect(jsonPath("$.template.components[0].parameters[2].text").value("Empatía"))
			.andRespond(withSuccess());
		WhatsAppPorMeta whatsApp = new WhatsAppPorMeta(cliente.build(), WHATSAPP);

		assertThat(whatsApp.canal()).isEqualTo(Canal.WHATSAPP);
		assertThat(whatsApp.enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.ENTREGADO);
		meta.verify();
	}

	@Test
	void si_meta_rechaza_el_mensaje_o_falta_el_token_queda_fallido() {
		RestClient.Builder cliente = RestClient.builder().baseUrl(WhatsAppPorMeta.API);
		MockRestServiceServer.bindTo(cliente).build()
			.expect(requestTo(WhatsAppPorMeta.API + "/v25.0/123456/messages"))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST));
		PropiedadesDeWhatsApp sinToken = new PropiedadesDeWhatsApp("", "123456", "kudo_recibido", "es_CO", "v25.0");

		assertThat(new WhatsAppPorMeta(cliente.build(), WHATSAPP).enviar(MENSAJE)).isEqualTo(ResultadoDelEnvio.FALLIDO);
		assertThat(new WhatsAppPorMeta(RestClient.create(), sinToken).enviar(MENSAJE))
			.isEqualTo(ResultadoDelEnvio.FALLIDO);
	}

}
