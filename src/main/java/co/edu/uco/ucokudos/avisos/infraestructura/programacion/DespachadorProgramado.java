package co.edu.uco.ucokudos.avisos.infraestructura.programacion;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;

/**
 * La tarea en segundo plano que mueve el outbox (ADR-003): cada pocos segundos despacha los avisos
 * listos. Corre dentro de la API, en su única instancia (ADR-011). Las pruebas la apagan para despachar
 * a mano.
 */
@Component
@ConditionalOnBooleanProperty(name = "ucokudos.avisos.despachador.activo", matchIfMissing = true)
class DespachadorProgramado {

	private static final Logger REGISTRO = LoggerFactory.getLogger(DespachadorProgramado.class);

	private final DespacharAvisos despachar;

	DespachadorProgramado(DespacharAvisos despachar) {
		this.despachar = despachar;
	}

	@Scheduled(fixedDelayString = "${ucokudos.avisos.despachador.intervalo:PT5S}")
	void despacharListos() {
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
