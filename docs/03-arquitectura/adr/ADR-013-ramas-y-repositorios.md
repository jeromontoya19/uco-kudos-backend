# `ADR-013` · Dos repositorios y estrategia de ramas con compuertas de CI

**Estado:** Aceptada · **Fecha:** 11-nov-2026
**Deriva de:** `CN-11`, `CN-12`; acordada al crear los repositorios

## Contexto

El enunciado pide un repositorio de Front-End y otro de Back-End (`CN-12`), además de CI, CD, Sonar y
Trivy con evidencia (`CN-11`). Trabaja una sola persona, así que no hay revisión entre pares. En
FlorLogic había seis ramas, y cuatro de ellas (`main`, `documentacion-lista`, `codigo-testing` y
`codigo-listo`) nunca recibieron trabajo.

## Decisión

Dos repositorios: `uco-kudos-backend` y `uco-kudos-frontend`. La documentación vive en `docs/` del
back, y el README del front enlaza a ella.

| Rama | Repositorio | Qué entra | Qué corre ahí | Cómo sale |
|---|---|---|---|---|
| `main` | los dos | Solo pull requests | Despliegue continuo | — |
| `documentacion-desarrollo` | back | `docs/` y `README.md` | Nada | PR a `main` cuando un documento queda completo |
| `codigo-desarrollo` | los dos | Código en curso | CI rápida: compilar, pruebas unitarias y de arquitectura | PR a `codigo-testing` cuando una funcionalidad queda lista |
| `codigo-testing` | los dos | Solo PR desde `codigo-desarrollo` | CI completa: pruebas de integración, cobertura, Sonar y Trivy | PR a `main`, que dispara el despliegue |

- **A `main` solo se entra por pull request.** Cada PR deja la evidencia de la CI.
- Después de cada merge a `main`, **`main` vuelve a las ramas de desarrollo** para que no se alejen.

## Alternativas

- **Las seis ramas de FlorLogic:** `documentacion-lista` y `codigo-listo` eran una revisión entre dos
  personas que aquí no existe.
- **Ramas cortas desde `main` (GitHub Flow):** más simple, pero sin una compuerta aparte para la CI
  completa.
- **Un solo repositorio:** el enunciado pide dos.

## Consecuencias

**Se gana:** cada etapa tiene una compuerta real: CI rápida, CI completa con Sonar y Trivy, y
despliegue.

**Cuesta:** más merges, y hay que traer `main` de vuelta a las ramas de desarrollo con frecuencia.

## Escenarios

`ESC-15`

## Observaciones

En GitHub, `main` se protege con pull request obligatorio y sin *force push*; cuando exista la CI, se
le agrega que los checks pasen. La herramienta de CI/CD se propone en `ADR-023`.
