package co.edu.uco.ucokudos.avisos.infraestructura.envivo;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * El canal en vivo: el front abre esta conexión mientras la aplicación está abierta y recibe por ella
 * los avisos de Kudos. Como exige el token en el encabezado, el front la abre con {@code fetch} y no con
 * {@code EventSource}, que no deja enviar encabezados.
 */
@RestController
class ControladorEnVivo {

	private final ConexionesEnVivo conexiones;

	ControladorEnVivo(ConexionesEnVivo conexiones) {
		this.conexiones = conexiones;
	}

	@GetMapping(path = "/api/avisos/en-vivo", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	SseEmitter conectar(Authentication sesion) {
		return conexiones.conectar(UUID.fromString(sesion.getName()));
	}

}
