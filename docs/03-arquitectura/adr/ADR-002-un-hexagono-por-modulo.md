# `ADR-002` · Organización interna: un hexágono por módulo

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-49`; `CN-05`, `CN-15`, `CN-25`; `D-38`

## Contexto

`CN-15` exige demostrar Clean Architecture: capas, dependencias, responsabilidades y reglas. `CN-05`
pide poder cambiar una parte en vivo sin entender todo el sistema, y `D-38` exige cambiar de proveedor
o agregar un canal de aviso sin tocar el caso de uso de publicar. Dentro del monolito de `ADR-001`, la
forma de organizar el código decide si todo eso es posible.

## Decisión

La API se parte en cinco módulos: **Kudos**, **avisos**, **identidad y permisos**, **auditoría** y
**moderación**. Cada módulo es un hexágono con tres capas:

| Capa | Qué contiene | De qué depende |
|---|---|---|
| Dominio | Entidades, valores y reglas del negocio | De nada: sin framework y sin base de datos |
| Aplicación | Casos de uso y puertos (interfaces de entrada y de salida) | Solo del dominio |
| Infraestructura | Adaptadores de entrada (controladores REST) y de salida (base de datos, proveedores externos) | De la aplicación y del dominio |

- Las dependencias apuntan **hacia adentro**.
- **Los módulos solo se hablan por sus puertos de entrada**: ninguno usa el interior de otro.
- Unas **pruebas de arquitectura** fallan si alguien rompe cualquiera de esas reglas.

## Alternativas

- **Un solo hexágono para toda la API:** más simple, pero las fronteras entre capacidades no se ven, y
  un cambio en los avisos puede terminar tocando a los Kudos.
- **Carpetas por capas técnicas** (controladores, servicios, repositorios): es lo más conocido, pero no
  separa el dominio del framework y no demuestra `CN-15`.

## Consecuencias

**Se gana:**
- Agregar un canal de aviso es agregar un adaptador en el módulo de avisos (`D-38`, `P-23`).
- El dominio se prueba sin base de datos ni servicios externos (`P-26`), que es donde se exige la
  cobertura del 70 % (`CN-25`).
- Un módulo se puede separar en un servicio si llega el disparador de `ADR-001`.

**Cuesta:** más archivos y más traducciones entre capas (objetos de entrada, de dominio y de
persistencia), y hay que vigilar las fronteras con pruebas.

## Escenarios

No tiene escenario propio: lo exigen `CN-05`, `CN-15` y `CN-25`, y lo preguntan `P-22`, `P-23` y
`P-26` (§7 de los drivers).

## Observaciones

Es el mismo criterio de FlorLogic: un hexágono por módulo, no uno por caso de uso.
