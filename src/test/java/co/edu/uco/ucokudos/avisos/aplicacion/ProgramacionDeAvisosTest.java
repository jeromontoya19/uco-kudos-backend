package co.edu.uco.ucokudos.avisos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.KudoPorAvisar;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;
import co.edu.uco.ucokudos.avisos.dominio.Canal;
import co.edu.uco.ucokudos.avisos.dominio.DatosDelKudo;
import co.edu.uco.ucokudos.avisos.dominio.EstadoDelAviso;

class ProgramacionDeAvisosTest {

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	@Test
	void deja_un_aviso_pendiente_por_cada_canal_listo_para_enviarse() {
		List<Aviso> guardados = new ArrayList<>();
		ProgramacionDeAvisos programacion = new ProgramacionDeAvisos(guardados::addAll, Clock.fixed(AHORA, ZoneOffset.UTC));
		KudoPorAvisar kudo = new KudoPorAvisar(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Empatía");

		programacion.programar(kudo);

		assertThat(guardados).extracting(Aviso::canal).containsExactly(Canal.CORREO, Canal.WHATSAPP, Canal.PUSH);
		assertThat(guardados).allSatisfy(aviso -> {
			assertThat(aviso.estado()).isEqualTo(EstadoDelAviso.PENDIENTE);
			assertThat(aviso.intentos()).isZero();
			assertThat(aviso.proximoIntentoEn()).isEqualTo(AHORA);
			assertThat(aviso.kudo()).isEqualTo(new DatosDelKudo(kudo.kudo(), kudo.autor(), kudo.destinatario(), "Empatía"));
		});
		assertThat(guardados).extracting(Aviso::id).doesNotHaveDuplicates();
	}

	@Test
	void un_aviso_no_puede_tener_intentos_negativos() {
		DatosDelKudo datos = new DatosDelKudo(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Empatía");

		assertThatThrownBy(
				() -> new Aviso(UUID.randomUUID(), datos, Canal.CORREO, EstadoDelAviso.PENDIENTE, -1, AHORA, AHORA))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
