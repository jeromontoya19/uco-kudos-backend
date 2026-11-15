package co.edu.uco.ucokudos.kudos.infraestructura.web;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * El cuerpo de {@code POST /api/kudos}. No trae autor: el autor es quien inició sesión.
 *
 * @param destinatario a quién se reconoce
 * @param categoria    en qué categoría
 * @param contenido    el texto, sin límite de longitud
 */
record PeticionDePublicacion(@NotNull UUID destinatario, @NotNull UUID categoria, @NotBlank String contenido) {
}
