# `ADR-011` · Una sola instancia; el escalado es una evolución documentada

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-05`, `D-15`, `D-39`, `D-48`, `D-53`, `D-54`; `CN-03`, `CN-24`; `T-1`, `T-3`

## Contexto

Se diseña para 10.000 usuarios con 800 simultáneos (`D-05`), pero el presupuesto es el crédito de
estudiante, con una meta de 30 USD al mes (`CN-03`, `D-53`), y no es seguro que una sola instancia
pequeña sostenga 800 usuarios a la vez (`T-1`).

## Decisión

- La línea base corre en **una sola instancia** de la API.
- La API **no guarda estado de sesión** entre peticiones. Lo que vive en su memoria —la caché del muro
  y el registro de quién está conectado al canal en vivo— se puede reconstruir.
- Los **800 simultáneos son una meta de diseño**. En la URL pública se mide la línea base con **unos
  50** (`D-39`).
- Pasar a varias instancias es una **evolución documentada**, en este orden, con su servicio de
  Azure (`D-54`):

  | Paso | Qué resuelve | En Azure |
  |---|---|---|
  | 1. Caché compartida (`ADR-004`) | Que todas las instancias muestren el mismo muro | Azure Cache for Redis |
  | 2. Relevo de push entre instancias (`ADR-021`) | Que el push llegue aunque el destinatario esté conectado a otra instancia | Azure Web PubSub, o publicar y suscribirse en el mismo Redis |
  | 3. Varias instancias detrás del balanceador | Repartir la carga | Subir el máximo de réplicas de Azure Container Apps, con reglas de escalado por peticiones (`ADR-022`) |

## Alternativas

- **Bajar la escala de diseño** a lo que aguanta una sola instancia pequeña: las tensiones desaparecen,
  pero los escenarios exigen mucho menos.

## Consecuencias

**Se gana:** cabe en la meta de gasto, y la caché y el canal en vivo funcionan sin coordinarse.

**Cuesta:** la línea base no demuestra los 800 simultáneos (Escalabilidad quedó en el puesto 13), y si
el proceso cae, la plataforma queda sin servicio hasta que se reinicie.

## Escenarios

`ESC-07`, `ESC-13`

## Observaciones

Reduce `T-1` y `T-3`.

`[:W:]` 12-nov-2026: con `D-53` el presupuesto pasó de 0 USD al crédito de Azure for Students, y con
`D-54` la evolución nombra los servicios de Azure de cada paso. La decisión no cambia.
