package co.edu.uco.ucokudos.kudos.infraestructura.configuracion;

import java.util.Objects;

import org.springframework.transaction.support.TransactionOperations;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;

/**
 * Decorador que envuelve la publicación en una transacción: el Kudo, sus avisos pendientes y su
 * registro de auditoría se guardan juntos o no se guarda ninguno (ADR-003). Así el caso de uso no
 * depende de Spring.
 */
final class PublicarKudoEnTransaccion implements PublicarKudo {

	private final PublicarKudo publicacion;

	private final TransactionOperations transaccion;

	PublicarKudoEnTransaccion(PublicarKudo publicacion, TransactionOperations transaccion) {
		this.publicacion = Objects.requireNonNull(publicacion, "publicacion");
		this.transaccion = Objects.requireNonNull(transaccion, "transaccion");
	}

	@Override
	public KudoPublicado publicar(SolicitudDePublicacion solicitud) {
		return Objects.requireNonNull(transaccion.execute(estado -> publicacion.publicar(solicitud)));
	}

}
