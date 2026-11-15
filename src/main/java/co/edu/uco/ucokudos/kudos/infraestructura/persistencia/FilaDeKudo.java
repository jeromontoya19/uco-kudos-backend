package co.edu.uco.ucokudos.kudos.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import co.edu.uco.ucokudos.kudos.dominio.Kudo;

/**
 * Una fila de {@code kudos.kudo}. Es el modelo de la base, no el del dominio.
 */
@Table(schema = "kudos", value = "kudo")
record FilaDeKudo(@Id UUID id, UUID autor, UUID destinatario, UUID categoria, String contenido, Instant publicadoEn) {

	static FilaDeKudo desde(Kudo kudo) {
		return new FilaDeKudo(kudo.id().valor(), kudo.autor().valor(), kudo.destinatario().valor(),
				kudo.categoria().valor(), kudo.contenido().texto(), kudo.publicadoEn());
	}

}
