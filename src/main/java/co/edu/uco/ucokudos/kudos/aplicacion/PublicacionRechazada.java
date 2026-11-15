package co.edu.uco.ucokudos.kudos.aplicacion;

/**
 * No se puede publicar porque falta algo que el Kudo necesita: el autor, el destinatario o la
 * categoría no existen o no están activos.
 */
public final class PublicacionRechazada extends RuntimeException {

	/** Qué falta. */
	public enum Motivo {

		AUTOR_NO_DISPONIBLE,
		/** El destinatario necesita una cuenta activa (D-12). */
		DESTINATARIO_NO_DISPONIBLE,
		CATEGORIA_NO_DISPONIBLE

	}

	private final Motivo motivo;

	private PublicacionRechazada(Motivo motivo, String mensaje) {
		super(mensaje);
		this.motivo = motivo;
	}

	static PublicacionRechazada autorNoDisponible() {
		return new PublicacionRechazada(Motivo.AUTOR_NO_DISPONIBLE, "El autor no tiene una cuenta activa");
	}

	static PublicacionRechazada destinatarioNoDisponible() {
		return new PublicacionRechazada(Motivo.DESTINATARIO_NO_DISPONIBLE, "El destinatario no tiene una cuenta activa");
	}

	static PublicacionRechazada categoriaNoDisponible() {
		return new PublicacionRechazada(Motivo.CATEGORIA_NO_DISPONIBLE, "La categoría no existe o no está activa");
	}

	public Motivo motivo() {
		return motivo;
	}

}
