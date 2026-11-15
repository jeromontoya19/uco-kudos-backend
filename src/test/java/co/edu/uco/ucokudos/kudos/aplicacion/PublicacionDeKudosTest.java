package co.edu.uco.ucokudos.kudos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.HechoAuditable;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.Resultado;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.KudoPorAvisar;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;
import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;
import co.edu.uco.ucokudos.kudos.dominio.Kudo;
import co.edu.uco.ucokudos.kudos.dominio.KudoInvalido;

/**
 * Prueba el caso de uso sin Spring ni base de datos: cada puerto es un doble hecho con una lambda.
 */
class PublicacionDeKudosTest {

	private static final Instant AHORA = Instant.parse("2026-11-14T22:00:00Z");

	private static final UUID DOCENTE = UUID.randomUUID();

	private static final UUID ESTUDIANTE = UUID.randomUUID();

	private static final IdCategoria EMPATIA = new IdCategoria(UUID.randomUUID());

	private static final IdCategoria ARCHIVADA = new IdCategoria(UUID.randomUUID());

	private final List<Kudo> kudosGuardados = new ArrayList<>();

	private final List<KudoPorAvisar> avisosProgramados = new ArrayList<>();

	private final List<HechoAuditable> hechosAuditados = new ArrayList<>();

	private final Set<UUID> personasActivas = new HashSet<>();

	private final Map<IdCategoria, Categoria> categorias = new HashMap<>();

	private PublicacionDeKudos publicacion;

	@BeforeEach
	void prepararDobles() {
		personasActivas.addAll(Set.of(DOCENTE, ESTUDIANTE));
		categorias.put(EMPATIA, new Categoria(EMPATIA, "Empatía", true));
		categorias.put(ARCHIVADA, new Categoria(ARCHIVADA, "Archivada", false));
		publicacion = new PublicacionDeKudos(kudosGuardados::add, id -> Optional.ofNullable(categorias.get(id)),
				id -> personasActivas.contains(id) ? Optional.of(new PersonaActiva(id, "Persona")) : Optional.empty(),
				avisosProgramados::add, hechosAuditados::add, Clock.fixed(AHORA, ZoneOffset.UTC));
	}

	@Test
	void publicar_guarda_el_kudo_programa_sus_avisos_y_lo_audita() {
		KudoPublicado publicado = publicacion.publicar(solicitud(DOCENTE, ESTUDIANTE, EMPATIA, "Gracias por tu ayuda"));

		assertThat(publicado.publicadoEn()).isEqualTo(AHORA);
		assertThat(kudosGuardados).singleElement().satisfies(kudo -> {
			assertThat(kudo.id().valor()).isEqualTo(publicado.id());
			assertThat(kudo.autor().valor()).isEqualTo(DOCENTE);
			assertThat(kudo.destinatario().valor()).isEqualTo(ESTUDIANTE);
		});
		assertThat(avisosProgramados)
			.containsExactly(new KudoPorAvisar(publicado.id(), DOCENTE, ESTUDIANTE, "Empatía"));
		assertThat(hechosAuditados).containsExactly(new HechoAuditable(PublicacionDeKudos.ACCION_PUBLICAR, DOCENTE,
				PublicacionDeKudos.TIPO_DE_RECURSO, publicado.id(), Resultado.EXITO));
	}

	@Test
	void rechaza_si_el_destinatario_no_tiene_una_cuenta_activa() {
		personasActivas.remove(ESTUDIANTE);

		assertRechazo(solicitud(DOCENTE, ESTUDIANTE, EMPATIA, "Gracias"),
				PublicacionRechazada.Motivo.DESTINATARIO_NO_DISPONIBLE);
	}

	@Test
	void rechaza_si_el_autor_no_tiene_una_cuenta_activa() {
		personasActivas.remove(DOCENTE);

		assertRechazo(solicitud(DOCENTE, ESTUDIANTE, EMPATIA, "Gracias"), PublicacionRechazada.Motivo.AUTOR_NO_DISPONIBLE);
	}

	@Test
	void rechaza_si_la_categoria_no_esta_activa_o_no_existe() {
		assertRechazo(solicitud(DOCENTE, ESTUDIANTE, ARCHIVADA, "Gracias"),
				PublicacionRechazada.Motivo.CATEGORIA_NO_DISPONIBLE);
		assertRechazo(solicitud(DOCENTE, ESTUDIANTE, new IdCategoria(UUID.randomUUID()), "Gracias"),
				PublicacionRechazada.Motivo.CATEGORIA_NO_DISPONIBLE);
	}

	@Test
	void rechaza_un_kudo_a_uno_mismo_sin_guardar_nada() {
		assertThatThrownBy(() -> publicacion.publicar(solicitud(DOCENTE, DOCENTE, EMPATIA, "Me lo merezco")))
			.isInstanceOf(KudoInvalido.class);
		assertNoSeGuardoNada();
	}

	@Test
	void rechaza_un_contenido_vacio_sin_guardar_nada() {
		assertThatThrownBy(() -> publicacion.publicar(solicitud(DOCENTE, ESTUDIANTE, EMPATIA, " ")))
			.isInstanceOf(KudoInvalido.class);
		assertNoSeGuardoNada();
	}

	private static SolicitudDePublicacion solicitud(UUID autor, UUID destinatario, IdCategoria categoria,
			String contenido) {
		return new SolicitudDePublicacion(autor, destinatario, categoria.valor(), contenido);
	}

	private void assertRechazo(SolicitudDePublicacion solicitud, PublicacionRechazada.Motivo motivo) {
		assertThatThrownBy(() -> publicacion.publicar(solicitud)).isInstanceOfSatisfying(PublicacionRechazada.class,
				error -> assertThat(error.motivo()).isEqualTo(motivo));
		assertNoSeGuardoNada();
	}

	private void assertNoSeGuardoNada() {
		assertThat(kudosGuardados).isEmpty();
		assertThat(avisosProgramados).isEmpty();
		assertThat(hechosAuditados).isEmpty();
	}

}
