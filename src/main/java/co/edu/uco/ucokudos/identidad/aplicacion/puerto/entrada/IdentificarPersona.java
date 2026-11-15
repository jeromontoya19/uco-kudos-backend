package co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada;

import java.util.Optional;

/**
 * Puerto de entrada: quién es, en UCO Kudos, la cuenta que inició sesión en el IdP. El IdP solo dice
 * quién es (el «sub» del token); el rol lo decide la API (ADR-007).
 */
public interface IdentificarPersona {

	/**
	 * @param sujeto el «sub» del token OIDC
	 * @return la persona activa vinculada a esa cuenta, o vacío si no hay ninguna
	 */
	Optional<PersonaIdentificada> porSujeto(String sujeto);

}
