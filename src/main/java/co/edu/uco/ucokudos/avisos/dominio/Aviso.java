package co.edu.uco.ucokudos.avisos.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Un aviso de un Kudo por un canal: la fila del outbox que el despachador envía (ADR-003).
 * <p>Si un envío falla, se reintenta hasta 5 veces en 30 minutos, con espera creciente; agotados los
 * reintentos, el aviso queda fallido. Cada cambio de estado devuelve un aviso nuevo: el aviso es
 * inmutable.
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

	/** La espera antes de cada reintento: 1 + 2 + 4 + 8 + 15 = 30 minutos (ADR-003). */
	public static final List<Duration> ESPERAS_ANTES_DE_REINTENTAR = List.of(Duration.ofMinutes(1),
			Duration.ofMinutes(2), Duration.ofMinutes(4), Duration.ofMinutes(8), Duration.ofMinutes(15));

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

	/** Si el despachador ya puede intentarlo: está pendiente y le llegó la hora. */
	public boolean estaListo(Instant ahora) {
		return estado == EstadoDelAviso.PENDIENTE && !proximoIntentoEn.isAfter(ahora);
	}

	/** El proveedor del canal lo aceptó. */
	public Aviso entregado(Instant ahora) {
		return conIntento(EstadoDelAviso.ENTREGADO, ahora);
	}

	/** No se envió a propósito; por ejemplo, el push con la aplicación cerrada (D-04). */
	public Aviso omitido(Instant ahora) {
		return conIntento(EstadoDelAviso.OMITIDO, ahora);
	}

	/**
	 * El envío falló: queda pendiente para el próximo reintento o, si ya no quedan, fallido.
	 */
	public Aviso fallo(Instant ahora) {
		int fallos = intentos + 1;
		if (fallos > ESPERAS_ANTES_DE_REINTENTAR.size()) {
			return conIntento(EstadoDelAviso.FALLIDO, ahora);
		}
		Instant proximo = ahora.plus(ESPERAS_ANTES_DE_REINTENTAR.get(fallos - 1));
		return new Aviso(id, kudo, canal, EstadoDelAviso.PENDIENTE, fallos, proximo, creadoEn);
	}

	private Aviso conIntento(EstadoDelAviso nuevoEstado, Instant ahora) {
		return new Aviso(id, kudo, canal, nuevoEstado, intentos + 1, ahora, creadoEn);
	}

}
