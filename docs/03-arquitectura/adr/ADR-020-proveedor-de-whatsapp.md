# `ADR-020` · Proveedor de WhatsApp

**Estado:** Aceptada el 15-nov-2026 (`D-62`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-38`, `D-62`; `ADR-003`; `CN-14`, `CN-18`; `DRV-003`
**La confirmó:** `SPK-01` y la construcción (15-nov-2026)

## Contexto

WhatsApp tiene que funcionar de verdad y con evidencia (`CN-14`). Es el canal más riesgoso: solo deja
iniciar una conversación con plantillas aprobadas, y un número de prueba solo envía a unos pocos
números verificados (`CN-18`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| WhatsApp Cloud API de Meta (número de prueba) | Bajo | Medio | Alto | Medio | Alto | Alto |
| Twilio WhatsApp Sandbox | Bajo | Bajo | Bajo | Medio | Alto | Medio |

**De dónde sale el riesgo:** con Meta, la plantilla tiene que estar aprobada y los destinatarios
verificados. Con Twilio Sandbox, cada destinatario tiene que unirse con un código, y esa unión caduca.

## Decisión

**La WhatsApp Cloud API de Meta con el número de prueba**, porque es el proveedor oficial y escala sin
cambiar de API. El aviso usa la plantilla aprobada `kudo_recibido` (`SPK-01`).

**Plan B:** Twilio WhatsApp Sandbox, detrás del mismo adaptador (`D-38`).

**En local**, un doble que registra los mensajes en vez de enviarlos.

**Cómo quedó construido** (15-nov-2026, back `8cb3bf6`):

| Pieza | Cómo |
|---|---|
| Adaptador | `WhatsAppPorMeta`: un `POST` a `https://graph.facebook.com/v25.0/{id del número}/messages`, con el token como `Bearer` |
| Plantilla | `kudo_recibido`, categoría Utilidad, idioma `es_CO`. Lleva tres variables en orden: `{{1}}` el destinatario, `{{2}}` el autor y `{{3}}` la categoría |
| Destinatario | El celular de la persona, guardado en la base de UCO Kudos; el adaptador le deja solo los dígitos |
| Token | Permanente, de un usuario del sistema del portafolio `UCO Kudos`, con los permisos `whatsapp_business_messaging` y `whatsapp_business_management`. Se guarda como el secreto `whatsapp-token` de Container Apps, que la API lee en la variable `WHATSAPP_TOKEN` |
| Número | El *Phone number ID* del número de prueba, en la variable `WHATSAPP_ID_TELEFONO` |
| Si falla | Una respuesta de error o la falta de token dejan el aviso FALLIDO para ese intento, sin lanzar errores, y el despachador lo reintenta (`ADR-003`) |

## Consecuencias

**Se gana:** el canal oficial, y un plan B que no exige tocar el caso de uso de publicar.

**Cuesta:**
- En la demo solo reciben los números verificados en el número de prueba, que admite hasta 5.
- Cambiar el texto del aviso exige que Meta apruebe otra plantilla.

## Escenarios

`ESC-05`, `ESC-08`

## Observaciones

`[:W:]` Antes decía: «Antes de cada demo, el destinatario le escribe primero al número de prueba para
abrir la ventana de 24 horas». **Ya no hace falta**, porque `kudo_recibido` está aprobada, y una
plantilla aprobada se puede enviar fuera de esa ventana.

`[:W:]` **Resultado del 15-nov-2026 (producción):**
- El aviso de un Kudo llegó al celular verificado **4,6 s** después de publicarse (`ESC-05` pide
  ≤ 1 min).
- El token permanente se probó primero con una llamada HTTP desde Cloud Shell, y después se cambió en
  la API.
- Evidencias: `docs/evidencias/notificaciones/avisos-whatsapp.png`, donde solo se ve el número de
  prueba, y `avisos-auditoria.png`, con `AVISO_WHATSAPP` en EXITO.

**Disparador para un número real:** pasar de la demo a uso real. Hay que verificar el portafolio
comercial, registrar un número propio y aceptar las tarifas de Meta por conversación. Nada de eso
cambia el adaptador.
