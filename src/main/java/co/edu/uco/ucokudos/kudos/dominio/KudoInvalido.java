package co.edu.uco.ucokudos.kudos.dominio;

/**
 * Un Kudo que rompe una regla del negocio y por eso no puede existir.
 */
public final class KudoInvalido extends RuntimeException {

	/** Qué regla rompe. */
	public enum Motivo {

		/** Nadie se da un Kudo a sí mismo (D-12). */
		AUTOR_ES_DESTINATARIO,
		/** El contenido no tiene límite de longitud, pero no puede quedar vacío. */
		CONTENIDO_VACIO

	}

	private final Motivo motivo;

	private KudoInvalido(Motivo motivo, String mensaje) {
		super(mensaje);
		this.motivo = motivo;
	}

	static KudoInvalido autorEsDestinatario() {
		return new KudoInvalido(Motivo.AUTOR_ES_DESTINATARIO, "Nadie puede darse un Kudo a sí mismo");
	}

	static KudoInvalido contenidoVacio() {
		return new KudoInvalido(Motivo.CONTENIDO_VACIO, "El Kudo necesita un contenido");
	}

	public Motivo motivo() {
		return motivo;
	}

}
