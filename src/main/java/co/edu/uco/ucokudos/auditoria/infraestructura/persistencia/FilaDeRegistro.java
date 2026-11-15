package co.edu.uco.ucokudos.auditoria.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import co.edu.uco.ucokudos.auditoria.dominio.RegistroDeAuditoria;

/**
 * Una fila de {@code auditoria.registro}. La tabla solo admite inserciones (ADR-008).
 */
@Table(schema = "auditoria", value = "registro")
record FilaDeRegistro(@Id UUID id, Instant ocurridoEn, String accion, UUID actor, String tipoRecurso, UUID idRecurso,
		String resultado) {

	static FilaDeRegistro desde(RegistroDeAuditoria registro) {
		return new FilaDeRegistro(registro.id(), registro.ocurridoEn(), registro.accion(), registro.actor(),
				registro.tipoRecurso(), registro.idRecurso(), registro.resultado());
	}

}
