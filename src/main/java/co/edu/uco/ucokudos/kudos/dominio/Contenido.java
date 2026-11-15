package co.edu.uco.ucokudos.kudos.dominio;

/**
 * El texto de un Kudo. No tiene límite de longitud (enunciado), pero no puede quedar vacío.
 */
public record Contenido(String texto) {

	public Contenido {
		if (texto == null || texto.isBlank()) {
			throw KudoInvalido.contenidoVacio();
		}
	}

}
