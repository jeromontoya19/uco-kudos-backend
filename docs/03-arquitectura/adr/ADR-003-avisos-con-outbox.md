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

`[:W:]` **Construido y en producción el 15-nov-2026** (back, versión `8cb3bf6`):
- **El outbox** es la tabla `avisos.aviso`, con un aviso por canal y por Kudo (no se repite el par
  Kudo–canal). Se escribe en la misma transacción que el Kudo y su registro de auditoría.
- **El despachador** es una tarea programada de la API. Cada 5 s toma hasta 20 avisos listos y
  despacha cada uno en su propia transacción. Si uno revienta, la tanda sigue con los demás.
- **Los reintentos** esperan 1, 2, 4, 8 y 15 min: son 5 intentos que suman 30 min. Si el quinto
  también falla, el aviso queda FALLIDO.
- **Los canales** son adaptadores del mismo puerto (`CanalDeAviso`), uno por canal: correo por Brevo
  (`ADR-019`), WhatsApp por Meta (`ADR-020`) y push por SSE (`ADR-021`). Si un canal se queda sin
  adaptador, la API no arranca.
- **La auditoría** guarda cada intento como `AVISO_CORREO`, `AVISO_WHATSAPP` o `AVISO_PUSH`. Usa el
  Kudo como recurso y el sistema como actor, y el resultado es EXITO, FALLO u OMITIDO.
- **Sin llaves no se cae nada:** si falta la llave de un proveedor, sus avisos fallan y se reintentan,
  y la API arranca igual.

**Pruebas automatizadas:**
- `AvisoTest`: la política de reintentos.
- `DespachoDeAvisosIT`: el flujo completo contra PostgreSQL. Sin llaves, el correo y WhatsApp quedan
  para reintento, el push se omite y todo queda auditado contra el Kudo.
- `DespachadorProgramadoTest`: un aviso que revienta no detiene la tanda.
- `CanalesHttpTest`: qué le pide cada adaptador al proveedor y cómo traduce sus respuestas.

**En producción (15-nov-2026):** un Kudo del docente al estudiante llegó por correo **4,0 s** después
de publicarse y por WhatsApp **4,6 s** después. El push quedó omitido porque el destinatario no tenía
la aplicación abierta. Evidencias en `docs/evidencias/notificaciones/`: `avisos-auditoria.png`,
`avisos-correo.png` y `avisos-whatsapp.png`.

`[!]` **Pendiente:**
- Los adaptadores todavía no mandan el identificador del aviso para evitar duplicados, así que la
  entrega sigue siendo al menos una vez.
- Las métricas por canal (punto 5) esperan `ADR-024`.
