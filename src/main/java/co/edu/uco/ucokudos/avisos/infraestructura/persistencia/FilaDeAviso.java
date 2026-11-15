package co.edu.uco.ucokudos.avisos.infraestructura.persistencia;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import co.edu.uco.ucokudos.avisos.dominio.Aviso;
import co.edu.uco.ucokudos.avisos.dominio.Canal;
import co.edu.uco.ucokudos.avisos.dominio.DatosDelKudo;
import co.edu.uco.ucokudos.avisos.dominio.EstadoDelAviso;

/**
 * Una fila de {@code avisos.aviso}, el outbox de ADR-003.
 */
@Table(schema = "avisos", value = "aviso")
record FilaDeAviso(@Id UUID id, UUID kudo, UUID autor, UUID destinatario, String categoria, String canal,
		String estado, int intentos, Instant proximoIntentoEn, Instant creadoEn) {

	static FilaDeAviso desde(Aviso aviso) {
		return new FilaDeAviso(aviso.id(), aviso.kudo().kudo(), aviso.kudo().autor(), aviso.kudo().destinatario(),
				aviso.kudo().categoria(), aviso.canal().name(), aviso.estado().name(), aviso.intentos(),
				aviso.proximoIntentoEn(), aviso.creadoEn());
	}

	Aviso aDominio() {
		return new Aviso(id, new DatosDelKudo(kudo, autor, destinatario, categoria), Canal.valueOf(canal),
				EstadoDelAviso.valueOf(estado), intentos, proximoIntentoEn, creadoEn);
	}

}
