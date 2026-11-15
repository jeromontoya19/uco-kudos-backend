package co.edu.uco.ucokudos.identidad.infraestructura.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.IdentificarPersona;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaActiva;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaIdentificada;

/**
 * Las personas, para elegir a quién darle un Kudo, y la persona de la sesión actual. Nunca expone el
 * correo ni el celular (D-16).
 */
@RestController
@RequestMapping("/api")
class ControladorDePersonas {

	private final ConsultarPersonas consultar;

	private final IdentificarPersona identificar;

	ControladorDePersonas(ConsultarPersonas consultar, IdentificarPersona identificar) {
		this.consultar = consultar;
		this.identificar = identificar;
	}

	@GetMapping("/personas")
	List<PersonaActiva> personas() {
		return consultar.listarActivas();
	}

	@GetMapping("/yo")
	ResponseEntity<PersonaIdentificada> yo(@AuthenticationPrincipal Jwt token) {
		return ResponseEntity.of(identificar.porSujeto(token.getSubject()));
	}

}
