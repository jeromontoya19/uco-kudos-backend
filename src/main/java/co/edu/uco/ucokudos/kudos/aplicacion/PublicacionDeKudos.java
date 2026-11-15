package co.edu.uco.ucokudos.kudos.aplicacion;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.HechoAuditable;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.Resultado;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.KudoPorAvisar;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.ProgramarAvisos;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarPersonas;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeCategorias;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.salida.RepositorioDeKudos;
import co.edu.uco.ucokudos.kudos.dominio.Categoria;
import co.edu.uco.ucokudos.kudos.dominio.Contenido;
import co.edu.uco.ucokudos.kudos.dominio.IdCategoria;
import co.edu.uco.ucokudos.kudos.dominio.IdPersona;
import co.edu.uco.ucokudos.kudos.dominio.Kudo;
import co.edu.uco.ucokudos.kudos.dominio.KudoId;

/**
 * Publica un Kudo: comprueba que el autor, el destinatario y la categoría estén disponibles, lo guarda,
 * deja pendientes sus avisos y lo registra en la auditoría.
 * <p>No abre la transacción: la pone quien lo arma (la infraestructura), para que este caso de uso no
 * dependa de Spring. Habla con los otros módulos solo por sus puertos de entrada (ADR-002).
 */
public final class PublicacionDeKudos implements PublicarKudo {

	static final String ACCION_PUBLICAR = "KUDO_PUBLICADO";

	static final String TIPO_DE_RECURSO = "KUDO";

	private final RepositorioDeKudos kudos;

	private final RepositorioDeCategorias categorias;

	private final ConsultarPersonas personas;

	private final ProgramarAvisos avisos;

	private final RegistrarEnAuditoria auditoria;

	private final Clock reloj;

	public PublicacionDeKudos(RepositorioDeKudos kudos, RepositorioDeCategorias categorias, ConsultarPersonas personas,
			ProgramarAvisos avisos, RegistrarEnAuditoria auditoria, Clock reloj) {
		this.kudos = Objects.requireNonNull(kudos, "kudos");
		this.categorias = Objects.requireNonNull(categorias, "categorias");
		this.personas = Objects.requireNonNull(personas, "personas");
		this.avisos = Objects.requireNonNull(avisos, "avisos");
		this.auditoria = Objects.requireNonNull(auditoria, "auditoria");
		this.reloj = Objects.requireNonNull(reloj, "reloj");
	}

	@Override
	public KudoPublicado publicar(SolicitudDePublicacion solicitud) {
		IdPersona autor = new IdPersona(solicitud.autor());
		IdPersona destinatario = new IdPersona(solicitud.destinatario());
		Contenido contenido = new Contenido(solicitud.contenido());
		exigirCuentaActiva(autor, PublicacionRechazada::autorNoDisponible);
		exigirCuentaActiva(destinatario, PublicacionRechazada::destinatarioNoDisponible);
		Categoria categoria = categoriaActiva(new IdCategoria(solicitud.categoria()));

		Kudo kudo = new Kudo(KudoId.nuevo(), autor, destinatario, categoria.id(), contenido, reloj.instant());
		kudos.guardar(kudo);
		avisos.programar(new KudoPorAvisar(kudo.id().valor(), autor.valor(), destinatario.valor(), categoria.nombre()));
		auditoria.registrar(new HechoAuditable(ACCION_PUBLICAR, autor.valor(), TIPO_DE_RECURSO, kudo.id().valor(),
				Resultado.EXITO));
		return new KudoPublicado(kudo.id().valor(), kudo.publicadoEn());
	}

	private void exigirCuentaActiva(IdPersona persona, Supplier<PublicacionRechazada> rechazo) {
		personas.buscarActiva(persona.valor()).orElseThrow(rechazo);
	}

	private Categoria categoriaActiva(IdCategoria id) {
		return categorias.buscarPorId(id)
			.filter(Categoria::activa)
			.orElseThrow(PublicacionRechazada::categoriaNoDisponible);
	}

}
