package co.edu.uco.ucokudos.avisos.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AvisoTest {

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	private static final DatosDelKudo KUDO = new DatosDelKudo(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
			"Empatía");

	@Test
	void un_aviso_nuevo_esta_listo_de_inmediato() {
		Aviso aviso = Aviso.pendiente(KUDO, Canal.CORREO, AHORA);

		assertThat(aviso.estaListo(AHORA)).isTrue();
	}

	@Test
	void entregado_y_omitido_cierran_el_aviso_y_cuentan_el_intento() {
		Aviso aviso = Aviso.pendiente(KUDO, Canal.PUSH, AHORA);

		assertThat(aviso.entregado(AHORA)).extracting(Aviso::estado, Aviso::intentos)
			.containsExactly(EstadoDelAviso.ENTREGADO, 1);
		assertThat(aviso.omitido(AHORA)).extracting(Aviso::estado, Aviso::intentos)
			.containsExactly(EstadoDelAviso.OMITIDO, 1);
		assertThat(aviso.entregado(AHORA).estaListo(AHORA)).isFalse();
	}

	@Test
	void se_reintenta_cinco_veces_en_treinta_minutos_con_espera_creciente_y_luego_falla() {
		Aviso aviso = Aviso.pendiente(KUDO, Canal.WHATSAPP, AHORA);
		Instant momento = AHORA;
		List<Duration> esperas = new ArrayList<>();

		for (int reintento = 0; reintento < Aviso.ESPERAS_ANTES_DE_REINTENTAR.size(); reintento++) {
			aviso = aviso.fallo(momento);
			assertThat(aviso.estado()).isEqualTo(EstadoDelAviso.PENDIENTE);
			assertThat(aviso.estaListo(momento)).isFalse();
			esperas.add(Duration.between(momento, aviso.proximoIntentoEn()));
			momento = aviso.proximoIntentoEn();
		}
		Aviso agotado = aviso.fallo(momento);

		assertThat(esperas).containsExactly(Duration.ofMinutes(1), Duration.ofMinutes(2), Duration.ofMinutes(4),
				Duration.ofMinutes(8), Duration.ofMinutes(15));
		assertThat(Duration.between(AHORA, momento)).isEqualTo(Duration.ofMinutes(30));
		assertThat(agotado.estado()).isEqualTo(EstadoDelAviso.FALLIDO);
		assertThat(agotado.intentos()).isEqualTo(6);
	}

}
