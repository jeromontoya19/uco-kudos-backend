package co.edu.uco.ucokudos.avisos.infraestructura.programacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;

class DespachadorProgramadoTest {

	@Test
	void si_un_aviso_revienta_la_tanda_sigue_con_los_demas() {
		UUID roto = UUID.randomUUID();
		UUID sano = UUID.randomUUID();
		List<UUID> despachados = new ArrayList<>();
		DespacharAvisos despacho = new DespacharAvisos() {

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

		new DespachadorProgramado(despacho).despacharListos();

		assertThat(despachados).containsExactly(sano);
	}

}
