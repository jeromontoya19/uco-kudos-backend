package co.edu.uco.ucokudos.avisos.infraestructura.programacion;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;

/**
 * La tarea en segundo plano que mueve el outbox (ADR-003). Despacha los avisos apenas se publica un
 * Kudo y, además, cada pocos segundos, para los reintentos. Corre dentro de la API, en su única
 * instancia (ADR-011). Las pruebas la apagan para despachar a mano.
 */
@Component
@ConditionalOnBooleanProperty(name = "ucokudos.avisos.despachador.activo", matchIfMissing = true)
class DespachadorProgramado {

	private static final Logger REGISTRO = LoggerFactory.getLogger(DespachadorProgramado.class);

	private final DespacharAvisos despachar;

	private final TaskExecutor ejecutor;

	/** Una tanda a la vez: la inmediata y la periódica nunca envían el mismo aviso dos veces. */
	private final Lock tanda = new ReentrantLock();

	DespachadorProgramado(DespacharAvisos despachar, @Qualifier("applicationTaskExecutor") TaskExecutor ejecutor) {
		this.despachar = despachar;
		this.ejecutor = ejecutor;
	}

	/**
	 * Apenas se confirma la publicación, despacha en otro hilo: la respuesta al autor no espera a los
	 * canales (ADR-003), y el push no espera la siguiente vuelta (ESC-14).
	 */
	@TransactionalEventListener
	void alProgramarAvisos(AvisosProgramados evento) {
		REGISTRO.debug("Despacho inmediato de los avisos del Kudo {}", evento.kudo());
		ejecutor.execute(this::despacharListos);
	}

	@Scheduled(fixedDelayString = "${ucokudos.avisos.despachador.intervalo:PT5S}")
	void despacharListos() {
		tanda.lock();
		try {
			despacharUnoPorUno();
		}
		finally {
			tanda.unlock();
		}
	}

	private void despacharUnoPorUno() {
		for (UUID aviso : despachar.listosParaEnviar()) {
			try {
				despachar.despachar(aviso);
			}
			catch (RuntimeException error) {
				REGISTRO.error("No se pudo despachar el aviso {}; se intentará en la próxima tanda", aviso, error);
			}
		}
	}

}
