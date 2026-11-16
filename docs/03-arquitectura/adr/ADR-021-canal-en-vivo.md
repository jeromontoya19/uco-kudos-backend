# `ADR-021` · Mecanismo del canal en vivo

**Estado:** Aceptada el 15-nov-2026 (`D-63`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-04`, `D-15`, `D-63`; `ADR-003`, `ADR-011`; `DRV-003`
**La confirmó:** la construcción (15-nov-2026): el aviso en vivo llegó a la app abierta del destinatario
en producción, lo que responde la pregunta de `SPK-03`

## Contexto

El push solo se envía si el destinatario tiene la aplicación abierta (`D-04`), y tiene que llegar en
≤ 2 s (`ESC-14`). Si no está conectado, el push se omite y se audita, así que hace falta saber quién
está conectado. Hay una sola instancia (`ADR-011`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Server-Sent Events (SSE) | Bajo | Bajo | Medio | Bajo | Alto | Medio |
| WebSocket | Bajo | Medio | Medio | Medio | Medio | Alto |
| Consulta periódica desde el cliente | Bajo | Bajo | Bajo | Bajo | Alto | Bajo |

La consulta periódica no cumple los 2 s sin generar mucho tráfico, y tampoco dice quién está
conectado.

## Decisión

**Server-Sent Events.** El canal solo necesita enviar del servidor al cliente; SSE es HTTP normal, el
navegador se reconecta solo, y saber si alguien está conectado es saber si tiene su flujo abierto.
WebSocket agrega un canal de ida y vuelta que nadie usa.

`[!]` Lo que el spike tiene que resolver: el navegador no deja agregar cabeceras a una conexión SSE,
así que el token debe viajar en una cookie o como un token corto en la URL. **Plan B:** WebSocket.

## Consecuencias

**Se gana:** la opción más simple que cumple `ESC-14`, sin piezas nuevas.

**Cuesta:** cada usuario conectado mantiene una conexión abierta contra la única instancia. Con varias
instancias, el push necesita un relevo entre ellas (`T-3`, evolución de `ADR-011`).

## Escenarios

`ESC-14`

## Observaciones

`[!]` En Container Apps (`ADR-022`), como detrás de cualquier balanceador, una conexión que pasa unos
minutos sin tráfico se corta. El canal envía un comentario de latido cada 30 s, y si aun así se
corta, el navegador se reconecta solo. `SPK-03` lo comprueba contra la URL pública. El ingreso de
Container Apps también admite WebSocket, por si se pasa al plan B.

`[:W:]` **El lado del back quedó construido el 15-nov-2026** (versión `8cb3bf6`). Siguió en Propuesta
hasta que el front recibió el push (ver abajo).
- **El flujo:** `GET /api/avisos/en-vivo` (`text/event-stream`), solo con sesión. Cada pestaña abierta
  de la persona es una conexión, y cada conexión dura 30 min; después, el cliente se vuelve a
  conectar.
- **El evento:** `kudo-recibido` lleva el Kudo, el autor y la categoría. Si la persona no tiene
  ninguna conexión abierta, el push queda OMITIDO y se audita. Así quedó en producción el 15-nov
  (`docs/evidencias/notificaciones/avisos-auditoria.png`).
- **El latido** es cada **25 s**, no cada 30, para quedar con margen frente al corte por inactividad.
  También sirve para descubrir las pestañas que ya se cerraron.
- **El token** (el `[!]` de la propuesta) se resuelve sin cookie y sin token en la URL. El front abre
  el flujo con `fetch` y la cabecera `Authorization`, en lugar de `EventSource`, y la API lo valida
  igual que en cualquier otra petición. A cambio, el front tiene que reconectarse por su cuenta, porque
  `fetch` no lo hace solo.

`[:W:]` Antes decía «Falta (`SPK-03`): que el front abra el flujo y medir el push en ≤ 2 s contra la
URL pública». **Ya está** (15-nov-2026, front `17246be`, back `10ebe87`):
- **El front** abre el flujo con `fetch` y el token en la cabecera. Usa un lector SSE propio que ignora
  los latidos, y reconecta con espera creciente, de 1 s a 30 s, pidiendo un token fresco en cada
  intento.
- **Al llegar `kudo-recibido`**, muestra un aviso flotante («Docente de prueba te dio un Kudo de
  Orientación al Logro») y refresca el muro. Si el usuario dio permiso, también muestra una
  notificación del sistema cuando la pestaña está oculta. No usa Web Push.
- **En producción**, el estudiante tenía la app abierta en el celular, el docente publicó, y el aviso
  apareció en pantalla (`docs/evidencias/notificaciones/avisos-push.jpg`).
- **La latencia:** el back ya despacha apenas se confirma la publicación (`ADR-003`). En la traza de
  Application Insights, el push salió unos **1,5 s** después de la petición
  (`docs/evidencias/observabilidad/observabilidad-transaccion.webp`), dentro de los ≤ 2 s de
  `ESC-14`.

`[!]` **Hallazgo:** los canales de un Kudo salen uno tras otro, y el push salió después del correo,
así que esperó la llamada a Brevo (1,1 s). Si Brevo tarda, el push también. **Evolución:** despachar
primero el push, que no tiene llamada externa, y dejaría el aviso en pantalla en unos 0,5 s. No se hizo
porque el código ya estaba congelado.
