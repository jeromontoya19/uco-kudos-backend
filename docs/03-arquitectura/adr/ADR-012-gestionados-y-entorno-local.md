# `ADR-012` · Servicios gestionados en producción y todo el sistema en local con un comando

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-19`, `D-20`, `D-38`, `D-53`, `D-54`; `CN-02`, `CN-03`, `CN-22`, `CN-23`; `T-5`, `T-7`, `T-8`

## Contexto

Una sola persona no puede operar servidores (`CN-02`), y autohospedarlos gastaría el crédito en
memoria e instancias (`CN-03`). Aun así, todo tiene que poder levantarse en local con un comando (`D-20`),
para el manual de puesta a punto y para probar sin depender de servicios externos.

## Decisión

- **En producción**, la base de datos y el almacenamiento de archivos son **servicios gestionados de
  Azure**, y el proveedor de identidad, un **servicio gestionado con capa gratuita** (`D-54`).
- **En local**, todo se levanta con **un solo comando de Docker Compose**: la API, la base, el almacén
  de archivos y el IdP en contenedores, y **dobles** para el correo y WhatsApp.
- Se aceptan dos diferencias a conciencia, que existen solo en local: el antivirus (`T-7`) y el
  gateway con WAF (`T-8`).

## Alternativas

- **Autohospedar todo en contenedores también en producción:** control total, pero consume memoria e
  instancias pagadas con el crédito y empeora `T-2`.
- **Solo en la nube, sin entorno local:** menos trabajo, pero sin paridad entre entornos y sin forma de
  que el docente lo corra en su máquina.

## Consecuencias

**Se gana:** nadie opera servidores en producción, y el manual de puesta a punto se reduce a un
comando.

**Cuesta:** `[!]` **`T-5` sigue abierta.** Solo hay paridad entre entornos si cada servicio gestionado
habla el mismo protocolo que su contenedor local: SQL para la base, la misma API de almacenamiento
para los archivos (con Azure Blob Storage, su emulador Azurite) y OIDC para el IdP. Eso lo deben
confirmar `ADR-017` y `ADR-018`.

## Escenarios

No tiene escenario propio. Sostiene `CN-23` y la prueba de punta a punta sin mensajes reales de
`P-27`.

## Observaciones

`[:W:]` 12-nov-2026: con `D-53` y `D-54`, la base y los archivos pasan a servicios gestionados de
Azure. La decisión de fondo —gestionados en producción y todo en local con un comando— no cambia.
