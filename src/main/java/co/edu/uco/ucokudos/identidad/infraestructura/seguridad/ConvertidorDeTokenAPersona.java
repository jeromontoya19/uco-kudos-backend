package co.edu.uco.ucokudos.identidad.infraestructura.seguridad;

import java.util.List;
import java.util.Objects;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.IdentificarPersona;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.PersonaIdentificada;

/**
 * Convierte un token válido del IdP en la sesión de una persona de UCO Kudos (ADR-007).
 * <p>El IdP solo dice quién es la cuenta (el «sub»); el rol sale de la base. El nombre de la sesión es
 * el identificador de la persona: así los controladores saben quién publica sin mirar el token. Una
 * cuenta del IdP sin persona activa queda sin permisos, y la API le responde 403.
 */
final class ConvertidorDeTokenAPersona implements Converter<Jwt, AbstractAuthenticationToken> {

	/** Lo tiene toda persona activa: es el permiso para usar la API. */
	static final String PERMISO_DE_PERSONA = "PERSONA";

	private static final String PREFIJO_DE_ROL = "ROLE_";

	private final IdentificarPersona identificar;

	ConvertidorDeTokenAPersona(IdentificarPersona identificar) {
		this.identificar = Objects.requireNonNull(identificar, "identificar");
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt token) {
		return identificar.porSujeto(token.getSubject())
			.map(persona -> new JwtAuthenticationToken(token, permisosDe(persona), persona.id().toString()))
			.orElseGet(() -> new JwtAuthenticationToken(token, List.of()));
	}

	private static List<GrantedAuthority> permisosDe(PersonaIdentificada persona) {
		return List.of(new SimpleGrantedAuthority(PERMISO_DE_PERSONA),
				new SimpleGrantedAuthority(PREFIJO_DE_ROL + persona.rol()));
	}

}
