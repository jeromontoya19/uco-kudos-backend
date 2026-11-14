package co.edu.uco.ucokudos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Comprueba que Flyway deja la base al día al arrancar la API, y que cada módulo tiene su esquema
 * (ADR-002, ADR-017).
 */
@SpringBootTest
@Import(BaseDeDatosDePrueba.class)
class MigracionesIT {

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcClient jdbc;

	@Test
	void al_arrancar_no_quedan_migraciones_pendientes() {
		assertThat(flyway.info().pending()).isEmpty();
	}

	@Test
	void cada_modulo_tiene_su_esquema() {
		List<String> esquemas = jdbc.sql("SELECT schema_name FROM information_schema.schemata")
			.query(String.class)
			.list();

		assertThat(esquemas).containsAll(ReglasDeArquitecturaTest.MODULOS);
	}

}
