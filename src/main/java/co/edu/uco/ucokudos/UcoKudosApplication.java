package co.edu.uco.ucokudos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Raíz de composición de la API: aquí Spring arma los adaptadores de cada módulo y los conecta con
 * los puertos que definen sus casos de uso. Ningún módulo crea por su cuenta su infraestructura.
 */
@SpringBootApplication
public class UcoKudosApplication {

	public static void main(String[] args) {
		SpringApplication.run(UcoKudosApplication.class, args);
	}

}
