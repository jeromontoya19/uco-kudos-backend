package co.edu.uco.ucokudos;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import co.edu.uco.ucokudos.avisos.aplicacion.puerto.entrada.DespacharAvisos;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.KudoPublicado;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.PublicarKudo;
import co.edu.uco.ucokudos.kudos.aplicacion.puerto.entrada.SolicitudDePublicacion;

/**
 * El flujo obligatorio completo contra PostgreSQL, sin llamar a ningún proveedor real. En las pruebas
 * el correo y WhatsApp no tienen llaves, así que sus avisos fallan y quedan para reintento; y como el
 * destinatario no tiene la aplicación abierta, el push se omite. Todo queda auditado contra el Kudo.
 */
@SpringBootTest
@Import(BaseDeDatosDePrueba.class)
class DespachoDeAvisosIT {

	@Autowired
	private PublicarKudo publicarKudo;

	@Autowired
	private DespacharAvisos despacharAvisos;

	@Autowired
	private JdbcClient jdbc;

	@Test
	void la_auditoria_de_un_kudo_cuenta_su_publicacion_y_cada_aviso() {
		UUID empatia = jdbc.sql("SELECT id FROM kudos.categoria WHERE nombre = 'Empatía'").query(UUID.class).single();
		KudoPublicado publicado = publicarKudo.publicar(new SolicitudDePublicacion(PublicarKudoIT.DOCENTE,
				PublicarKudoIT.ESTUDIANTE, empatia, "Gracias por organizar la monitoría"));

		despacharAvisos.listosParaEnviar().forEach(despacharAvisos::despachar);

		assertThat(jdbc.sql("SELECT canal || ':' || estado || ':' || intentos FROM avisos.aviso WHERE kudo = ? ORDER BY canal")
			.param(publicado.id())
			.query(String.class)
			.list()).containsExactly("CORREO:PENDIENTE:1", "PUSH:OMITIDO:1", "WHATSAPP:PENDIENTE:1");
		assertThat(jdbc.sql("SELECT accion || ':' || resultado FROM auditoria.registro WHERE id_recurso = ? ORDER BY accion")
			.param(publicado.id())
			.query(String.class)
			.list()).containsExactly("AVISO_CORREO:FALLO", "AVISO_PUSH:OMITIDO", "AVISO_WHATSAPP:FALLO",
					"KUDO_PUBLICADO:EXITO");
		assertThat(despacharAvisos.listosParaEnviar()).as("los fallidos esperan su reintento").isEmpty();
	}

}
