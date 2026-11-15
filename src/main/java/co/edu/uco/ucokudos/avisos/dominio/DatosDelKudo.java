package co.edu.uco.ucokudos.avisos.dominio;

import java.util.Objects;
import java.util.UUID;

/**
 * Lo que un aviso necesita saber del Kudo para escribir su mensaje, copiado al publicarlo. Así el
 * despachador no tiene que consultar el módulo de Kudos.
 *
 * @param kudo         identificador del Kudo
 * @param autor        quién lo publicó
 * @param destinatario a quién avisar
 * @param categoria    nombre de la categoría, tal como estaba al publicar
 */
public record DatosDelKudo(UUID kudo, UUID autor, UUID destinatario, String categoria) {

	public DatosDelKudo {
		Objects.requireNonNull(kudo, "kudo");
		Objects.requireNonNull(autor, "autor");
		Objects.requireNonNull(destinatario, "destinatario");
		Objects.requireNonNull(categoria, "categoria");
	}

}
