package co.edu.uco.ucokudos.auditoria.aplicacion;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.HechoAuditable;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.salida.BitacoraDeAuditoria;
import co.edu.uco.ucokudos.auditoria.dominio.RegistroDeAuditoria;

/**
 * Convierte un hecho auditable en un registro con su fecha y lo agrega a la bitácora.
 */
public final class AnotacionEnAuditoria implements RegistrarEnAuditoria {

	private final BitacoraDeAuditoria bitacora;

	private final Clock reloj;

	public AnotacionEnAuditoria(BitacoraDeAuditoria bitacora, Clock reloj) {
		this.bitacora = Objects.requireNonNull(bitacora, "bitacora");
		this.reloj = Objects.requireNonNull(reloj, "reloj");
	}

	@Override
	public void registrar(HechoAuditable hecho) {
		bitacora.agregar(new RegistroDeAuditoria(UUID.randomUUID(), reloj.instant(), hecho.accion(), hecho.actor(),
				hecho.tipoRecurso(), hecho.idRecurso(), hecho.resultado().name()));
	}

}
