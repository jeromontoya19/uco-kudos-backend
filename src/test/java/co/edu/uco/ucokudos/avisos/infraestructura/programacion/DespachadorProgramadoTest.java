package co.edu.uco.ucokudos.avisos.infraestructura.programacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;

class DespachadorProgramadoTest {

	private final UUID roto = UUID.randomUUID();

	private final UUID sano = UUID.randomUUID();

	private final List<UUID> despachados = new ArrayList<>();

	private final DespacharAvisos despacho = new DespacharAvisos() {

		@Override
		public List<UUID> listosParaEnviar() {
			return List.of(roto, sano);
		}

		@Override
		public void despachar(UUID aviso) {
			if (aviso.equals(roto)) {
				throw new IllegalStateException("La base no respondió");
			}
			despachados.add(aviso);
		}

	};

	@Test
	void si_un_aviso_revienta_la_tanda_sigue_con_los_demas() {
		new DespachadorProgramado(despacho, Runnable::run).despacharListos();

		assertThat(despachados).containsExactly(sano);
	}

	@Test
	void al_programar_avisos_despacha_en_otro_hilo_sin_esperar_la_siguiente_vuelta() {
		List<Runnable> tareas = new ArrayList<>();
		DespachadorProgramado despachador = new DespachadorProgramado(despacho, tareas::add);

		despachador.alProgramarAvisos(new AvisosProgramados(UUID.randomUUID()));

		assertThat(despachados).as("quien publica no espera a los canales").isEmpty();
		tareas.forEach(Runnable::run);
		assertThat(despachados).containsExactly(sano);
	}

}
