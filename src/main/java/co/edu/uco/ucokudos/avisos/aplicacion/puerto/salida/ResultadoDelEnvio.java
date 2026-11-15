package co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida;

/**
 * Cómo le fue a un canal con un aviso.
 */
public enum ResultadoDelEnvio {

	/** El proveedor del canal lo aceptó. */
	ENTREGADO,
	/** No se envió a propósito; por ejemplo, el push sin la aplicación abierta (D-04). */
	OMITIDO,
	/** No se pudo enviar; se reintentará si quedan intentos. */
	FALLIDO

}
