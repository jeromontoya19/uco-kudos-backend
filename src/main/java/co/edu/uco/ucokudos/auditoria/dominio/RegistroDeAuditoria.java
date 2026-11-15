package co.edu.uco.ucokudos.auditoria.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Un hecho auditado: quién hizo qué, sobre qué recurso, cuándo y con qué resultado (ADR-008).
 * <p>Solo guarda identificadores, nunca datos personales, y una vez escrito no cambia.
 *
 * @param id          identificador del registro
 * @param ocurridoEn  cuándo pasó
 * @param accion      qué pasó, como un código (por ejemplo, {@code KUDO_PUBLICADO})
 * @param actor       quién lo hizo
 * @param tipoRecurso sobre qué tipo de recurso (por ejemplo, {@code KUDO})
 * @param idRecurso   sobre qué recurso
 * @param resultado   cómo terminó: {@code EXITO}, {@code FALLO} u {@code OMITIDO}
 */
public record RegistroDeAuditoria(UUID id, Instant ocurridoEn, String accion, UUID actor, String tipoRecurso,
		UUID idRecurso, String resultado) {

	public RegistroDeAuditoria {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(ocurridoEn, "ocurridoEn");
		Objects.requireNonNull(actor, "actor");
		Objects.requireNonNull(idRecurso, "idRecurso");
		exigirTexto(accion, "accion");
		exigirTexto(tipoRecurso, "tipoRecurso");
		exigirTexto(resultado, "resultado");
	}

	private static void exigirTexto(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El registro de auditoría necesita " + campo);
		}
	}

}
