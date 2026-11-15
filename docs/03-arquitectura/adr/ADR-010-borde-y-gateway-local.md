# `ADR-010` · Borde de la API: protección en la API y gateway con WAF solo en local

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-51`; `DRV-005`; `CN-03`, `D-15`; `T-8`

## Contexto

La guía de evaluación revisa WAF y API Gateway en la línea base técnica, pero un gateway es otra
instancia, y solo hay presupuesto para una (`CN-03`, `D-15`). En Azure, un WAF gestionado
(Application Gateway o Front Door Premium) cuesta varios cientos de dólares al mes, más que todo el
crédito (`D-53`).

## Decisión

- **En producción**, la API queda directa detrás del HTTPS de la plataforma. La propia API aplica
  **CORS** (con los orígenes permitidos en una variable de entorno), **validación de entrada** y
  **límite de peticiones**.
- **En local**, un **gateway Nginx con ModSecurity** (reglas OWASP CRS) va delante de la API en Docker
  Compose. Se prueba en la CI y se demuestra en la sustentación.
- La diferencia entre los dos entornos se acepta a conciencia (`T-8`).

## Alternativas

- **Sin gateway, ni siquiera en local:** menos trabajo, pero no hay nada que mostrar del WAF.
- **Un WAF gratuito delante:** exige un dominio propio administrado en ese proveedor.
- **Gateway con WAF también en producción:** es otra instancia, contra `CN-03` y `D-15`.
- **El WAF gestionado de Azure:** no cabe en el crédito.

## Consecuencias

**Se gana:** el WAF se demuestra y se prueba sin gastar, y la API se protege por sí misma en
producción.

**Cuesta:** en producción no hay WAF. Los ataques que filtraría llegan a la API, que se defiende con
validación de entrada y límite de peticiones.

## Escenarios

`ESC-01`, de forma indirecta.

## Observaciones

**Disparador para el gateway en producción:** presupuesto para otra instancia. Es el mismo patrón del
antivirus (`ADR-009`).
