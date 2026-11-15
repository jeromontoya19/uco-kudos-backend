# `ADR-004` · Caché del muro en memoria

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-03`, `D-06`, `D-10`, `D-13`, `D-48`; `DRV-001`, `DRV-002`

## Contexto

El muro es la consulta que más se repite (`DRV-002`), casi siempre desde el celular (`DRV-001`). Debe
responder con p95 ≤ 1 s con 50 usuarios simultáneos y mostrar un Kudo nuevo en ≤ 5 s (`ESC-13`).
Todos ven el mismo muro (`D-03`).

## Decisión

- El muro se entrega **en bloques sucesivos**, del más reciente al más antiguo: cada bloque empieza
  donde terminó el anterior, sin números de página (`D-13`).
- Los bloques se sirven desde **una caché en la memoria de la API**. Como todos ven el mismo muro, hay
  una sola copia para toda la comunidad.
- **Publicar y vetar invalidan la caché** dentro del mismo caso de uso (`D-10`), así que el cambio se
  ve en la siguiente lectura.
- Las réplicas no viajan con el muro: se cargan al abrir la conversación (`D-13`).

## Alternativas

- **Una caché compartida gestionada** (en Azure, Azure Cache for Redis): sirve para varias
  instancias, pero es otra pieza que operar y pagar con el crédito (`CN-02`, `CN-03`).
- **Sin caché, solo índices:** lo más simple, pero es difícil sostener `ESC-13` en una sola instancia
  pequeña.

## Consecuencias

**Se gana:** lectura rápida sin costo y sin piezas extra, y con una sola instancia el cambio se ve casi
de inmediato.

**Cuesta:**
- La caché vive y muere con el proceso: tras un reinicio, la primera lectura va a la base.
- Con más de una instancia, cada una tendría su propia copia y podrían mostrar muros distintos. Por
  eso pasar a varias instancias exige primero una caché compartida (`ADR-011`).

## Escenarios

`ESC-13`

## Observaciones

La librería de caché se elige junto con el lenguaje del back (`ADR-015`).
