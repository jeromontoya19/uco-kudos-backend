package co.edu.uco.ucokudos.identidad.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.Contacto;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaIdentificada;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida.RepositorioDePersonas;
import co.edu.uco.ucokudos.identidad.dominio.Persona;
import co.edu.uco.ucokudos.identidad.dominio.Rol;

class ConsultaDePersonasTest {

	private static final Persona DOCENTE = persona("Docente", Rol.DOCENTE, true);

	private static final Persona ESTUDIANTE_INACTIVO = persona("Estudiante", Rol.ESTUDIANTE, false);

	private static final Map<String, Persona> CUENTAS_DEL_IDP = Map.of("auth0|docente", DOCENTE, "auth0|estudiante",
			ESTUDIANTE_INACTIVO);

	private final ConsultaDePersonas consulta = new ConsultaDePersonas(new RepositorioEnMemoria(List.of(DOCENTE,
			ESTUDIANTE_INACTIVO)));

	@Test
	void devuelve_solo_el_nombre_de_una_persona_activa() {
		assertThat(consulta.buscarActiva(DOCENTE.id())).contains(new PersonaActiva(DOCENTE.id(), "Docente"));
	}

	@Test
	void una_persona_inactiva_o_inexistente_no_esta_disponible() {
		assertThat(consulta.buscarActiva(ESTUDIANTE_INACTIVO.id())).isEmpty();
		assertThat(consulta.buscarActiva(UUID.randomUUID())).isEmpty();
	}

	@Test
	void para_elegir_destinatario_solo_lista_personas_activas() {
		assertThat(consulta.listarActivas()).containsExactly(new PersonaActiva(DOCENTE.id(), "Docente"));
	}

	@Test
	void da_los_nombres_de_varias_personas_aunque_esten_inactivas() {
		assertThat(consulta.nombresDe(Set.of(DOCENTE.id(), ESTUDIANTE_INACTIVO.id(), UUID.randomUUID())))
			.containsOnly(Map.entry(DOCENTE.id(), "Docente"), Map.entry(ESTUDIANTE_INACTIVO.id(), "Estudiante"));
		assertThat(consulta.nombresDe(Set.of())).isEmpty();
	}

	@Test
	void identifica_la_cuenta_del_idp_solo_si_su_persona_esta_activa() {
		assertThat(consulta.porSujeto("auth0|docente"))
			.contains(new PersonaIdentificada(DOCENTE.id(), "Docente", "DOCENTE"));
		assertThat(consulta.porSujeto("auth0|estudiante")).isEmpty();
		assertThat(consulta.porSujeto("auth0|sin-persona")).isEmpty();
	}

	@Test
	void da_el_contacto_para_avisar_aunque_la_persona_este_inactiva() {
		assertThat(consulta.contactoDe(DOCENTE.id())).contains(new Contacto(DOCENTE.id(), "Docente", DOCENTE.correo(),
				DOCENTE.celular()));
		assertThat(consulta.contactoDe(ESTUDIANTE_INACTIVO.id())).isPresent();
		assertThat(consulta.contactoDe(UUID.randomUUID())).isEmpty();
	}

	private static Persona persona(String nombre, Rol rol, boolean activa) {
		UUID id = UUID.randomUUID();
		return new Persona(id, nombre, id + "@example.com", "+57" + id.hashCode(), rol, activa);
	}

	private record RepositorioEnMemoria(List<Persona> personas) implements RepositorioDePersonas {

		@Override
		public Optional<Persona> buscarPorId(UUID id) {
			return personas.stream().filter(persona -> persona.id().equals(id)).findFirst();
		}

		@Override
		public Optional<Persona> buscarPorSujeto(String sujeto) {
			return Optional.ofNullable(CUENTAS_DEL_IDP.get(sujeto));
		}

		@Override
		public List<Persona> listarActivas() {
			return personas.stream().filter(Persona::activa).sorted(Comparator.comparing(Persona::nombre)).toList();
		}

		@Override
		public List<Persona> buscarPorIds(Set<UUID> ids) {
			return personas.stream().filter(persona -> ids.contains(persona.id())).toList();
		}

	}

}
