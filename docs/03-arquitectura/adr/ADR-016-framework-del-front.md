# `ADR-016` · Framework del front

**Estado:** Aceptada el 14-nov-2026 (`D-58`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-18`, `D-58`; `ADR-005`, `ADR-006`; `DRV-001`; `CN-16`, `CN-20`, `CN-21`
**La confirmó:** el esqueleto del front desplegado (no necesitó spike)

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

## Decisión

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

`[:W:]` 14-nov-2026: **el esqueleto del front quedó desplegado**, que es lo que confirma este ADR. Está
en el repositorio `uco-kudos-frontend`.
- **Stack:** React 19 con Vite 8 y TypeScript.
- **PWA instalable:** se instala con `vite-plugin-pwa`, que genera el manifiesto y el *service
  worker*; en el celular abre sin la barra del navegador.
- **Pruebas:** Vitest y Testing Library, con lint de oxlint.
- **CI y CD en verde:** el CD lo despliega en Static Web Apps.
- **Dónde abrirla:** https://happy-bush-0fa58021e.4.azurestaticapps.net (`SPK-04`, paso 10).

Con eso el autor aceptó este ADR (`D-58`).
