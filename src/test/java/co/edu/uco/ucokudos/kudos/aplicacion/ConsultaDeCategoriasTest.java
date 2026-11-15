package co.edu.uco.ucokudos.kudos.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.CategoriaVisible;
import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;

class ConsultaDeCategoriasTest {

	@Test
	void solo_ofrece_las_categorias_activas_en_orden_alfabetico() {
		IdCategoria liderazgo = new IdCategoria(UUID.randomUUID());
		IdCategoria empatia = new IdCategoria(UUID.randomUUID());
		CategoriasEnMemoria categorias = new CategoriasEnMemoria().con(new Categoria(liderazgo, "Liderazgo", true))
			.con(new Categoria(new IdCategoria(UUID.randomUUID()), "Archivada", false))
			.con(new Categoria(empatia, "Empatía", true));

		assertThat(new ConsultaDeCategorias(categorias).activas()).containsExactly(
				new CategoriaVisible(empatia.valor(), "Empatía"), new CategoriaVisible(liderazgo.valor(), "Liderazgo"));
	}

}
