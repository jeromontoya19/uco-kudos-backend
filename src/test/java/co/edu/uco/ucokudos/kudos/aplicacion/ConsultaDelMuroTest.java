package co.edu.uco.ucokudos.kudos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoDelMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.LecturaDelMuro.KudoLeido;

class ConsultaDelMuroTest {

	private static final UUID DOCENTE = UUID.randomUUID();

	private static final UUID ESTUDIANTE = UUID.randomUUID();

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	private record Lectura(int desde, int cantidad) {
	}

	private final List<Lectura> lecturas = new ArrayList<>();

	private final PersonasEnMemoria personas = new PersonasEnMemoria().con(DOCENTE, "Docente").con(ESTUDIANTE, "Estudiante");

	@Test
	void pone_el_nombre_del_autor_y_del_destinatario_a_cada_kudo() {
		KudoLeido leido = new KudoLeido(UUID.randomUUID(), DOCENTE, ESTUDIANTE, "Empatía", "Gracias", AHORA);
		ConsultaDelMuro consulta = consultaQueLee(List.of(leido));

		List<KudoDelMuro> muro = consulta.pagina(0, 20);

		assertThat(muro).containsExactly(new KudoDelMuro(leido.id(), new KudoDelMuro.Persona(DOCENTE, "Docente"),
				new KudoDelMuro.Persona(ESTUDIANTE, "Estudiante"), "Empatía", "Gracias", AHORA));
	}

	@Test
	void una_persona_que_ya_no_existe_se_muestra_como_no_disponible() {
		UUID desconocida = UUID.randomUUID();
		ConsultaDelMuro consulta = consultaQueLee(
				List.of(new KudoLeido(UUID.randomUUID(), desconocida, ESTUDIANTE, "Empatía", "Gracias", AHORA)));

		assertThat(consulta.pagina(0, 20)).singleElement()
			.satisfies(kudo -> assertThat(kudo.autor().nombre()).isEqualTo(ConsultaDelMuro.NOMBRE_NO_DISPONIBLE));
	}

	@Test
	void la_pagina_se_ajusta_a_los_limites() {
		ConsultaDelMuro consulta = consultaQueLee(List.of());

		consulta.pagina(2, 10);
		consulta.pagina(-1, 500);
		consulta.pagina(0, 0);

		assertThat(lecturas).containsExactly(new Lectura(20, 10), new Lectura(0, ConsultarMuro.TAMANO_MAXIMO),
				new Lectura(0, 1));
	}

	private ConsultaDelMuro consultaQueLee(List<KudoLeido> kudos) {
		return new ConsultaDelMuro((desde, cantidad) -> {
			lecturas.add(new Lectura(desde, cantidad));
			return kudos;
		}, personas);
	}

}
