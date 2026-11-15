# `ADR-007` · Identidad delegada a un proveedor de identidad con tokens OIDC

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-09`, `D-14`, `D-27`, `D-28`, `D-38`; `DRV-005`, `DRV-008`

## Contexto

La seguridad es el atributo de calidad número 1 (§6.1 de los drivers). El enunciado pide registrarse
con correo institucional y celular (`RF-007`), y escribir y mantener un sistema propio de contraseñas
y tokens es justamente el código de seguridad más riesgoso.

## Decisión

- **Un proveedor de identidad (IdP) externo** maneja las cuentas, el inicio de sesión y la emisión de
  **tokens estándar OIDC**.
- **La API solo valida los tokens**, con las llaves públicas del IdP y sin consultarlo en cada
  petición. Qué puede hacer cada quien lo decide la API, según el rol activo (`ABB-03`).
- **El token de acceso es corto**, de unos 15 minutos, y se renueva solo: quien pierde su rol deja de
  actuar en minutos (`D-27`, `ESC-02`).
- **La sesión es larga y renovable**, y caduca tras unos 30 días sin uso (`D-28`).
- **Cambiar de IdP es solo configuración**, porque la API depende del estándar, no del producto
  (`D-38`).
- El primer administrador se crea con datos semilla al desplegar (`D-14`).

## Alternativas

- **Identidad dentro de la propia API:** menos piezas, pero las contraseñas, los tokens y la
  recuperación de cuentas quedan como código propio.
- **Inicio de sesión institucional:** depende de un sistema al que el proyecto no tiene acceso, y el
  registro con aprobación queda partido entre dos sistemas.

## Consecuencias

**Se gana:** el código de seguridad más delicado lo pone un producto especializado, y la API queda
atada a un estándar, no a un proveedor.

**Cuesta:**
- Una pieza externa más, que tiene que caber en el presupuesto sin una segunda instancia (`T-2`).
- Revocar no es instantáneo: hay hasta 15 minutos de margen.
- `[!]` El registro de `RF-007` se reparte entre el IdP y UCO Kudos. Quién verifica el correo y quién
  el celular se decide con el producto (`ADR-018`).

## Escenarios

`ESC-01`, `ESC-02`

## Observaciones

El producto de IdP se propone en `ADR-018`.
