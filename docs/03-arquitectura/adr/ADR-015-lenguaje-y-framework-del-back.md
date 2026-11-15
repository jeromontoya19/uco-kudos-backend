# `ADR-015` · Lenguaje y framework del back

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-18`; `ADR-002`, `ADR-003`, `ADR-005`, `ADR-007`; `CN-03`, `CN-05`, `CN-15`, `CN-16`
**La confirma:** `SPK-04` (memoria y arranque en la réplica de 1 GB)

## Contexto

`D-18` dice que cada tecnología se elige por mérito. El back tiene que separar el dominio del
framework (`ADR-002`), correr una tarea en segundo plano (`ADR-003`), validar tokens OIDC (`ADR-007`),
generar el contrato OpenAPI (`ADR-005`), servir el canal en vivo (`ADR-021`), exponer métricas y
trazas (`ADR-024`), probarse contra contenedores reales (`CN-16`) y caber en la memoria de una
instancia pequeña: 1 GB en la réplica de Container Apps, y unos 512 MB si hubiera que pasar al plan B
con capas gratuitas (`ADR-022`, `CN-03`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Java 21 + Spring Boot | Bajo | Medio | Alto | Medio | Alto | Alto |
| TypeScript + NestJS | Bajo | Medio | Alto | Medio | Alto | Alto |
| C# + ASP.NET Core | Bajo | Medio | Alto | Alto | Alto | Alto |
| Python + FastAPI | Bajo | Bajo | Medio | Medio | Medio | Medio |

**De dónde sale el riesgo:** Spring Boot consume más memoria: en 1 GB cabe si se limita la memoria
de la JVM a una parte del contenedor (por ejemplo, el 75 %), y en el plan B con capas gratuitas
quedaría justo.
NestJS usa el mismo lenguaje del front, pero el autor tiene menos práctica con él en un back. Con
ASP.NET Core el autor no tiene experiencia. FastAPI es menos estricto con el tipado y con la
separación en capas.

## Propuesta

**Java 21 con Spring Boot.** Trae resuelto lo que piden los ADR aceptados: tareas programadas para el
despachador (`ADR-003`), validación de tokens OIDC (`ADR-007`), generación del contrato OpenAPI
(`ADR-005`), SSE y WebSocket (`ADR-021`), métricas y trazas (`ADR-024`), pruebas de arquitectura
(ArchUnit) y pruebas con contenedores (Testcontainers). Además, el autor ya lo usó en FlorLogic, lo que
baja el riesgo de los cambios en vivo (`CN-05`).

Su punto débil es la memoria: `SPK-04` comprueba si arranca y responde dentro de la réplica de 1 GB.
**Plan B:** TypeScript con NestJS.

## Consecuencias

**Se gana:** casi todo lo que piden los ADR viene incluido, y es una tecnología que el autor ya puede
explicar.

**Cuesta:** más memoria y un arranque más lento que las otras opciones; en el plan B con capas
gratuitas, eso pesaría.

## Escenarios

`ESC-04`, `ESC-13`

## Observaciones

Si `SPK-04` muestra que no cabe en la réplica de 1 GB, se prueba una réplica de 1 vCPU y 2 GB; si
tampoco, se pasa al plan B y este ADR se marca Rechazada.
