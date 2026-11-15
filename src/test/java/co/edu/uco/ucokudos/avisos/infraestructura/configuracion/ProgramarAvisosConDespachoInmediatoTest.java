package co.edu.uco.ucokudos.avisos.infraestructura.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.KudoPorAvisar;
import co.edu.uco.ucokudos.avisos.infraestructura.programacion.AvisosProgramados;

class ProgramarAvisosConDespachoInmediatoTest {

	@Test
	void deja_los_avisos_pendientes_y_despues_anuncia_que_hay_avisos_nuevos() {
		List<Object> pasos = new ArrayList<>();
		KudoPorAvisar kudo = new KudoPorAvisar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Empatía");
		ProgramarAvisosConDespachoInmediato programacion = new ProgramarAvisosConDespachoInmediato(pasos::add,
				pasos::add);

		programacion.programar(kudo);

		assertThat(pasos).containsExactly(kudo, new AvisosProgramados(kudo.kudo()));
	}

}
