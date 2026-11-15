package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

/**
 * Puerto de entrada del flujo obligatorio: publicar un Kudo (RF-001).
 * <p>En una sola transacción guarda el Kudo, deja pendiente un aviso por canal y lo registra en la
 * auditoría (ADR-003, ADR-008). Los avisos los envía después el despachador.
 */
public interface PublicarKudo {

	/**
	 * @throws co.edu.uco.ucokudos.kudos.dominio.KudoInvalido si el Kudo rompe una regla del negocio
	 * @throws co.edu.uco.ucokudos.kudos.aplicacion.PublicacionRechazada si el autor, el destinatario o
	 *         la categoría no están disponibles
	 */
	KudoPublicado publicar(SolicitudDePublicacion solicitud);

}
