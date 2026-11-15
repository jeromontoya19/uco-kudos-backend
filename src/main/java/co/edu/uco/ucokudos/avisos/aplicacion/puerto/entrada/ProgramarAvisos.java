package co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada;

/**
 * Puerto de entrada del módulo de avisos: deja pendiente un aviso por cada canal.
 * <p>Corre dentro de la transacción de quien publica el Kudo: o quedan el Kudo y sus avisos, o no
 * queda ninguno (ADR-003). La respuesta no espera a los canales; los envía el despachador.
 */
public interface ProgramarAvisos {

	void programar(KudoPorAvisar kudo);

}
