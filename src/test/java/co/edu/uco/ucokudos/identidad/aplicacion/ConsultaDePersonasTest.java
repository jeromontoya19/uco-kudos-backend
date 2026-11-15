package co.edu.uco.ucokudos.identidad.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;
import co.edu.uco.ucokudos.identidad.dominio.Persona;
import co.edu.uco.ucokudos.identidad.dominio.Rol;

class ConsultaDePersonasTest {

	private static final Persona DOCENTE = persona("Docente", Rol.DOCENTE, true);

	private static final Persona ESTUDIANTE_INACTIVO = persona("Estudiante", Rol.ESTUDIANTE, false);

	private final ConsultaDePersonas consulta = new ConsultaDePersonas(
			id -> Optional.ofNullable(Map.of(DOCENTE.id(), DOCENTE, ESTUDIANTE_INACTIVO.id(), ESTUDIANTE_INACTIVO).get(id)));

	@Test
	void devuelve_solo_el_nombre_de_una_persona_activa() {
		assertThat(consulta.buscarActiva(DOCENTE.id())).contains(new PersonaActiva(DOCENTE.id(), "Docente"));
	}

	@Test
	void una_persona_inactiva_o_inexistente_no_esta_disponible() {
		assertThat(consulta.buscarActiva(ESTUDIANTE_INACTIVO.id())).isEmpty();
		assertThat(consulta.buscarActiva(UUID.randomUUID())).isEmpty();
	}

	private static Persona persona(String nombre, Rol rol, boolean activa) {
		UUID id = UUID.randomUUID();
		return new Persona(id, nombre, id + "@example.com", "+57" + id.hashCode(), rol, activa);
	}

}
