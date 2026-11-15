package co.edu.uco.ucokudos.identidad.infraestructura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.uco.ucokudos.identidad.aplicacion.ConsultaDePersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.salida.RepositorioDePersonas;

/**
 * Arma los casos de uso del módulo de identidad con sus adaptadores.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionDeIdentidad {

	@Bean
	ConsultarPersonas consultarPersonas(RepositorioDePersonas personas) {
		return new ConsultaDePersonas(personas);
	}

}
