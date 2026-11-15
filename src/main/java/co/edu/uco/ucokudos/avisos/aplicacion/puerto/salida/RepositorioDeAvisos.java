package co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida;

import java.util.List;

import co.edu.uco.ucokudos.avisos.dominio.Aviso;

/**
 * Puerto de salida: dónde quedan los avisos pendientes (el outbox).
 */
public interface RepositorioDeAvisos {

	void guardarTodos(List<Aviso> avisos);

}
