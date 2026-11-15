package co.edu.uco.ucokudos.kudos.dominio;

import java.time.Instant;
import java.util.Objects;

/**
 * Un reconocimiento de una persona a otra, en una categoría. Si existe, cumple sus reglas: no hay
 * forma de construir un Kudo inválido.
 *
 * @param id           identificador del Kudo
 * @param autor        quién lo da
 * @param destinatario quién lo recibe; nunca es el mismo autor (D-12)
 * @param categoria    en qué categoría
 * @param contenido    el texto del reconocimiento
 * @param publicadoEn  cuándo se publicó
 */
public record Kudo(KudoId id, IdPersona autor, IdPersona destinatario, IdCategoria categoria, Contenido contenido,
		Instant publicadoEn) {

	public Kudo {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(autor, "autor");
		Objects.requireNonNull(destinatario, "destinatario");
		Objects.requireNonNull(categoria, "categoria");
		Objects.requireNonNull(contenido, "contenido");
		Objects.requireNonNull(publicadoEn, "publicadoEn");
		if (autor.equals(destinatario)) {
			throw KudoInvalido.autorEsDestinatario();
		}
	}

}
