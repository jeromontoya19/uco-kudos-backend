package co.edu.uco.ucokudos.kudos.infraestructura.persistencia;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;

/**
 * Una fila de {@code kudos.categoria}.
 */
@Table(schema = "kudos", value = "categoria")
record FilaDeCategoria(@Id UUID id, String nombre, boolean activa) {

	Categoria aDominio() {
		return new Categoria(new IdCategoria(id), nombre, activa);
	}

}
