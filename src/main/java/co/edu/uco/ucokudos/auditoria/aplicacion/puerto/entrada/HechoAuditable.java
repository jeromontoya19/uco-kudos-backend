package co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada;

import java.util.Objects;
import java.util.UUID;

/**
 * Lo que otro módulo le pide a la auditoría que registre. Solo lleva identificadores (ADR-008).
 *
 * @param accion      qué pasó, como un código (por ejemplo, {@code KUDO_PUBLICADO})
 * @param actor       quién lo hizo
 * @param tipoRecurso sobre qué tipo de recurso (por ejemplo, {@code KUDO})
 * @param idRecurso   sobre qué recurso
 * @param resultado   cómo terminó
 */
public record HechoAuditable(String accion, UUID actor, String tipoRecurso, UUID idRecurso, Resultado resultado) {

	/** El actor de lo que hace la propia API sin que nadie lo pida en ese momento, como el despachador. */
	public static final UUID SISTEMA = new UUID(0L, 0L);

	public HechoAuditable {
		Objects.requireNonNull(accion, "accion");
		Objects.requireNonNull(actor, "actor");
		Objects.requireNonNull(tipoRecurso, "tipoRecurso");
		Objects.requireNonNull(idRecurso, "idRecurso");
		Objects.requireNonNull(resultado, "resultado");
	}

}
