# `ADR-001` · Estilo arquitectónico: monolito modular con Clean Architecture

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-45`; `CN-01`, `CN-02`, `CN-03`, `CN-05`, `CN-15`, `CN-24`; `D-15`, `D-20`, `D-38`, `D-53`

## Contexto

Antes de elegir cualquier tecnología hay que elegir la forma de la solución. La cierran cinco cosas:

- **Una sola persona y 4,5 días** (`CN-01`, `CN-02`), con **un crédito de 100 USD y una sola
  instancia** (`CN-03`, `CN-24`).
- **Clean Architecture visible** (`CN-15`) y **cambios en vivo durante la sustentación** (`CN-05`).
- El flujo obligatorio: guardar el Kudo y avisar por tres canales **sin perder nada**, aunque falle
  uno (`ESC-04`, `ESC-05`, `ESC-08`).
- Una carga **sobre todo de lectura**, desde el celular (`DRV-001`, `DRV-002`, `ESC-13`).
- Poder **cambiar de proveedor** sin tocar las reglas (`D-38`) y **levantar todo en local con un
  comando** (`D-20`).

## Decisión

**Monolito modular.** Una sola API, desplegada como un solo contenedor y organizada en cinco módulos
con frontera explícita: Kudos, avisos, identidad y permisos, auditoría y moderación (`ADR-002`). Los
avisos salen en segundo plano dentro del mismo proceso, con un outbox en la base (`ADR-003`), y el
muro se sirve desde una caché en memoria (`ADR-004`). El cliente es una PWA aparte (`ADR-006`) que
habla con la API por REST, con un contrato OpenAPI (`ADR-005`).

## Alternativas

Se compararon cuatro estilos con los mismos ocho criterios, cada uno atado a una restricción, un
escenario o una decisión. La descripción completa de cada alternativa está en la §2 de
[`ALTERNATIVA_DE_SOLUCION.md`](../ALTERNATIVA_DE_SOLUCION.md).

| Criterio | `ALT-1` Monolito modular | `ALT-2` Microservicios | `ALT-3` Backend como servicio | `ALT-4` Serverless |
|---|:---:|:---:|:---:|:---:|
| Cabe en una persona y en 4,5 días (`CN-01`, `CN-02`) | **Sí** | No | Sí | Dudoso |
| Deja ver Clean Architecture (`CN-15`) | **Sí** | Sí | Débil | Débil |
| Cabe en el crédito con una instancia (`CN-03`, `D-15`) | **Sí** | No | Sí | Sí |
| Ningún Kudo se pierde si falla un canal (`ESC-04`) | **Sí** | Sí | Parcial | Parcial |
| Se cambia de proveedor sin tocar las reglas (`D-38`) | **Sí** | Sí | No | Parcial |
| El muro responde con p95 ≤ 1 s gracias a una caché (`ESC-13`) | **Sí** | Sí | Parcial | No |
| Todo se levanta en local con un comando (`D-20`) | **Sí** | Difícil | Parcial | Difícil |
| Admite cambios en vivo en la sustentación (`CN-05`) | **Sí** | No | Parcial | Parcial |
| **Criterios que cumple por completo** | **8 de 8** | 4 de 8 | 2 de 8 | 1 de 8 |

### Por qué ganó `ALT-1`

Es la **única que cumple los ocho criterios por completo**, y la única que cumple a la vez las tres
restricciones duras de esta entrega: una sola persona, el plazo y un crédito limitado con una sola
instancia. Las
otras tres pierden por razones concretas:

- **`ALT-2` Microservicios** falla justo en esas tres restricciones duras: son varios servicios
  siempre encendidos, un broker y despliegues coordinados, para una persona, en 4,5 días y con un
  crédito que no alcanza para tantas piezas encendidas. Además, los microservicios resuelven sobre todo el problema de varios equipos
  trabajando a la vez, y aquí hay uno solo. Lo que ofrecen —escalar por partes— no se pierde: los
  módulos de `ALT-1` tienen frontera explícita y se pueden separar después.
- **`ALT-3` Backend como servicio** era la más tentadora, porque compra hechas la identidad, la base y
  los archivos. Pierde por tres razones: casi no queda código de dominio con el que mostrar Clean
  Architecture (`CN-15`); las reglas quedan atadas a las políticas y funciones del proveedor, así que
  cambiarlo deja de ser solo configuración (`D-38`), y el outbox con reintentos habría que construirlo
  igual, con funciones sueltas (`ESC-04`).
- **`ALT-4` Serverless** pone en riesgo el muro: cada función tiene su propio arranque en frío y no
  comparte una caché en memoria (`ESC-13`). Además es difícil de levantar en local con un comando
  (`D-20`), y repartir el flujo de publicar en funciones complica la transacción del outbox.

## Consecuencias

**Se gana:**
- Un solo artefacto que construir, desplegar, probar y explicar; cabe en una sola instancia pagada
  con el crédito.
- El Kudo y sus avisos pendientes se guardan en una sola transacción, en una sola base (`ADR-003`).
- Los módulos tienen frontera explícita, así que se pueden separar en servicios si llega el disparador.

**Cuesta:**
- **Escalar es escalar todo junto.** Con una instancia, la caché del muro y el canal en vivo funcionan
  sin coordinarse; con dos o más, necesitan una caché compartida y un relevo de push (`ADR-011`,
  `T-3`).
- **Si el proceso cae, cae todo:** la publicación, la lectura y los avisos a la vez. El monitor de
  `ADR-014` lo detecta.
- **Las fronteras entre módulos se pueden erosionar** si nadie las vigila; por eso `ADR-002` exige
  pruebas de arquitectura.

## Escenarios

`ESC-04`, `ESC-05`, `ESC-08`, `ESC-13`, `ESC-15`

## Observaciones

**Disparador para reconsiderarla:** necesitar más de una instancia, o que haya un segundo equipo. El
primer paso sería separar el módulo de avisos, que ya tiene frontera propia y su outbox.

`[:W:]` 12-nov-2026: `D-53` cambió el presupuesto de 0 USD al crédito de Azure for Students. La
decisión y los veredictos de la tabla no cambian (`D-54`).
