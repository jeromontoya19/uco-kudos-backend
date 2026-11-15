package co.edu.uco.ucokudos.avisos.infraestructura.envivo;

import java.util.UUID;

import org.springframework.stereotype.Component;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.CanalDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.MensajeDeAviso;
import co.edu.uco.ucokudos.avisos.aplicacion.puerto.salida.ResultadoDelEnvio;
import co.edu.uco.ucokudos.avisos.dominio.Canal;

/**
 * El push en tiempo real (paso 4 del flujo obligatorio): si el destinatario tiene la aplicación abierta,
 * le llega el evento {@code kudo-recibido} al instante; si no, se omite (D-04) y queda auditado.
 */
@Component
class PushEnVivo implements CanalDeAviso {

	static final String EVENTO = "kudo-recibido";

	private final ConexionesEnVivo conexiones;

	PushEnVivo(ConexionesEnVivo conexiones) {
		this.conexiones = conexiones;
	}

	@Override
	public Canal canal() {
		return Canal.PUSH;
	}

	@Override
	public ResultadoDelEnvio enviar(MensajeDeAviso mensaje) {
		KudoRecibido evento = new KudoRecibido(mensaje.kudo(), mensaje.autor(), mensaje.categoria());
		return conexiones.enviar(mensaje.destinatario().id(), EVENTO, evento) ? ResultadoDelEnvio.ENTREGADO
				: ResultadoDelEnvio.OMITIDO;
	}

	/**
	 * Lo que recibe el front: con esto muestra «Fulano te dio un Kudo en Empatía».
	 */
	record KudoRecibido(UUID kudo, String autor, String categoria) {
	}

}
