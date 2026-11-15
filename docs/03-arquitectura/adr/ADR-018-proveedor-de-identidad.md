# `ADR-018` · Producto del proveedor de identidad

**Estado:** Aceptada el 15-nov-2026 (`D-60`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-09`, `D-19`, `D-38`, `D-54`, `D-60`; `ADR-007`, `ADR-012`; `RF-007`; `T-2`, `T-5`
**La confirmó:** la construcción (15-nov-2026): el front inicia sesión con Auth0 y la API valida sus
tokens en producción. `SPK-05` sigue abierto para la parte local (Keycloak) y el reparto de `RF-007`

## Contexto

`ADR-007` decidió delegar la identidad en un IdP con tokens OIDC, y que cambiar de IdP sea solo
configuración. Falta el producto, que tiene que caber en el presupuesto sin una segunda instancia
(`T-2`) y tener un equivalente local
(`T-5`). También falta decidir quién verifica el correo y el celular en el registro de `RF-007`.

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Keycloak autohospedado | Bajo | Alto | Alto | Alto | Medio | Alto |
| Auth0 | Bajo | Bajo | Alto | Medio | Alto | Medio |
| Zitadel Cloud | Bajo | Bajo | Alto | Medio | Alto | Alto |
| Supabase Auth | Bajo | Bajo | Alto | Medio | Medio | Medio |
| Microsoft Entra External ID | Bajo | Medio | Alto | Alto | Alto | Medio |

**De dónde sale el riesgo:** Keycloak necesita una memoria que la instancia de la API no puede ceder,
y una segunda instancia sale de la meta de gasto (`D-53`). Los gestionados dependen de los límites de
su capa gratuita. Supabase Auth, además, ata la identidad al proveedor de datos. Entra External ID
exige crear y configurar un inquilino externo, que la suscripción de estudiante puede no permitir, y
es una pieza más que aprender en el plazo.

## Decisión

**Auth0 en producción, con su capa gratuita, y Keycloak en Docker Compose en local, con la misma
configuración OIDC.**
- El autor eligió Auth0 entre los dos gestionados el 14-nov-2026 y lo dejó funcionando en producción.
- Zitadel Cloud no se llegó a probar y queda como plan B. Cambiar a él es configuración (`D-38`).

**Por qué no Entra External ID, si la plataforma es Azure** (`D-54`): la API solo valida tokens OIDC
estándar (`ADR-007`), así que el IdP no tiene por qué ser del mismo proveedor que la plataforma, y
Auth0 o Zitadel se configuran más rápido. Si la universidad algún día quisiera el inicio de sesión
institucional, cambiar de IdP es configuración (`D-38`).

**Cómo quedó configurado** (15-nov-2026, back `8cb3bf6`):

| Pieza | Cómo |
|---|---|
| API en Auth0 | Identificador `https://api.ucokudos`, que es la audiencia que exige la API. Firma RS256 y token de acceso de 900 s, es decir, 15 min (`D-27`) |
| Aplicación del front | De una sola página, con código de autorización y PKCE. Retorno, salida y orígenes permitidos: la URL pública del front y `http://localhost:5173` |
| Sesión | Rotación de los *refresh tokens*, que caducan tras 30 días sin uso (`D-28`) |
| Registro | Cerrado (*Disable Sign Ups*): solo existen las cuentas que crea el autor |
| Validación en la API | Firma, emisor (`IDP_EMISOR`) y audiencia (`IDP_AUDIENCIA`), con las llaves públicas de Auth0. La API no guarda ninguna llave ni secreto de Auth0 |
| Vínculo con la persona | El `sub` del token se busca en `identidad.persona.sujeto_idp`, la columna de la migración V6. Si no hay una persona activa con ese `sub`, la API responde 403 |
| Rol | Sale de la base de UCO Kudos en cada petición, no del token (`ADR-007`, `ABB-03`) |
| Sin sesión | La API responde 401. La salud y la versión son públicas, y el resto de la API exige sesión |

## Consecuencias

**Se gana:**
- Cero memoria del IdP en producción y paridad por protocolo en local.
- El rol vive en UCO Kudos: si a una persona le quitan el rol o la desactivan, pierde el acceso en su
  siguiente petición, sin esperar a que venza el token.

**Cuesta:**
- Depender de los límites de un tercero.
- Local y producción usan productos distintos, aunque hablen el mismo estándar.
- Mientras no exista el registro de `RF-007`, vincular una cuenta de Auth0 con su persona es un paso
  manual en la base: el `sub` se escribe en `sujeto_idp`.

## Escenarios

`ESC-01`, `ESC-02`

## Observaciones

Cierra `T-2` (`D-60`): el IdP gestionado no gasta memoria de la instancia ni crédito.

`[:W:]` **Evidencia (15-nov-2026):**
- **En producción:** el front inicia sesión con las cuentas del docente y del estudiante, publica y
  ve el muro. La API responde 401 a una petición sin token.
- **Pruebas automatizadas:** `ApiDeKudosIT` cubre el 401 sin token, el 403 para una cuenta del IdP
  sin persona activa y la persona con su rol en `/api/yo`. `ConsultaDePersonasTest` cubre que una
  persona inactiva no se identifica. En estas pruebas, el validador de tokens se reemplaza por un
  doble, así que no llaman a Auth0.

`[!]` **Sigue abierto (`SPK-05`):**
1. **Keycloak en local, con la misma configuración** (`T-5`): todavía no está en Docker Compose.
2. **El reparto de `RF-007`**, es decir, qué verifica Auth0 y qué verifica UCO Kudos. Hoy el registro
   está cerrado y el autor crea las cuentas.
