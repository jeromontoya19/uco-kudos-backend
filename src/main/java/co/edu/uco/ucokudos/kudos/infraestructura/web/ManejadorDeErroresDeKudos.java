package co.edu.uco.ucokudos.kudos.infraestructura.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.uco.ucokudos.kudos.aplicacion.PublicacionRechazada;
import co.edu.uco.ucokudos.kudos.dominio.KudoInvalido;

/**
 * Traduce los rechazos del negocio a 422 con su motivo, para que el front muestre un mensaje claro en
 * vez de un error genérico.
 */
@RestControllerAdvice(assignableTypes = ControladorDeKudos.class)
class ManejadorDeErroresDeKudos {

	static final String PROPIEDAD_MOTIVO = "motivo";

	@ExceptionHandler(KudoInvalido.class)
	ProblemDetail kudoInvalido(KudoInvalido error) {
		return rechazo("El Kudo no cumple las reglas", error.getMessage(), error.motivo().name());
	}

	@ExceptionHandler(PublicacionRechazada.class)
	ProblemDetail publicacionRechazada(PublicacionRechazada error) {
		return rechazo("No se pudo publicar el Kudo", error.getMessage(), error.motivo().name());
	}

	private static ProblemDetail rechazo(String titulo, String detalle, String motivo) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, detalle);
		problema.setTitle(titulo);
		problema.setProperty(PROPIEDAD_MOTIVO, motivo);
		return problema;
	}

}
