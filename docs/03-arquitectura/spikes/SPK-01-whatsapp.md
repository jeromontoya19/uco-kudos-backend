# `SPK-01` · WhatsApp: ¿llega el aviso desde una llamada HTTP a la API de Meta?

> **Borrador:** preparado con IA a partir de `ADR-020`. Los pasos son una guía; los resultados los llena el autor al correr el spike. Borra esta nota cuando el documento sea tuyo.

> **Fecha:** 13-nov-2026 · **Límite de tiempo:** 1 h · **Estado:** ABIERTO
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
   el envío sale desde código, como lo hará el adaptador de WhatsApp.

### Parte 3 · Plantilla del aviso de Kudo (≤ 15 min; la aprobación puede tardar)

7. En el **Administrador de WhatsApp**, *Plantillas → Crear plantilla*: categoría **Utilidad**,
   nombre `kudo_recibido`, idioma español, y un cuerpo con variables y ejemplos. Por ejemplo:
   «Hola, {{1}}. {{2}} te reconoció en UCO Kudos con un Kudo de {{3}}. Entra a la aplicación para
   verlo.» Enviarla a revisión.
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
| 1 a 4 · App, número de prueba y destinatarios verificados | | `docs/evidencias/notificaciones/spk-01-configuracion.png` (sin el token) |
| 5 · `hello_world` desde la página | | `docs/evidencias/notificaciones/spk-01-hello-world.png` |
| 6 · Envío desde una llamada HTTP | | `docs/evidencias/notificaciones/spk-01-http.png` (sin el token) |
| 7 · Plantilla `kudo_recibido` | ¿Aprobada? ¿Cuánto tardó? | `docs/evidencias/notificaciones/spk-01-plantilla.png` |
| 9 · Token permanente | | — (es un secreto: no hay captura) |

**Respuesta:** …
**Qué decide:** → `ADR-020`
**Lo que queda abierto:** …
