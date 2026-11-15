package co.edu.uco.ucokudos.avisos.infraestructura.configuracion;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.uco.ucokudos.avisos.aplicacion.ProgramacionDeAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.ProgramarAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;

/**
 * Arma los casos de uso del módulo de avisos con sus adaptadores.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionDeAvisos {

	@Bean
	ProgramarAvisos programarAvisos(RepositorioDeAvisos avisos, Clock reloj) {
		return new ProgramacionDeAvisos(avisos, reloj);
	}

}
