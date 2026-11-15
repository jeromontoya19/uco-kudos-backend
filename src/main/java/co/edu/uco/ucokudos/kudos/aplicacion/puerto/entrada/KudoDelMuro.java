package co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada;

import java.time.Instant;
import java.util.UUID;

/**
 * Un Kudo tal como se muestra en el muro.
 *
 * @param id           identificador del Kudo
 * @param autor        quién lo dio
 * @param destinatario quién lo recibió
 * @param categoria    nombre de la categoría
 * @param contenido    el texto
 * @param publicadoEn  cuándo se publicó
 */
public record KudoDelMuro(UUID id, Persona autor, Persona destinatario, String categoria, String contenido,
		Instant publicadoEn) {

	/**
	 * Una persona del muro: solo su identificador y su nombre (D-16).
	 */
	public record Persona(UUID id, String nombre) {
	}

}
