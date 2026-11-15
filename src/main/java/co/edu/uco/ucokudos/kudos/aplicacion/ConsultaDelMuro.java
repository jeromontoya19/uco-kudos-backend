package co.edu.uco.ucokudos.kudos.aplicacion;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoDelMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.LecturaDelMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.LecturaDelMuro.KudoLeido;

/**
 * Arma una página del muro: lee los Kudos y les pone el nombre de sus personas, pidiéndolos todos de una
 * vez al módulo de identidad.
 */
public final class ConsultaDelMuro implements ConsultarMuro {

	/** Si una persona ya no existe, el muro la muestra así en lugar de fallar. */
	static final String NOMBRE_NO_DISPONIBLE = "Persona no disponible";

	private final LecturaDelMuro muro;

	private final ConsultarPersonas personas;

	public ConsultaDelMuro(LecturaDelMuro muro, ConsultarPersonas personas) {
		this.muro = Objects.requireNonNull(muro, "muro");
		this.personas = Objects.requireNonNull(personas, "personas");
	}

	@Override
	public List<KudoDelMuro> pagina(int pagina, int tamano) {
		int cantidad = Math.clamp(tamano, 1, TAMANO_MAXIMO);
		int desde = Math.max(pagina, 0) * cantidad;
		List<KudoLeido> leidos = muro.leer(desde, cantidad);
		Map<UUID, String> nombres = personas.nombresDe(personasDe(leidos));
		return leidos.stream().map(kudo -> conNombres(kudo, nombres)).toList();
	}

	private static Set<UUID> personasDe(List<KudoLeido> kudos) {
		return kudos.stream().flatMap(kudo -> Stream.of(kudo.autor(), kudo.destinatario())).collect(Collectors.toSet());
	}

	private static KudoDelMuro conNombres(KudoLeido kudo, Map<UUID, String> nombres) {
		return new KudoDelMuro(kudo.id(), persona(kudo.autor(), nombres), persona(kudo.destinatario(), nombres),
				kudo.categoria(), kudo.contenido(), kudo.publicadoEn());
	}

	private static KudoDelMuro.Persona persona(UUID id, Map<UUID, String> nombres) {
		return new KudoDelMuro.Persona(id, nombres.getOrDefault(id, NOMBRE_NO_DISPONIBLE));
	}

}
