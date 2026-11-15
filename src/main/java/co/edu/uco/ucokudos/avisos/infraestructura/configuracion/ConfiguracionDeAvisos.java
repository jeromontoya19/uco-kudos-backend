package co.edu.uco.ucokudos.avisos.infraestructura.configuracion;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.avisos.aplicacion.DespachoDeAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.ProgramacionDeAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.ProgramarAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.CanalDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;
import co.edu.uco.ucokudos.avisos.infraestructura.canales.CorreoPorBrevo;
import co.edu.uco.ucokudos.avisos.infraestructura.canales.PropiedadesDeCorreo;
import co.edu.uco.ucokudos.avisos.infraestructura.canales.PropiedadesDeWhatsApp;
import co.edu.uco.ucokudos.avisos.infraestructura.canales.WhatsAppPorMeta;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarContactos;

/**
 * Arma los casos de uso del módulo de avisos y sus canales. Cada llamada a un proveedor tiene tiempos
 * máximos: un proveedor lento no puede trabar el despachador.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties({ PropiedadesDeCorreo.class, PropiedadesDeWhatsApp.class })
class ConfiguracionDeAvisos {

	private static final Duration TIEMPO_PARA_CONECTAR = Duration.ofSeconds(5);

	private static final Duration TIEMPO_PARA_RESPONDER = Duration.ofSeconds(10);

	@Bean
	ProgramarAvisos programarAvisos(RepositorioDeAvisos avisos, Clock reloj) {
		return new ProgramacionDeAvisos(avisos, reloj);
	}

	@Bean
	DespacharAvisos despacharAvisos(RepositorioDeAvisos avisos, List<CanalDeAviso> canales,
			ConsultarContactos contactos, RegistrarEnAuditoria auditoria, Clock reloj,
			PlatformTransactionManager transacciones) {
		DespacharAvisos despacho = new DespachoDeAvisos(avisos, canales, contactos, auditoria, reloj);
		return new DespacharAvisosEnTransaccion(despacho, new TransactionTemplate(transacciones));
	}

	@Bean
	CanalDeAviso correoPorBrevo(PropiedadesDeCorreo propiedades) {
		return new CorreoPorBrevo(clienteDe(CorreoPorBrevo.API), propiedades);
	}

	@Bean
	CanalDeAviso whatsAppPorMeta(PropiedadesDeWhatsApp propiedades) {
		return new WhatsAppPorMeta(clienteDe(WhatsAppPorMeta.API), propiedades);
	}

	private static RestClient clienteDe(String api) {
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder().connectTimeout(TIEMPO_PARA_CONECTAR).build());
		fabrica.setReadTimeout(TIEMPO_PARA_RESPONDER);
		return RestClient.builder().baseUrl(api).requestFactory(fabrica).build();
	}

}
