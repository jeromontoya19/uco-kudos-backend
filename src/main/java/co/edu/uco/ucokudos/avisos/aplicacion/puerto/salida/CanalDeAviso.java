package co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida;

import co.edu.uco.ucokudos.avisos.dominio.Canal;

/**
 * Puerto de salida: un canal por el que se avisa (Strategy). Cada proveedor es un adaptador; agregar un
 * canal es agregar uno más, sin tocar el despachador (D-38).
 * <p>Un adaptador nunca lanza excepciones por fallas del proveedor: las devuelve como
 * {@link ResultadoDelEnvio#FALLIDO}, para que el despachador decida si reintenta.
 */
public interface CanalDeAviso {

	/** Qué canal atiende este adaptador. */
	Canal canal();

	ResultadoDelEnvio enviar(MensajeDeAviso mensaje);

}
