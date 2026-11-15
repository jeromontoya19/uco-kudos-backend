package co.edu.uco.ucokudos;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lo que comparten todos los módulos. El reloj se inyecta para que las pruebas fijen la hora.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionComun {

	@Bean
	Clock reloj() {
		return Clock.systemUTC();
	}

}
