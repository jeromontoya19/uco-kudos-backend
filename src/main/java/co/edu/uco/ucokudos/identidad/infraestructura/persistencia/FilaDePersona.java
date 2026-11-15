package co.edu.uco.ucokudos.identidad.infraestructura.persistencia;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import co.edu.uco.ucokudos.identidad.dominio.Persona;
import co.edu.uco.ucokudos.identidad.dominio.Rol;

/**
 * Una fila de {@code identidad.persona}. Es el modelo de la base, no el del dominio.
 */
@Table(schema = "identidad", value = "persona")
record FilaDePersona(@Id UUID id, String nombre, String correo, String celular, String rol, boolean activa) {

	Persona aDominio() {
		return new Persona(id, nombre, correo, celular, Rol.valueOf(rol), activa);
	}

}
