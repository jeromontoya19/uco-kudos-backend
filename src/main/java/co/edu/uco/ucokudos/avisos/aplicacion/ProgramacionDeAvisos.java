package co.edu.uco.ucokudos.avisos.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.KudoPorAvisar;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.ProgramarAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;
import co.edu.uco.ucokudos.avisos.dominio.Canal;
import co.edu.uco.ucokudos.avisos.dominio.DatosDelKudo;

/**
 * Deja un aviso pendiente por cada canal. Un canal nuevo solo necesita su valor en {@link Canal} y su
 * adaptador; este caso de uso no cambia (D-38).
 */
public final class ProgramacionDeAvisos implements ProgramarAvisos {

	private final RepositorioDeAvisos avisos;

	private final Clock reloj;

	public ProgramacionDeAvisos(RepositorioDeAvisos avisos, Clock reloj) {
		this.avisos = Objects.requireNonNull(avisos, "avisos");
		this.reloj = Objects.requireNonNull(reloj, "reloj");
	}

	@Override
	public void programar(KudoPorAvisar kudo) {
		DatosDelKudo datos = new DatosDelKudo(kudo.kudo(), kudo.autor(), kudo.destinatario(), kudo.categoria());
		Instant ahora = reloj.instant();
		List<Aviso> pendientes = Arrays.stream(Canal.values())
			.map(canal -> Aviso.pendiente(datos, canal, ahora))
			.toList();
		avisos.guardarTodos(pendientes);
	}

}
