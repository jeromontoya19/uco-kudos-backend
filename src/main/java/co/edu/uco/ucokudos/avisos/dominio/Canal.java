package co.edu.uco.ucokudos.avisos.dominio;

/**
 * Los canales por los que el destinatario se entera de un Kudo (DRV-003).
 */
public enum Canal {

	CORREO,
	WHATSAPP,
	/** Aviso en vivo dentro de la aplicación, solo si el destinatario la tiene abierta (D-04). */
	PUSH

}
