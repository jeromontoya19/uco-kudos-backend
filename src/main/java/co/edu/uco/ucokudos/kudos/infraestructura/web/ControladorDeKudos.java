package co.edu.uco.ucokudos.kudos.infraestructura.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.CategoriaVisible;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarCategorias;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.ConsultarMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoDelMuro;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;

/**
 * La API de Kudos. Solo traduce HTTP a los puertos de entrada: las reglas están en el dominio y en los
 * casos de uso. El autor sale de la sesión, cuyo nombre es el identificador de la persona.
 */
@RestController
@RequestMapping("/api")
class ControladorDeKudos {

	private final PublicarKudo publicar;

	private final ConsultarMuro muro;

	private final ConsultarCategorias categorias;

	ControladorDeKudos(PublicarKudo publicar, ConsultarMuro muro, ConsultarCategorias categorias) {
		this.publicar = publicar;
		this.muro = muro;
		this.categorias = categorias;
	}

	@PostMapping("/kudos")
	ResponseEntity<KudoPublicado> publicar(@Valid @RequestBody PeticionDePublicacion peticion, Authentication sesion) {
		UUID autor = UUID.fromString(sesion.getName());
		KudoPublicado publicado = publicar.publicar(
				new SolicitudDePublicacion(autor, peticion.destinatario(), peticion.categoria(), peticion.contenido()));
		return ResponseEntity.created(URI.create("/api/kudos/" + publicado.id())).body(publicado);
	}

	@GetMapping("/kudos")
	List<KudoDelMuro> muro(@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "" + ConsultarMuro.TAMANO_POR_DEFECTO) int tamano) {
		return muro.pagina(pagina, tamano);
	}

	@GetMapping("/categorias")
	List<CategoriaVisible> categorias() {
		return categorias.activas();
	}

}
