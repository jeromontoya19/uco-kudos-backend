package co.edu.uco.ucokudos.kudos.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class KudoTest {

	private static final IdPersona DOCENTE = new IdPersona(UUID.randomUUID());

	private static final IdPersona ESTUDIANTE = new IdPersona(UUID.randomUUID());

	private static final IdCategoria EMPATIA = new IdCategoria(UUID.randomUUID());

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	@Test
	void un_kudo_valido_conserva_sus_datos() {
		KudoId id = KudoId.nuevo();

		Kudo kudo = new Kudo(id, DOCENTE, ESTUDIANTE, EMPATIA, new Contenido("Gracias por tu ayuda"), AHORA);

		assertThat(kudo.id()).isEqualTo(id);
		assertThat(kudo.autor()).isEqualTo(DOCENTE);
		assertThat(kudo.destinatario()).isEqualTo(ESTUDIANTE);
		assertThat(kudo.contenido().texto()).isEqualTo("Gracias por tu ayuda");
		assertThat(kudo.publicadoEn()).isEqualTo(AHORA);
	}

	@Test
	void nadie_puede_darse_un_kudo_a_si_mismo() {
		Contenido contenido = new Contenido("Me lo merezco");

		assertThatThrownBy(() -> new Kudo(KudoId.nuevo(), DOCENTE, DOCENTE, EMPATIA, contenido, AHORA))
			.isInstanceOfSatisfying(KudoInvalido.class,
					error -> assertThat(error.motivo()).isEqualTo(KudoInvalido.Motivo.AUTOR_ES_DESTINATARIO));
	}

	@Test
	void el_contenido_no_puede_quedar_vacio() {
		assertThatThrownBy(() -> new Contenido("   "))
			.isInstanceOfSatisfying(KudoInvalido.class,
					error -> assertThat(error.motivo()).isEqualTo(KudoInvalido.Motivo.CONTENIDO_VACIO));
		assertThatThrownBy(() -> new Contenido(null)).isInstanceOf(KudoInvalido.class);
	}

	@Test
	void el_contenido_no_tiene_limite_de_longitud() {
		String muyLargo = "Gracias. ".repeat(20_000);

		assertThat(new Contenido(muyLargo).texto()).hasSize(muyLargo.length());
	}

	@Test
	void los_identificadores_no_aceptan_nulos() {
		assertThatThrownBy(() -> new IdPersona(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new IdCategoria(null)).isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new KudoId(null)).isInstanceOf(NullPointerException.class);
	}

}
