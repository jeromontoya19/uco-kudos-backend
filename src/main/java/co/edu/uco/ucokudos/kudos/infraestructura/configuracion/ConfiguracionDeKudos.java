package co.edu.uco.ucokudos.kudos.infraestructura.configuracion;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.ProgramarAvisos;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.kudos.aplicacion.ConsultaDeCategorias;
import co.edu.uco.ucokudos.kudos.aplicacion.ConsultaDelMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.PublicacionDeKudos;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarCategorias;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.LecturaDelMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeCategorias;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeKudos;

/**
 * Arma los casos de uso del módulo de Kudos. De los otros módulos solo recibe sus puertos de entrada
 * (ADR-002).
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracionDeKudos {

	@Bean
	PublicarKudo publicarKudo(RepositorioDeKudos kudos, RepositorioDeCategorias categorias, ConsultarPersonas personas,
			ProgramarAvisos avisos, RegistrarEnAuditoria auditoria, Clock reloj,
			PlatformTransactionManager transacciones) {
		PublicarKudo publicacion = new PublicacionDeKudos(kudos, categorias, personas, avisos, auditoria, reloj);
		return new PublicarKudoEnTransaccion(publicacion, new TransactionTemplate(transacciones));
	}

	@Bean
	ConsultarMuro consultarMuro(LecturaDelMuro muro, ConsultarPersonas personas) {
		return new ConsultaDelMuro(muro, personas);
	}

	@Bean
	ConsultarCategorias consultarCategorias(RepositorioDeCategorias categorias) {
		return new ConsultaDeCategorias(categorias);
	}

}
