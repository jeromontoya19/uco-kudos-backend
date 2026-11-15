package co.edu.uco.ucokudos.avisos.dominio;

/**
 * En qué va un aviso (ADR-003).
 */
public enum EstadoDelAviso {

	/** Guardado con el Kudo, esperando al despachador. */
	PENDIENTE,
	/** El proveedor del canal lo aceptó. */
	ENTREGADO,
	/** Se agotaron los reintentos. */
	FALLIDO,
	/** No se envió a propósito; por ejemplo, el push con la aplicación cerrada. */
	OMITIDO

}
