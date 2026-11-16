package co.edu.uco.ucokudos.avisos.infraestructura.configuracion;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.KudoPorAvisar;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.ProgramarAvisos;
import co.edu.uco.ucokudos.avisos.infraestructura.programacion.AvisosProgramados;

/**
 * Decorador que, además de dejar los avisos pendientes, anuncia que hay avisos nuevos. Así el
 * despachador los envía apenas se publica el Kudo, sin esperar su siguiente vuelta, y el push llega en
 * menos de 2 s (ESC-14). La vuelta periódica sigue haciendo los reintentos.
 */
final class ProgramarAvisosConDespachoInmediato implements ProgramarAvisos {

	private final ProgramarAvisos programacion;

	private final ApplicationEventPublisher eventos;

	ProgramarAvisosConDespachoInmediato(ProgramarAvisos programacion, ApplicationEventPublisher eventos) {
		this.programacion = Objects.requireNonNull(programacion, "programacion");
		this.eventos = Objects.requireNonNull(eventos, "eventos");
	}

	@Override
	public void programar(KudoPorAvisar kudo) {
		programacion.programar(kudo);
		eventos.publishEvent(new AvisosProgramados(kudo.kudo()));
	}

}
