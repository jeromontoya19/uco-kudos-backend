# `ADR-018` · Producto del proveedor de identidad

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-09`, `D-19`, `D-38`, `D-54`; `ADR-007`, `ADR-012`; `RF-007`; `T-2`, `T-5`
**La confirma:** `SPK-05`

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

## Propuesta

**Un IdP gestionado con capa gratuita en producción —Auth0 o Zitadel Cloud— y Keycloak en Docker
Compose en local, con la misma configuración OIDC.** `SPK-05` elige entre los dos gestionados y
comprueba lo más importante: que pasar de Keycloak al gestionado sea solo cambiar la configuración.

El mismo spike decide el reparto de `RF-007`: qué verifica el IdP (normalmente el correo) y qué
verifica UCO Kudos (el celular y la aprobación jerárquica).

**Por qué no Entra External ID, si la plataforma es Azure** (`D-54`): la API solo valida tokens OIDC
estándar (`ADR-007`), así que el IdP no tiene por qué ser del mismo proveedor que la plataforma, y
Auth0 o Zitadel se configuran más rápido. Si la universidad algún día quisiera el inicio de sesión
institucional, cambiar de IdP es configuración (`D-38`).

## Consecuencias

**Se gana:** cero memoria del IdP en producción y paridad por protocolo en local.

**Cuesta:** depender de los límites de un tercero, y que local y producción usen productos distintos
aunque hablen el mismo estándar.

## Escenarios

`ESC-01`, `ESC-02`

## Observaciones

Cierra `T-2`, y la parte de identidad de `T-5`, si `SPK-05` confirma la propuesta.
