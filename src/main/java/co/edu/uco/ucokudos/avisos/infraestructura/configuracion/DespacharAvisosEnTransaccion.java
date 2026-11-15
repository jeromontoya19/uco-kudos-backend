package co.edu.uco.ucokudos.avisos.infraestructura.configuracion;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.transaction.support.TransactionOperations;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;

/**
 * Decorador que pone cada aviso en su propia transacción: su nuevo estado y su registro de auditoría se
 * guardan juntos, y si un aviso falla, los demás de la tanda siguen.
 */
final class DespacharAvisosEnTransaccion implements DespacharAvisos {

	private final DespacharAvisos despacho;

	private final TransactionOperations transaccion;

	DespacharAvisosEnTransaccion(DespacharAvisos despacho, TransactionOperations transaccion) {
		this.despacho = Objects.requireNonNull(despacho, "despacho");
		this.transaccion = Objects.requireNonNull(transaccion, "transaccion");
	}

	@Override
	public List<UUID> listosParaEnviar() {
		return despacho.listosParaEnviar();
	}

	@Override
	public void despachar(UUID aviso) {
		transaccion.executeWithoutResult(estado -> despacho.despachar(aviso));
	}

}
