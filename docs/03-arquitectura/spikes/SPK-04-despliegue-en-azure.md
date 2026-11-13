# `SPK-04` · Despliegue: ¿se puede montar UCO Kudos en Azure for Students?

> **Borrador:** preparado con IA a partir de `ADR-022`. Los pasos son una guía; los resultados los llena el autor al correr el spike. Borra esta nota cuando el documento sea tuyo.

> **Fecha:** 12-nov-2026 · **Límite de tiempo:** 3 h · **Estado:** ABIERTO. El 13-nov-2026 quedaron
> terminadas las partes 1 y 2 y el paso 9 de la parte 3. Faltan los pasos 10 y 11 (despliegue desde
> GitHub y vuelta atrás) y la parte 4, que esperan el esqueleto del back
> **Desbloquea:** `ADR-015`, `ADR-017` (región), `ADR-022`, `ADR-023`, `ESC-07`, `ESC-13`, `ESC-15`, `T-1`, `T-9`

## Pregunta

¿Se activa Azure for Students y deja montar, en una misma región, la API en App Service B1 siempre
encendida, la base en PostgreSQL y el front en Static Web Apps, desplegados desde GitHub Actions?

## Por qué ahora

`ADR-022` pasó la plataforma a Azure (`D-53`, `D-54`), pero nada de eso existe hasta que la
suscripción se active y su política de regiones lo permita (`CN-19`). Si falla, hay que saberlo hoy,
antes de escribir código, para pasar al plan B sin perder un día.

## Caminos a probar

| Camino | Qué se hace | Qué se mide |
|---|---|---|
| A · Azure: App Service B1, PostgreSQL servidor flexible, Blob Storage y Static Web Apps | Los pasos de abajo | Que cada paso termine sin error de política, y el tiempo total |
| B · Render y Vercel (plan B de `D-53`) | Solo si A muere | Arranque del back en la capa gratuita y que el ping la mantenga despierta |

## Pasos del camino A

### Parte 1 · Suscripción (≤ 30 min)

1. **Activar Azure for Students** en `azure.microsoft.com/free/students`, verificando con el correo
   de la UCO. Conviene iniciar sesión con una cuenta Microsoft personal y usar el correo de la UCO
   solo para la verificación: si se entra con la cuenta de la UCO, la suscripción puede quedar en el
   directorio de la universidad, donde normalmente no se pueden registrar aplicaciones (lo necesita el
   paso 9).
2. **Revisar las regiones permitidas:** en el portal, *Policy → Assignments → Allowed resource
   deployment regions → Parameters*. Anotar la lista y elegir **una sola** región, la más cercana a
   Colombia. Todo lo demás se crea ahí.
3. **Alerta de presupuesto:** *Cost Management → Budgets*, 30 USD al mes, con avisos al 50 %, 80 % y
   100 %. Si la suscripción no deja crear presupuestos, anotarlo y revisar el saldo del crédito a
   diario durante la semana de evaluación (`T-9`).
4. **Grupo de recursos** `rg-uco-kudos`, en la región elegida.

### Parte 2 · Recursos (≤ 1 h)

5. **PostgreSQL:** Azure Database for PostgreSQL, servidor flexible, *Burstable* B1ms, 32 GB, en la
   misma región. Acceso público solo desde servicios de Azure y desde la IP propia. Anotar si el costo
   estimado aparece cubierto por la capa gratuita de 12 meses.
6. **API:** plan de App Service Linux **B1** y una aplicación web de contenedor con cualquier imagen
   HTTP pública de prueba (o la del esqueleto, si ya está en GHCR). Activar *Always On*. Comprobar que
   `https://<aplicacion>.azurewebsites.net` responde desde el celular.
7. **Archivos:** cuenta de almacenamiento estándar (LRS) con un contenedor privado `adjuntos`.
8. **Front:** Static Web Apps, plan Free, conectada al repositorio del front (sirve un `index.html` de
   prueba).

### Parte 3 · Despliegue desde GitHub (≤ 1 h)

9. **Identidad del pipeline:** registrar una aplicación en Microsoft Entra ID con una **credencial
   federada** para el repositorio `uco-kudos-backend` y la rama `main`, y darle el rol *Colaborador*
   sobre el grupo de recursos.
10. **Workflow de prueba:** `azure/login` por OIDC y cambio de la imagen de la aplicación web. Si el
    paso 9 no se puede por falta de permisos, habilitar las credenciales de publicación básicas de la
    aplicación web, descargar su **perfil de publicación** y guardarlo como secreto del repositorio.
11. **Volver atrás:** redesplegar la imagen anterior por su etiqueta y cronometrarlo. Meta: ≤ 10 min
    (`ESC-15`).

### Parte 4 · Cuando exista el esqueleto del back

12. Desplegar la imagen real y anotar la memoria en uso y el tiempo de arranque (`ADR-015`).
13. Cuando exista el endpoint del muro, medirlo con 50 usuarios simultáneos (por ejemplo, con k6):
    p95 ≤ 1 s (`ESC-13`, `D-39`).
14. Crear el monitor de UptimeRobot sobre el endpoint de salud, cada 5 min (`ESC-07`).

## Criterio de muerte

- Si en 30 min la suscripción no queda activa → camino B.
- Si la política no deja crear App Service en ninguna región permitida → camino B.
- Si deja App Service pero no el servidor flexible de PostgreSQL en la misma región → la base pasa al
  plan B de `ADR-017` (Neon) y la API sigue en Azure.
- Si GitHub Actions no puede desplegar ni con OIDC ni con el perfil de publicación → camino B.
- Si a las 3 h no está terminada la parte 3 → camino B, sin discutirlo más.

---

## Resultado

| Paso | Qué pasó | Evidencia |
|---|---|---|
| 1 · Suscripción activa | **Sí**, el 13-nov-2026: cuenta Microsoft personal, verificada con el correo de la UCO; 100 USD de crédito que vencen el 13-nov-2027. Dos tropiezos: el inicio de sesión de la UCO pide Microsoft Authenticator, que no estaba registrado, así que al portal se entra con la cuenta personal en una ventana privada; y justo después de verificar, *Suscripciones* mostraba «0 de 0», pero la oferta ya aparecía en *Education* | `docs/evidencias/despliegue/spk-04-suscripcion.png` (la página *Education*, con el correo recortado) |
| 2 · Regiones permitidas y región elegida | La política «Allowed resource deployment regions» permite cinco regiones: `brazilsouth`, `westus2`, `canadacentral`, `francecentral` y `spaincentral` (se consultó con `az policy assignment list` en Cloud Shell). Primero se eligió `westus2`: según la lista de regiones de Static Web Apps, es la única de las cinco donde también se puede crear el front, y tiene los precios más bajos. `[:W:]` Pero ahí la suscripción no deja crear el servidor flexible de PostgreSQL («The location is restricted from performing this operation»), y sí lo deja en **`canadacentral`**. Quedan **la API, la base y los archivos en `canadacentral`**, juntos, y **el front en `westus2`**: para Static Web Apps esa es solo la región de administración, porque el sitio se sirve desde su red global. `[!]` Ley 1581 (`CN-04`): **Estados Unidos sí** está en la lista de países con nivel adecuado de protección de la SIC (Circular Externa 005 de 2017, modificada por la 008 del mismo año). **Canadá no aparece por nombre** en las fuentes revisadas: falta confirmar en el texto vigente de la Circular Única de la SIC si entra por los países que la Comisión Europea declaró adecuados. Mientras tanto no hay riesgo real, porque la demo usa datos de prueba (`CN-04`) | `docs/evidencias/despliegue/spk-04-regiones.png` (la salida del comando) |
| 3 · Alerta de presupuesto | **Sí**, creada el 13-nov-2026: la meta de 30 USD al mes de `D-53`, con avisos al correo personal del autor | `docs/evidencias/despliegue/spk-04-presupuesto.png` |
| 5 a 8 · Recursos creados | Antes de crear nada hubo que registrar los proveedores de recursos (`MissingSubscriptionRegistration`). **Base:** `ucokudos-db-4632`, B1ms, en `canadacentral`. **Archivos:** cuenta `ucokudos4632` en `canadacentral`, con el contenedor privado `adjuntos` (su comando no dio error). **API:** el plan App Service B1 **no se pudo crear**, porque la suscripción tiene cuota 0 de máquinas B1 («Operation cannot be completed without additional quota»). Se pasó a **Azure Container Apps**, la segunda opción de `ADR-022`: entorno `env-uco-kudos` y aplicación `ucokudos-api` en `canadacentral`, con 0,5 vCPU, 1 GB y una réplica siempre encendida, y una imagen de prueba publicada en `*.canadacentral.azurecontainerapps.io`. La URL pública abre desde el celular (`spk-04-api-celular.png`). El entorno creó solo su espacio de Log Analytics. **Front:** Static Web Apps `ucokudos-web`, plan Free, en `westus2`. Se creó **vacío y sin conectar a GitHub**: por ahora solo muestra la página de bienvenida de Azure. El front se publica desde el workflow de `uco-kudos-frontend`, con el token de despliegue guardado como secreto `AZURE_STATIC_WEB_APPS_API_TOKEN` | `docs/evidencias/despliegue/spk-04-recursos.png` (la tabla de `az resource list`) |
| 9 · Identidad del pipeline | **Sí**, el 13-nov-2026: aplicación `github-uco-kudos-backend` en Microsoft Entra ID, con credencial federada solo para la rama `main` de `uco-kudos-backend` y el rol *Colaborador* solo sobre `rg-uco-kudos`. Se pudo registrar porque la suscripción está en el directorio de la cuenta personal del autor. Secretos creados: `AZURE_CLIENT_ID`, `AZURE_TENANT_ID` y `AZURE_SUBSCRIPTION_ID` en `uco-kudos-backend`, y `AZURE_STATIC_WEB_APPS_API_TOKEN` en `uco-kudos-frontend`. Falta probarla con el primer workflow (paso 10) | Captura de los secretos creados, sin sus valores |
| 10 · Despliegue desde GitHub | | Enlace a la ejecución del workflow |
| 11 · Vuelta atrás | ¿Cuántos minutos? | Enlace a la ejecución del workflow |
| 12 · Memoria y arranque del back | | |
| 13 · p95 del muro con 50 simultáneos | | |

**Respuesta parcial (13-nov-2026, partes 1 y 2):** **sí se puede montar UCO Kudos en Azure for
Students**, con dos cambios frente a la pregunta original:
- **La API va en Container Apps**, no en App Service B1, porque la cuota de B1 es 0.
- **La API, la base y los archivos van en `canadacentral`**, porque `westus2` no deja crear
  PostgreSQL.

El front queda en `westus2`, que para Static Web Apps es solo la región de administración. Ningún
criterio de muerte se ha cumplido, así que el plan B (Render y Vercel) sigue sin usarse.

**Qué decide:** → `ADR-022`, `ADR-023`; `ADR-015` (memoria); `ADR-017` (región)

**Lo que queda abierto:**
1. **Pasos 10 y 11:** el primer despliegue desde GitHub, que también prueba la identidad del paso 9,
   y la vuelta atrás. Hasta que pasen, `ADR-022` y `ADR-023` siguen en Propuesta.
2. **Parte 4:** memoria y arranque del back real y el p95 del muro. Llega con el esqueleto del back.
3. **Costos:** confirmar en *Cost Management* si la base quedó dentro de la capa gratuita de 12 meses
   y cuánto cobra la réplica de Container Apps en reposo.
4. **Ley 1581:** la duda sobre Canadá del paso 2.
5. **Evidencias:** guardar las capturas de los pasos 1, 2, 3 y 5 a 8 (con el correo recortado). La del
   celular ya está.
