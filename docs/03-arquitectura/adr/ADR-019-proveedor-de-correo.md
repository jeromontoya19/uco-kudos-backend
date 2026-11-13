# `ADR-019` · Proveedor de correo

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-38`; `ADR-003`; `CN-14`, `CN-19`; `DRV-003`
**La confirma:** `SPK-02`

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

## Propuesta

**La API HTTP de Resend o de Brevo**, que no depende de los puertos SMTP. `SPK-02` elige entre los dos
según cuál permite escribirle a cualquier destinatario sin un dominio propio. **En local**, un
contenedor que captura los correos (Mailpit) hace de doble.

**Plan B:** Azure Communication Services Email con el dominio administrado por Azure, pagado con el
crédito (`D-53`), si ninguno de los dos deja escribirle a cualquier destinatario sin dominio propio.

## Consecuencias

**Se gana:** envío por HTTP, que funciona en cualquier plataforma, y un doble local para probar sin
enviar correos reales.

**Cuesta:** un proveedor más, con sus límites diarios.

## Escenarios

`ESC-05`, `ESC-08`

## Observaciones

—
