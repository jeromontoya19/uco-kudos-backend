package co.edu.uco.ucokudos;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * La base de las pruebas de integración: un PostgreSQL real en un contenedor, con la misma versión
 * mayor del servidor flexible de Azure (ADR-017). Spring lo arranca con el contexto y le pasa a la API
 * la URL, el usuario y la contraseña, en lugar de las variables de entorno.
 */
@TestConfiguration(proxyBeanMethods = false)
class BaseDeDatosDePrueba {

	private static final DockerImageName IMAGEN = DockerImageName.parse("postgres:16-alpine");

	@Bean
	@ServiceConnection
	PostgreSQLContainer baseDeDatos() {
		// El contenedor no tiene certificado: aquí la conexión va sin cifrar. La URL de producción no
		// trae «sslmode», así que allá rige el verify-full de application.yml.
		return new PostgreSQLContainer(IMAGEN).withUrlParam("sslmode", "disable");
	}

}
