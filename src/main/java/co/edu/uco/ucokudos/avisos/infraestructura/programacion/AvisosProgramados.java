package co.edu.uco.ucokudos.avisos.infraestructura.programacion;

import java.util.UUID;

/**
 * Anuncio de que un Kudo dejó avisos pendientes. El despachador lo atiende apenas se confirma la
 * transacción que los guardó; si la transacción se revierte, el anuncio se descarta.
 *
 * @param kudo el Kudo cuyos avisos quedaron pendientes
 */
public record AvisosProgramados(UUID kudo) {
}
