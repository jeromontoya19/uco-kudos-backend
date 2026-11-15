# `ADR-016` · Framework del front

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-18`; `ADR-005`, `ADR-006`; `DRV-001`; `CN-16`, `CN-20`, `CN-21`
**La confirma:** el esqueleto del front desplegado (no necesita spike)

## Contexto

El front es una PWA *mobile first* (`ADR-006`) que consume un contrato OpenAPI (`ADR-005`) y se prueba
de punta a punta en la CI (`CN-16`, `CN-21`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| React + Vite + TypeScript | Bajo | Medio | Alto | Medio | Alto | Alto |
| Vue + Vite + TypeScript | Bajo | Bajo | Alto | Medio | Alto | Alto |
| Angular | Bajo | Alto | Alto | Medio | Alto | Alto |
| Svelte (SvelteKit) | Bajo | Bajo | Medio | Medio | Medio | Medio |

**De dónde sale el riesgo:** en todos depende de cuánto los haya usado el autor. Angular, además, trae
más estructura de la que necesita una sola pantalla de publicar y un muro.

## Propuesta

**React con Vite y TypeScript.** Tiene el ecosistema más amplio para lo que pide `ADR-006`: un
complemento de Vite para la PWA, generación de tipos desde OpenAPI, Vitest para las pruebas unitarias
y Playwright para la de punta a punta. TypeScript es el lenguaje que el autor usó en la PWA de
FlorLogic. **Plan B:** Vue con Vite, que es más simple de aprender.

## Consecuencias

**Se gana:** mucha documentación y ejemplos para PWA y para clientes generados desde OpenAPI.

**Cuesta:** React deja muchas decisiones al desarrollador (estado, rutas), y hay que tomarlas.

## Escenarios

`ESC-16`, `ESC-13`

## Observaciones

—
