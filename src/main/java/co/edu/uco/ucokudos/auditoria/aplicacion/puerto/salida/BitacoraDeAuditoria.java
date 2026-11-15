package co.edu.uco.ucokudos.auditoria.aplicacion.puerto.salida;

import co.edu.uco.ucokudos.auditoria.dominio.RegistroDeAuditoria;

/**
 * Puerto de salida: dónde se guardan los registros de auditoría. Solo permite agregar, porque la
 * auditoría nunca se edita ni se borra (ADR-008).
 */
public interface BitacoraDeAuditoria {

	void agregar(RegistroDeAuditoria registro);

}
