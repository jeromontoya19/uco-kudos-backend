package co.edu.uco.ucokudos.auditoria.infraestructura.persistencia;

import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.stereotype.Repository;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.salida.BitacoraDeAuditoria;
import co.edu.uco.ucokudos.auditoria.dominio.RegistroDeAuditoria;

/**
 * La bitácora de auditoría en PostgreSQL. Solo inserta: la base rechaza cualquier cambio o borrado.
 */
@Repository
class BitacoraDeAuditoriaJdbc implements BitacoraDeAuditoria {

	private final JdbcAggregateOperations jdbc;

	BitacoraDeAuditoriaJdbc(JdbcAggregateOperations jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public void agregar(RegistroDeAuditoria registro) {
		jdbc.insert(FilaDeRegistro.desde(registro));
	}

}
