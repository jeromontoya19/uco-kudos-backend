# `SPK-01` · WhatsApp: ¿llega el aviso desde una llamada HTTP a la API de Meta?

> **Borrador:** preparado con IA a partir de `ADR-020`. Los pasos son una guía; los resultados los llena el autor al correr el spike. Borra esta nota cuando el documento sea tuyo.

> **Fecha:** 13-nov-2026 · **Límite de tiempo:** 1 h · **Estado:** RESPONDIDO el 13-nov-2026. Faltan
> la aprobación de la plantilla y el token permanente
> **Desbloquea:** `ADR-020`, `ABB-11`, `ESC-05`, `CN-14`, `CN-18`

## Pregunta

¿Llega un WhatsApp a un número verificado desde una llamada HTTP a la WhatsApp Cloud API de Meta, con
un número de prueba?

## Por qué ahora

Es el canal más riesgoso del flujo obligatorio (`CN-14`): depende de las reglas de Meta, que solo deja
iniciar conversaciones con plantillas aprobadas, y la aprobación de una plantilla propia puede tardar
(`CN-18`). Si falla, hay que pasar a Twilio con tiempo.

## Caminos a probar

| Camino | Qué se hace | Qué se mide |
|---|---|---|
| A · WhatsApp Cloud API de Meta, número de prueba | Los pasos de abajo | Que llegue `hello_world` al celular verificado, primero desde la página y luego desde una llamada HTTP |
| B · Twilio WhatsApp Sandbox | Solo si A muere | Lo mismo, con el código de unión del sandbox |

## Pasos del camino A

### Parte 1 · App de Meta y número de prueba (≤ 30 min)

1. Entrar a `developers.facebook.com` con una cuenta de Facebook y registrarse como desarrollador si
   lo pide.
2. *Mis apps → Crear app*, con el caso de uso **«Conectarte con los clientes a través de WhatsApp»**
   y el nombre `UCO Kudos`. Si pide un portafolio comercial, crear uno.
3. En *WhatsApp → Configuración de la API*, anotar el **identificador del número de teléfono** y el de
   la **cuenta de WhatsApp Business**, y generar el **token de acceso temporal**, que dura 24 h.
   Guardarlos en un gestor de contraseñas: nunca en el repositorio ni en un chat.
4. En el campo **«Para»**, agregar el celular propio y verificarlo con el código que llega por
   WhatsApp. Se admiten hasta 5 números: agregar también el del destinatario de la demo.

### Parte 2 · Primer envío (≤ 15 min)

5. Pulsar **«Enviar mensaje»** en esa misma página: envía la plantilla `hello_world`, que ya viene
   aprobada. Hacer la captura del mensaje recibido.
6. Repetir el envío desde una terminal, con la llamada HTTP que muestra la página. Esto demuestra que
   el envío sale desde código, como lo hará el adaptador de WhatsApp. Para que el token no quede en el
   historial de la terminal, se lee sin mostrarlo (Cloud Shell o Git Bash). La versión de la Graph
   API (`vNN.0`) se copia de la llamada de ejemplo de la página:

   ```bash
   read -s -p "Token temporal: " WA_TOKEN; echo
   read -p "Identificador del número de teléfono: " WA_PHONE_ID
   read -p "Celular destino con indicativo y sin + (57...): " WA_TO
   read -p "Versión de la Graph API (ej. v23.0): " WA_VER
   curl -s -X POST "https://graph.facebook.com/$WA_VER/$WA_PHONE_ID/messages" \
     -H "Authorization: Bearer $WA_TOKEN" -H "Content-Type: application/json" \
     -d "{\"messaging_product\":\"whatsapp\",\"to\":\"$WA_TO\",\"type\":\"template\",\"template\":{\"name\":\"hello_world\",\"language\":{\"code\":\"en_US\"}}}"
   ```

   Si sale bien, la respuesta trae `"message_status":"accepted"` y un identificador `wamid...`.

### Parte 3 · Plantilla del aviso de Kudo (≤ 15 min; la aprobación puede tardar)

7. En el **Administrador de WhatsApp**, *Plantillas → Crear plantilla*, y enviarla a revisión con
   estos datos:

   | Campo | Valor |
   |---|---|
   | Categoría | **Utilidad**: avisa de algo que pasó en la cuenta del destinatario, no promociona nada |
   | Nombre | `kudo_recibido` |
   | Idioma | Español |
   | Cuerpo | `Hola, {{1}}. {{2}} te reconoció en UCO Kudos con un Kudo de {{3}}. Entra a la aplicación para verlo.` |
   | Ejemplos de las variables | `{{1}}` = Laura · `{{2}}` = Andrés Gómez · `{{3}}` = Trabajo en Equipo |
   | Pie (opcional) | `UCO Kudos` |
   | Botones | Ninguno por ahora; el de «Ver Kudo» necesita la URL final del front |

   El cuerpo empieza y termina con texto fijo, y tiene más texto que variables: así lo exige Meta.
   Si la reclasifica como *Marketing*, se acepta igual para la demo.
8. Mientras se aprueba: si el destinatario le escribe primero al número de prueba, se abre la ventana
   de 24 horas y se pueden enviar mensajes de texto libre (`CN-18`).

### Parte 4 · Token permanente para producción (≤ 15 min)

9. En la *Configuración del negocio → Usuarios del sistema*, crear un usuario del sistema, asignarle la
   app y generar un token **sin vencimiento**, con los permisos `whatsapp_business_messaging` y
   `whatsapp_business_management`. Ese token va como secreto en Container Apps y en GitHub, nunca en el
   repositorio.

## Criterio de muerte

- Si en 60 min Meta no entrega `hello_world` al celular verificado, o no deja crear la app o el
  portafolio → camino B (Twilio Sandbox), detrás del mismo adaptador (`D-38`).
- Si `kudo_recibido` sale rechazada → se corrige y se reenvía. Mientras tanto, la demo usa la ventana
  de 24 horas, con mensajes de texto libre.

---

## Resultado

| Paso | Qué pasó | Evidencia |
|---|---|---|
| 1 a 4 · App, número de prueba y destinatarios verificados | **Sí**, el 13-nov-2026. App `UCO Kudos` con el caso de uso de WhatsApp, conectada al portafolio comercial `UCO Kudos`, creado para esto y sin verificar (para el número de prueba no hace falta verificarlo). Meta asignó el número de prueba **+1 555 140 8598**. El celular del autor quedó verificado como destinatario. En la interfaz actual, la antigua «Configuración de la API» se llama *Configuración básica → Paso 1. Pruébalo* | `docs/evidencias/notificaciones/spk-01-configuracion.png` (sin el token) |
| 5 · `hello_world` desde la página | **Sí**: llegó el 13-nov-2026 a las 3:12 p. m., desde el número de prueba, como cuenta de empresa | [`spk-01-hello-world.png`](../../evidencias/notificaciones/spk-01-hello-world.png) |
| 6 · Envío desde una llamada HTTP | **Sí**, el 13-nov-2026: `POST` a la Graph API **v25.0** desde Git Bash, con el token leído sin mostrarlo. Respuesta: `"message_status":"accepted"` con su `wamid`, y el `hello_world` llegó al celular. Ojo: el curl de ejemplo de la página usa la sintaxis de PowerShell, y en Bash hay que reescribirlo | [`spk-01-http.png`](../../evidencias/notificaciones/spk-01-http.png) (con el número del destinatario tapado) |
| 7 · Plantilla `kudo_recibido` | **En revisión** desde el 13-nov-2026 (tarde). Categoría Utilidad, idioma **Spanish (COL)**, que en la API es `es_CO`, y variables por número. Encabezado «¡Recibiste un Kudo!», el cuerpo de arriba y el pie «UCO Kudos». Los ejemplos solo le sirven a Meta para revisarla: la API manda los valores reales en `components → body → parameters`, en el orden `{{1}}` destinatario, `{{2}}` autor y `{{3}}` categoría. ¿Cuánto tardó la aprobación? … | `docs/evidencias/notificaciones/spk-01-plantilla.png` |
| 9 · Token permanente | | — (es un secreto: no hay captura) |

**Respuesta (13-nov-2026):** **sí.** Un WhatsApp llega al celular verificado desde una llamada HTTP a
la Cloud API de Meta, con el número de prueba. Se cumplió dentro del límite de 1 h, y ningún criterio
de muerte se activó, así que el plan B (Twilio Sandbox) no hace falta.

**Qué decide:** → `ADR-020`

**Lo que queda abierto:**
1. **La aprobación de `kudo_recibido`.** Si la rechazan, se corrige y se reenvía. Mientras tanto se
   usa la ventana de 24 horas.
2. **La parte 4, el token permanente.** El temporal vence a las 24 h y no sirve para la demo. Se hace
   antes del primer despliegue del back con el adaptador de WhatsApp.
3. **La evidencia de la configuración** (`spk-01-configuracion.png`, sin el token ni el celular).
