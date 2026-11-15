package co.edu.uco.ucokudos.avisos.infraestructura.envivo;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Las conexiones abiertas del canal en vivo (Server-Sent Events, ADR-021), por persona. Una persona
 * «está conectada» si tiene al menos una pestaña con la aplicación abierta (D-04).
 * <p>Viven en la memoria de la única instancia (ADR-011). Con más instancias haría falta un relevo
 * compartido (T-3).
 */
@Component
public class ConexionesEnVivo {

	/** Cuánto dura una conexión antes de que el navegador se reconecte solo. */
	static final Duration DURACION = Duration.ofMinutes(30);

	private final Map<UUID, Set<SseEmitter>> conexiones = new ConcurrentHashMap<>();

	SseEmitter conectar(UUID persona) {
		SseEmitter emisor = new SseEmitter(DURACION.toMillis());
		conexiones.computeIfAbsent(persona, clave -> ConcurrentHashMap.newKeySet()).add(emisor);
		emisor.onCompletion(() -> quitar(persona, emisor));
		emisor.onTimeout(() -> quitar(persona, emisor));
		emisor.onError(error -> quitar(persona, emisor));
		return emisor;
	}

	/**
	 * Envía un evento a todas las pestañas abiertas de la persona.
	 *
	 * @return si llegó al menos a una
	 */
	boolean enviar(UUID persona, String evento, Object datos) {
		boolean llego = false;
		for (SseEmitter emisor : conexiones.getOrDefault(persona, Set.of())) {
			llego |= intentar(persona, emisor, SseEmitter.event().name(evento).data(datos, MediaType.APPLICATION_JSON));
		}
		return llego;
	}

	/**
	 * Un comentario cada 25 s mantiene viva la conexión a través de los proxies, y descubre las
	 * pestañas que ya se cerraron.
	 */
	@Scheduled(fixedRate = 25_000)
	void mantenerVivas() {
		conexiones.forEach((persona, emisores) -> emisores
			.forEach(emisor -> intentar(persona, emisor, SseEmitter.event().comment("latido"))));
	}

	private boolean intentar(UUID persona, SseEmitter emisor, SseEmitter.SseEventBuilder evento) {
		try {
			emisor.send(evento);
			return true;
		}
		catch (IOException | IllegalStateException conexionCerrada) {
			quitar(persona, emisor);
			return false;
		}
	}

	private void quitar(UUID persona, SseEmitter emisor) {
		conexiones.computeIfPresent(persona, (clave, emisores) -> {
			emisores.remove(emisor);
			return emisores.isEmpty() ? null : emisores;
		});
	}

}
