package co.edu.uco.ucokudos.avisos.aplicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.HechoAuditable;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.RegistrarEnAuditoria;
import co.edu.uco.ucokudos.auditoria.aplicacion.puerto.entrada.Resultado;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.CanalDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.MensajeDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.RepositorioDeAvisos;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.ResultadoDelEnvio;
import co.edu.uco.ucokudos.avisos.dominio.Aviso;
import co.edu.uco.ucokudos.avisos.dominio.Canal;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.ConsultarContactos;
import co.edu.uco.ucokudos.identidad.aplicacion.puerto.entrada.Contacto;

/**
 * El despachador del outbox (ADR-003): envía cada aviso pendiente por el adaptador de su canal, guarda
 * su nuevo estado y deja cada intento en la auditoría, con el Kudo como recurso. Así la auditoría de un
 * Kudo cuenta el proceso completo: su publicación y cada aviso (paso 5 del flujo obligatorio).
 */
public final class DespachoDeAvisos implements DespacharAvisos {

	/** Cuántos avisos se toman por tanda. */
	static final int TANDA = 20;

	static final String PREFIJO_DE_ACCION = "AVISO_";

	static final String TIPO_DE_RECURSO = "KUDO";

	/** Si el autor ya no existe, el mensaje lo nombra así. */
	static final String AUTOR_NO_DISPONIBLE = "Alguien de la comunidad UCO";

	private final RepositorioDeAvisos avisos;

	private final Map<Canal, CanalDeAviso> canales;

	private final ConsultarContactos contactos;

	private final RegistrarEnAuditoria auditoria;

	private final Clock reloj;

	/**
	 * @throws IllegalStateException si algún canal no tiene adaptador: mejor fallar al arrancar que
	 *         dejar avisos sin enviar
	 */
	public DespachoDeAvisos(RepositorioDeAvisos avisos, List<CanalDeAviso> canales, ConsultarContactos contactos,
			RegistrarEnAuditoria auditoria, Clock reloj) {
		this.avisos = Objects.requireNonNull(avisos, "avisos");
		this.contactos = Objects.requireNonNull(contactos, "contactos");
		this.auditoria = Objects.requireNonNull(auditoria, "auditoria");
		this.reloj = Objects.requireNonNull(reloj, "reloj");
		this.canales = new EnumMap<>(Canal.class);
		canales.forEach(adaptador -> this.canales.put(adaptador.canal(), adaptador));
		for (Canal canal : Canal.values()) {
			if (!this.canales.containsKey(canal)) {
				throw new IllegalStateException("El canal " + canal + " no tiene adaptador");
			}
		}
	}

	@Override
	public List<UUID> listosParaEnviar() {
		return avisos.listosParaEnviar(reloj.instant(), TANDA);
	}

	@Override
	public void despachar(UUID id) {
		Instant ahora = reloj.instant();
		avisos.buscarPorId(id).filter(aviso -> aviso.estaListo(ahora)).ifPresent(aviso -> intentar(aviso, ahora));
	}

	private void intentar(Aviso aviso, Instant ahora) {
		ResultadoDelEnvio resultado = mensajePara(aviso).map(mensaje -> canales.get(aviso.canal()).enviar(mensaje))
			.orElse(ResultadoDelEnvio.FALLIDO);
		avisos.actualizar(switch (resultado) {
			case ENTREGADO -> aviso.entregado(ahora);
			case OMITIDO -> aviso.omitido(ahora);
			case FALLIDO -> aviso.fallo(ahora);
		});
		auditoria.registrar(new HechoAuditable(PREFIJO_DE_ACCION + aviso.canal().name(), HechoAuditable.SISTEMA,
				TIPO_DE_RECURSO, aviso.kudo().kudo(), comoResultadoAuditado(resultado)));
	}

	private Optional<MensajeDeAviso> mensajePara(Aviso aviso) {
		String autor = contactos.contactoDe(aviso.kudo().autor()).map(Contacto::nombre).orElse(AUTOR_NO_DISPONIBLE);
		return contactos.contactoDe(aviso.kudo().destinatario())
			.map(contacto -> new MensajeDeAviso(aviso.id(), aviso.kudo().kudo(), new MensajeDeAviso.Destinatario(
					contacto.id(), contacto.nombre(), contacto.correo(), contacto.celular()), autor,
					aviso.kudo().categoria()));
	}

	private static Resultado comoResultadoAuditado(ResultadoDelEnvio resultado) {
		return switch (resultado) {
			case ENTREGADO -> Resultado.EXITO;
			case OMITIDO -> Resultado.OMITIDO;
			case FALLIDO -> Resultado.FALLO;
		};
	}

}
