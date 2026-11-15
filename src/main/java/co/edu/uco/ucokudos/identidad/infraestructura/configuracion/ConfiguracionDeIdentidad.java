package co.edu.uco.ucokudos.identidad.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.uco.ucokudos.identidad.aplicacion.ConsultaDePersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida.RepositorioDePersonas;

/**
 * Arma los casos de uso del módulo de identidad con sus adaptadores. La misma consulta atiende los dos
 * puertos de entrada: {@code ConsultarPersonas} e {@code IdentificarPersona}.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionDeIdentidad {

	@Bean
	ConsultaDePersonas consultaDePersonas(RepositorioDePersonas personas) {
		return new ConsultaDePersonas(personas);
	}

}
