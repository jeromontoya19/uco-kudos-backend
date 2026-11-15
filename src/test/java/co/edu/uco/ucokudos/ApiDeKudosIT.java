package co.edu.uco.ucokudos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * La API de punta a punta, con la seguridad real: solo el decodificador de tokens es un doble, así que
 * cada petición pasa por el convertidor que lleva el «sub» del token a la persona y su rol (ADR-007).
 */
@SpringBootTest(properties = "CORS_ORIGENES=" + ApiDeKudosIT.FRONT)
@AutoConfigureMockMvc
@Import(BaseDeDatosDePrueba.class)
class ApiDeKudosIT {

	static final String FRONT = "https://front.example.test";

	private static final String TOKEN_DOCENTE = "token-docente";

	private static final String TOKEN_SIN_PERSONA = "token-sin-persona";

	/** Qué «sub» trae cada token de prueba. */
	private static final Map<String, String> SUJETOS = Map.of(TOKEN_DOCENTE, "auth0|docente-de-prueba",
			TOKEN_SIN_PERSONA, "auth0|cuenta-sin-persona");

	@MockitoBean
	private JwtDecoder decodificador;

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private JdbcClient jdbc;

	@BeforeEach
	void vincularLaCuentaDelDocente() {
		jdbc.sql("UPDATE identidad.persona SET sujeto_idp = ? WHERE id = ?")
			.param(SUJETOS.get(TOKEN_DOCENTE))
			.param(PublicarKudoIT.DOCENTE)
			.update();
		when(decodificador.decode(anyString())).thenAnswer(llamada -> token(llamada.getArgument(0)));
	}

	@Test
	void sin_token_la_api_responde_401() {
		assertThat(mvc.get().uri("/api/categorias")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void una_cuenta_del_idp_sin_persona_activa_responde_403() {
		assertThat(mvc.get().uri("/api/categorias").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_SIN_PERSONA)))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void publicar_responde_201_y_el_kudo_queda_primero_en_el_muro() {
		assertThat(mvc.post().uri("/api/kudos").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(cuerpo(PublicarKudoIT.ESTUDIANTE, "Gracias por explicarle a todo el grupo")))
			.hasStatus(HttpStatus.CREATED)
			.headers().containsHeader(HttpHeaders.LOCATION);
		String publicado = jdbc.sql("SELECT id FROM kudos.kudo ORDER BY publicado_en DESC LIMIT 1")
			.query(String.class)
			.single();

		assertThat(mvc.get().uri("/api/kudos").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE)))
			.hasStatusOk()
			.bodyJson()
			.satisfies(muro -> {
				assertThat(muro).extractingPath("$[0].id").isEqualTo(publicado);
				assertThat(muro).extractingPath("$[0].autor.nombre").isEqualTo("Docente de prueba");
				assertThat(muro).extractingPath("$[0].destinatario.nombre").isEqualTo("Estudiante de prueba");
				assertThat(muro).extractingPath("$[0].categoria").isEqualTo("Empatía");
			});
	}

	@Test
	void un_kudo_a_uno_mismo_responde_422_con_su_motivo() {
		assertThat(mvc.post().uri("/api/kudos").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE))
			.contentType(MediaType.APPLICATION_JSON)
			.content(cuerpo(PublicarKudoIT.DOCENTE, "Me lo merezco")))
			.hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
			.bodyJson().extractingPath("$.motivo").isEqualTo("AUTOR_ES_DESTINATARIO");
	}

	@Test
	void una_peticion_incompleta_responde_400() {
		assertThat(mvc.post().uri("/api/kudos").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{}")).hasStatus(HttpStatus.BAD_REQUEST);
	}

	@Test
	void ofrece_las_15_categorias_y_las_personas_sin_correo_ni_celular() {
		assertThat(mvc.get().uri("/api/categorias").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE)))
			.hasStatusOk()
			.bodyJson().extractingPath("$.length()").isEqualTo(15);
		assertThat(mvc.get().uri("/api/personas").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE)))
			.hasStatusOk()
			.bodyJson()
			.hasPath("$[0].nombre")
			.doesNotHavePath("$[0].correo")
			.doesNotHavePath("$[0].celular");
	}

	@Test
	void yo_devuelve_la_persona_de_la_sesion_con_su_rol() {
		assertThat(mvc.get().uri("/api/yo").header(HttpHeaders.AUTHORIZATION, bearer(TOKEN_DOCENTE)))
			.hasStatusOk()
			.bodyJson()
			.satisfies(yo -> {
				assertThat(yo).extractingPath("$.id").isEqualTo(PublicarKudoIT.DOCENTE.toString());
				assertThat(yo).extractingPath("$.rol").isEqualTo("DOCENTE");
			});
	}

	@Test
	void solo_el_origen_del_front_pasa_el_preflight_de_cors() {
		assertThat(mvc.options().uri("/api/kudos").header(HttpHeaders.ORIGIN, FRONT)
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
			.hasStatusOk()
			.headers().hasValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONT);
		assertThat(mvc.options().uri("/api/kudos").header(HttpHeaders.ORIGIN, "https://otro.example.test")
			.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

	private static Jwt token(String valor) {
		String sujeto = SUJETOS.get(valor);
		if (sujeto == null) {
			throw new BadJwtException("Token de prueba desconocido");
		}
		Instant ahora = Instant.now();
		return Jwt.withTokenValue(valor)
			.header("alg", "RS256")
			.subject(sujeto)
			.issuedAt(ahora)
			.expiresAt(ahora.plusSeconds(900))
			.build();
	}

	private String cuerpo(UUID destinatario, String contenido) {
		UUID empatia = jdbc.sql("SELECT id FROM kudos.categoria WHERE nombre = 'Empatía'").query(UUID.class).single();
		return """
				{"destinatario": "%s", "categoria": "%s", "contenido": "%s"}
				""".formatted(destinatario, empatia, contenido);
	}

}
