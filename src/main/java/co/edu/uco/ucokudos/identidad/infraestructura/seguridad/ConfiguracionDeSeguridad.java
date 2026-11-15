package co.edu.uco.ucokudos.identidad.infraestructura.seguridad;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.IdentificarPersona;

/**
 * Quién puede llamar a qué (ADR-007, ADR-010):
 * <ul>
 * <li>La salud y la versión son públicas: las usan la plataforma, el monitor y el CD.</li>
 * <li>Todo {@code /api} exige un token válido del IdP de una persona activa. Los permisos por rol
 * van en cada endpoint, con {@code @PreAuthorize}.</li>
 * <li>Lo demás se niega.</li>
 * </ul>
 * Sin sesión en el servidor: cada petición trae su token. Como no hay cookies de sesión, Spring no
 * exige el token CSRF a las peticiones con token Bearer.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
class ConfiguracionDeSeguridad {

	private static final long CACHE_DE_CORS_EN_SEGUNDOS = 3600;

	@Bean
	SecurityFilterChain cadenaDeSeguridad(HttpSecurity http, IdentificarPersona identificar,
			CorsConfigurationSource origenesPermitidos) {
		http.authorizeHttpRequests(peticiones -> peticiones
			.requestMatchers("/actuator/health/**", "/actuator/info", "/error").permitAll()
			.requestMatchers("/api/**").hasAuthority(ConvertidorDeTokenAPersona.PERMISO_DE_PERSONA)
			.anyRequest().denyAll())
			.oauth2ResourceServer(servidor -> servidor
				.jwt(jwt -> jwt.jwtAuthenticationConverter(new ConvertidorDeTokenAPersona(identificar))))
			.sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.cors(cors -> cors.configurationSource(origenesPermitidos));
		return http.build();
	}

	/**
	 * Solo los orígenes de {@code CORS_ORIGENES} (separados por comas) llaman a la API desde el navegador.
	 * Si la variable falta, no entra ningún origen.
	 */
	@Bean
	CorsConfigurationSource origenesPermitidos(@Value("${ucokudos.cors.origenes:}") String[] origenes) {
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOrigins(Arrays.stream(origenes).map(String::trim).filter(origen -> !origen.isEmpty()).toList());
		cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		cors.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
		cors.setExposedHeaders(List.of(HttpHeaders.LOCATION));
		cors.setMaxAge(CACHE_DE_CORS_EN_SEGUNDOS);
		UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
		fuente.registerCorsConfiguration("/api/**", cors);
		return fuente;
	}

}
