package co.edu.uco.ucokudos.avisos.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Un aviso de un Kudo por un canal: la fila del outbox que el despachador envía (ADR-003).
 *
 * @param id               identificador del aviso
 * @param kudo             los datos del Kudo que necesita el mensaje
 * @param canal            por dónde se envía
 * @param estado           en qué va
 * @param intentos         cuántas veces se intentó enviar
 * @param proximoIntentoEn desde cuándo el despachador puede intentarlo
 * @param creadoEn         cuándo se programó
 */
public record Aviso(UUID id, DatosDelKudo kudo, Canal canal, EstadoDelAviso estado, int intentos,
		Instant proximoIntentoEn, Instant creadoEn) {

	public Aviso {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(kudo, "kudo");
		Objects.requireNonNull(canal, "canal");
		Objects.requireNonNull(estado, "estado");
		Objects.requireNonNull(proximoIntentoEn, "proximoIntentoEn");
		Objects.requireNonNull(creadoEn, "creadoEn");
		if (intentos < 0) {
			throw new IllegalArgumentException("Un aviso no puede tener intentos negativos");
		}
	}

	/**
	 * Un aviso recién programado: pendiente, sin intentos y listo para enviarse ya.
	 */
	public static Aviso pendiente(DatosDelKudo kudo, Canal canal, Instant ahora) {
		return new Aviso(UUID.randomUUID(), kudo, canal, EstadoDelAviso.PENDIENTE, 0, ahora, ahora);
	}

}
