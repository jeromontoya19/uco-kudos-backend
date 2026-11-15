package co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida;

import co.edu.uco.ucokudos.kudos.dominio.Kudo;

/**
 * Puerto de salida: dónde se guardan los Kudos.
 */
public interface RepositorioDeKudos {

	void guardar(Kudo kudo);

}
