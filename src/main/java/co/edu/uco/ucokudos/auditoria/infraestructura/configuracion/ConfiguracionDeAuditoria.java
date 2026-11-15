package co.edu.uco.ucokudos.auditoria.infraestructura.configuracion;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.edu.uco.ucokudos.auditoria.aplicacion.AnotacionEnAuditoria;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.salida.BitacoraDeAuditoria;

/**
 * Arma los casos de uso del módulo de auditoría con sus adaptadores.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionDeAuditoria {

	@Bean
	RegistrarEnAuditoria registrarEnAuditoria(BitacoraDeAuditoria bitacora, Clock reloj) {
		return new AnotacionEnAuditoria(bitacora, reloj);
	}

}
