package co.edu.uco.ucokudos.avisos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.HechoAuditable;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.Resultado;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.CanalDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.MensajeDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.ResultadoDelEnvio;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;
import co.edu.uco.ucokudos.avisos.dominio.Canal;
import co.edu.uco.ucokudos.avisos.dominio.DatosDelKudo;
import co.edu.uco.ucokudos.avisos.dominio.EstadoDelAviso;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.Contacto;

/**
 * El despachador sin Spring, sin base y sin proveedores: los canales son dobles que responden lo que
 * cada prueba necesita.
 */
class DespachoDeAvisosTest {

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	private static final Contacto DOCENTE = new Contacto(UUID.randomUUID(), "Docente", "docente@example.com",
			"+570000000006");

	private static final Contacto ESTUDIANTE = new Contacto(UUID.randomUUID(), "Estudiante", "estudiante@example.com",
			"+570000000007");

	private static final DatosDelKudo KUDO = new DatosDelKudo(UUID.randomUUID(), DOCENTE.id(), ESTUDIANTE.id(),
			"Empatía");

	private final AvisosEnMemoria outbox = new AvisosEnMemoria();

	private final List<HechoAuditable> auditados = new ArrayList<>();

	private final List<MensajeDeAviso> enviados = new ArrayList<>();

	private final Map<Canal, ResultadoDelEnvio> respuestas = new EnumMap<>(Canal.class);

	private final Map<UUID, Contacto> contactos = new HashMap<>(Map.of(DOCENTE.id(), DOCENTE, ESTUDIANTE.id(),
			ESTUDIANTE));

	private DespachoDeAvisos despacho;

	@BeforeEach
	void prepararDobles() {
		List<CanalDeAviso> canales = List.of(canalFalso(Canal.CORREO), canalFalso(Canal.WHATSAPP),
				canalFalso(Canal.PUSH));
		despacho = new DespachoDeAvisos(outbox, canales, id -> Optional.ofNullable(contactos.get(id)),
				auditados::add, Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	@Test
	void un_aviso_entregado_queda_cerrado_y_auditado_con_exito() {
		Aviso aviso = programar(Canal.CORREO, ResultadoDelEnvio.ENTREGADO);

		despacharListos();

		assertThat(estadoDe(aviso)).isEqualTo(EstadoDelAviso.ENTREGADO);
		assertThat(enviados).singleElement().satisfies(mensaje -> {
			assertThat(mensaje.destinatario().correo()).isEqualTo("estudiante@example.com");
			assertThat(mensaje.autor()).isEqualTo("Docente");
			assertThat(mensaje.categoria()).isEqualTo("Empatía");
		});
		assertThat(auditados).containsExactly(new HechoAuditable("AVISO_CORREO", HechoAuditable.SISTEMA, "KUDO",
				KUDO.kudo(), Resultado.EXITO));
	}

	@Test
	void un_push_sin_la_aplicacion_abierta_queda_omitido_y_auditado() {
		Aviso aviso = programar(Canal.PUSH, ResultadoDelEnvio.OMITIDO);

		despacharListos();

		assertThat(estadoDe(aviso)).isEqualTo(EstadoDelAviso.OMITIDO);
		assertThat(auditados).extracting(HechoAuditable::accion, HechoAuditable::resultado)
			.containsExactly(tuple("AVISO_PUSH", Resultado.OMITIDO));
	}

	@Test
	void un_envio_fallido_queda_pendiente_para_reintento_y_auditado_como_fallo() {
		Aviso aviso = programar(Canal.WHATSAPP, ResultadoDelEnvio.FALLIDO);

		despacharListos();

		Aviso despues = outbox.buscarPorId(aviso.id()).orElseThrow();
		assertThat(despues.estado()).isEqualTo(EstadoDelAviso.PENDIENTE);
		assertThat(despues.intentos()).isEqualTo(1);
		assertThat(despues.proximoIntentoEn()).isAfter(AHORA);
		assertThat(despacho.listosParaEnviar()).isEmpty();
		assertThat(auditados).extracting(HechoAuditable::resultado).containsExactly(Resultado.FALLO);
	}

	@Test
	void si_el_destinatario_ya_no_existe_el_aviso_falla_sin_llamar_al_canal() {
		contactos.remove(ESTUDIANTE.id());
		Aviso aviso = programar(Canal.CORREO, ResultadoDelEnvio.ENTREGADO);

		despacharListos();

		assertThat(enviados).isEmpty();
		assertThat(outbox.buscarPorId(aviso.id()).orElseThrow().intentos()).isEqualTo(1);
	}

	@Test
	void si_el_autor_ya_no_existe_el_mensaje_igual_sale_sin_su_nombre() {
		contactos.remove(DOCENTE.id());
		programar(Canal.CORREO, ResultadoDelEnvio.ENTREGADO);

		despacharListos();

		assertThat(enviados).singleElement()
			.satisfies(mensaje -> assertThat(mensaje.autor()).isEqualTo(DespachoDeAvisos.AUTOR_NO_DISPONIBLE));
	}

	@Test
	void un_aviso_que_ya_no_esta_listo_no_se_vuelve_a_enviar() {
		Aviso aviso = programar(Canal.CORREO, ResultadoDelEnvio.ENTREGADO);
		despacharListos();

		despacho.despachar(aviso.id());

		assertThat(enviados).hasSize(1);
	}

	@Test
	void no_arranca_si_a_un_canal_le_falta_su_adaptador() {
		List<CanalDeAviso> sinPush = List.of(canalFalso(Canal.CORREO), canalFalso(Canal.WHATSAPP));

		assertThatThrownBy(() -> new DespachoDeAvisos(outbox, sinPush, id -> Optional.empty(), auditados::add,
				Clock.systemUTC())).isInstanceOf(IllegalStateException.class).hasMessageContaining("PUSH");
	}

	private Aviso programar(Canal canal, ResultadoDelEnvio respuesta) {
		respuestas.put(canal, respuesta);
		Aviso aviso = Aviso.pendiente(KUDO, canal, AHORA);
		outbox.guardarTodos(List.of(aviso));
		return aviso;
	}

	private void despacharListos() {
		despacho.listosParaEnviar().forEach(despacho::despachar);
	}

	private EstadoDelAviso estadoDe(Aviso aviso) {
		return outbox.buscarPorId(aviso.id()).orElseThrow().estado();
	}

	private CanalDeAviso canalFalso(Canal canal) {
		return new CanalDeAviso() {

			@Override
			public Canal canal() {
				return canal;
			}

			@Override
			public ResultadoDelEnvio enviar(MensajeDeAviso mensaje) {
				enviados.add(mensaje);
				return respuestas.getOrDefault(canal, ResultadoDelEnvio.FALLIDO);
			}

		};
	}

}
