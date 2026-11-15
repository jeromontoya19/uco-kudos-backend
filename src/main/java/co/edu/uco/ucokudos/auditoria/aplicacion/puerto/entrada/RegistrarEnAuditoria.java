package co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada;

/**
 * Puerto de entrada del módulo de auditoría: cualquier módulo registra aquí lo que cambió algo.
 * <p>Corre dentro de la transacción de quien lo llama: si esa transacción se deshace, el registro
 * también, así que la auditoría nunca cuenta algo que no pasó.
 */
public interface RegistrarEnAuditoria {

	void registrar(HechoAuditable hecho);

}
