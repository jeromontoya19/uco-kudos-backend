# `ADR-019` · Proveedor de correo

**Estado:** Aceptada el 15-nov-2026 (`D-61`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-38`, `D-61`; `ADR-003`; `CN-14`, `CN-19`; `DRV-003`
**La confirmó:** la construcción (15-nov-2026). El primer aviso real respondió la pregunta de `SPK-02`

## Contexto

El correo tiene que funcionar de verdad y con evidencia (`CN-14`), llegar en ≤ 1 min en operación
normal (`ESC-05`) y quedar detrás de un adaptador que permita cambiar de proveedor (`D-38`). Azure
bloquea el correo saliente por el puerto 25, y algunas capas gratuitas bloquean el SMTP (`CN-19`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Resend (API HTTP) | Bajo | Bajo | Medio | Medio | Alto | Medio |
| Brevo (API HTTP) | Bajo | Bajo | Medio | Medio | Alto | Medio |
| SMTP de una cuenta de correo personal | Bajo | Bajo | Bajo | Alto | Medio | Bajo |
| Amazon SES | Medio | Medio | Alto | Medio | Alto | Alto |
| Azure Communication Services Email (dominio administrado por Azure) | Bajo | Medio | Alto | Medio | Alto | Alto |

**De dónde sale el riesgo:** el SMTP puede estar bloqueado en la plataforma de despliegue (`CN-19`).
Los proveedores por API pueden exigir un dominio propio verificado para escribirle a cualquier
destinatario. El dominio administrado por Azure no exige dominio propio, pero tiene límites de envío
bajos y sus correos pueden caer en spam.

## Decisión

**La API HTTP de Brevo, con un remitente verificado y sin dominio propio.** El criterio de `SPK-02`
era cuál de los dos candidatos deja escribirle a cualquier destinatario sin un dominio propio:
- **Resend**, sin dominio verificado, solo le escribe a la dirección de la propia cuenta.
- **Brevo** lo permite desde una dirección personal que se verifica en el panel.

Por eso ganó Brevo. Va por HTTPS, así que no le afecta el bloqueo del puerto 25 (`CN-19`).

**En local**, un contenedor que captura los correos (Mailpit) sigue siendo el doble previsto.

**Plan B:** Azure Communication Services Email con el dominio administrado por Azure, pagado con el
crédito (`D-53`), detrás del mismo adaptador (`D-38`).

**Cómo quedó construido** (15-nov-2026, back `8cb3bf6`):

| Pieza | Cómo |
|---|---|
| Adaptador | `CorreoPorBrevo`: un `POST` a `https://api.brevo.com/v3/smtp/email`, con la llave en la cabecera `api-key`. El mensaje es solo de texto: quién dio el Kudo, la categoría y el enlace al front |
| Llave | Una llave de API de Brevo con permiso solo para correos transaccionales. Se guarda como el secreto `brevo-api-key` de Container Apps, que la API lee en la variable `BREVO_API_KEY`. Nunca está en el repositorio |
| Remitente | La dirección verificada en Brevo, en la variable `CORREO_REMITENTE`, con el nombre «UCO Kudos» |
| Si falla | Una respuesta de error o la falta de llave dejan el aviso FALLIDO para ese intento, sin lanzar errores, y el despachador lo reintenta (`ADR-003`) |

## Consecuencias

**Se gana:**
- El envío va por HTTP y funciona en cualquier plataforma.
- No hace falta comprar ni verificar un dominio.
- Cambiar de proveedor es cambiar el adaptador.

**Cuesta:**
- Un proveedor más, con su límite diario de la capa gratuita.
- El remitente es de un dominio gratuito, sin firma de un dominio propio, así que en otros buzones el
  aviso puede caer en spam.

## Escenarios

`ESC-05`, `ESC-08`

## Observaciones

`[:W:]` **Resultado del 15-nov-2026 (producción):** el aviso de un Kudo llegó a la bandeja de entrada,
no a spam, **4,0 s** después de publicarse (`ESC-05` pide ≤ 1 min). Evidencias:
`docs/evidencias/notificaciones/avisos-correo.png`, sin la dirección del destinatario, y
`avisos-auditoria.png`, con `AVISO_CORREO` en EXITO.

**Disparador para un dominio propio:** que los avisos empiecen a caer en spam, o pasar de la demo a
uso real. Con un dominio propio verificado en Brevo, el correo sale firmado por ese dominio.
