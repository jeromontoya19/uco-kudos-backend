# `ADR-005` · Contrato entre el front y el back: REST con OpenAPI

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-50`; `CN-12`, `CN-16`

## Contexto

El front y el back viven en repositorios distintos (`CN-12`) y se despliegan por separado. Sin un
contrato explícito, un cambio en un endpoint rompe al otro lado sin que nadie lo note.

## Decisión

- La API es **REST con JSON**.
- Su **contrato OpenAPI se genera desde el código** y se publica con cada versión.
- El front usa ese contrato como fuente de sus tipos y de su cliente HTTP.

## Alternativas

- **GraphQL:** una consulta flexible para el muro, pero es otra pieza que aprender y que asegurar, y el
  muro no necesita esa flexibilidad.
- **REST sin contrato formal:** más rápido al principio, pero el front y el back se desalinean sin que
  se note.

## Consecuencias

**Se gana:** un contrato verificable entre dos repositorios, documentación de la API sin escribirla a
mano, y pruebas que pueden comprobar que la API cumple su contrato.

**Cuesta:** mantener las descripciones del contrato junto al código, y regenerar el cliente del front
cada vez que el contrato cambia.

## Escenarios

No tiene escenario propio. Sostiene `CN-12` y facilita la prueba de punta a punta de `P-27`.

## Observaciones

—
