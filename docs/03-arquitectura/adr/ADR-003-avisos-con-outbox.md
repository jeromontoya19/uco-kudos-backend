# `ADR-003` · Avisos en segundo plano con outbox y reintentos

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-01`, `D-02`, `D-04`, `D-42`, `D-46`, `D-47`; `DRV-003`; `CN-03`, `D-15`

## Contexto

Publicar un Kudo dispara tres avisos por servicios externos que fallan cada uno por su lado
(`DRV-003`). `D-01` dice que un Kudo perdido deja inútil el sistema, y `D-02`, que el Kudo se publica
aunque falle un canal. Además, hay una sola instancia y el crédito no alcanza para otra pieza siempre
encendida (`CN-03`, `D-15`).

## Decisión

1. **Al publicar**, en una sola transacción se guardan el Kudo y un aviso pendiente por canal (correo,
   WhatsApp y push). Eso es el *outbox*. La respuesta no espera a los canales.
2. **Un despachador en segundo plano**, dentro del mismo proceso de la API, lee los avisos pendientes y
   los envía por el adaptador de cada canal.
3. Si un envío falla, **se reintenta hasta 5 veces en 30 minutos**, con espera creciente. Si se agotan
   los intentos, el aviso queda **fallido**.
4. Si el destinatario no tiene la aplicación abierta, **el push queda omitido** (`D-04`).
5. Cada intento y cada estado final quedan en la auditoría (`ADR-008`) y en las métricas por canal
   (`ADR-024`).

## Alternativas

- **Un broker de mensajes gestionado** (en Azure, Service Bus): otra pieza que operar y pagar con el
  crédito. Además, para no perder avisos entre la base y la cola igual haría falta un outbox.
- **Eventos en memoria:** si el proceso se reinicia con avisos pendientes, se pierden. Choca con `D-01`
  y `ESC-04`.
- **El despachador en un proceso aparte o en funciones serverless:** otra instancia u otra
  plataforma, contra `CN-03` y `D-15`.

## Consecuencias

**Se gana:**
- Ningún Kudo se pierde (`ESC-04`), y publicar no depende de los proveedores (`ESC-08`).
- Cada aviso termina en un estado final: entregado, fallido u omitido (`ESC-05`).
- Agregar un canal es agregar un adaptador (`D-38`).

**Cuesta:**
- La base de datos hace también de cola, y el despachador la consulta cada poco tiempo.
- La entrega es **al menos una vez**: si el proceso cae justo después de enviar y antes de marcar el
  aviso como entregado, el aviso se reenvía. `[!]` Cada adaptador debe evitar el duplicado cuando el
  proveedor lo permita, por ejemplo con el identificador del aviso.

## Escenarios

`ESC-04`, `ESC-05`, `ESC-08`, `ESC-14`

## Observaciones

**Disparador para un broker:** varias instancias de la API, o más avisos de los que el despachador
alcanza a enviar (§3.1 de la alternativa de solución).
