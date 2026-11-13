# `ADR-022` · Plataforma de despliegue y monitor de disponibilidad

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-15`, `D-31`, `D-39`, `D-43`, `D-53`, `D-54`; `ADR-011`, `ADR-014`; `CN-03`, `CN-13`, `CN-19`; `T-9`
**La confirma:** `SPK-04`

## Contexto

El back corre como un contenedor en una sola instancia (`ADR-011`), siempre encendida (`ADR-014`). El
front es una PWA estática (`ADR-006`), y los dos necesitan una URL pública (`CN-13`). El presupuesto
es el crédito de Azure for Students, con una meta de 30 USD al mes (`D-53`), y la suscripción solo
deja desplegar en algunas regiones (`CN-19`). Un monitor mide la disponibilidad (`ADR-014`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

**Back (contenedor):**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Azure App Service, Linux, plan B1 | Medio | Bajo | Medio | Medio | Alto | Alto |
| Azure Container Apps, plan de consumo con una réplica mínima | Bajo | Medio | Alto | Medio | Alto | Alto |
| Azure App Service, plan F1 gratuito | Bajo | Bajo | Bajo | Alto | Alto | Medio |
| Render (capa gratuita) | Bajo | Bajo | Medio | Medio | Alto | Medio |
| Koyeb (capa gratuita) | Bajo | Bajo | Medio | Medio | Alto | Medio |
| Google Cloud Run | Bajo | Medio | Alto | Medio | Alto | Alto |

**De dónde sale el riesgo:**
- **App Service B1** cuesta unos 13 USD al mes (1 núcleo y 1,75 GB) y gasta crédito. El plan básico
  no tiene ranuras de despliegue, así que volver atrás es redesplegar la imagen anterior. Además, en
  la suscripción de estudiante la cuota de máquinas B1 resultó ser 0 (`SPK-04`).
- **Container Apps** cobra también el tiempo en reposo de la réplica mínima, y ese costo es más
  difícil de prever. Además suma un entorno y un espacio de Log Analytics que aprender. A cambio,
  trae revisiones (volver atrás moviendo el tráfico) y reglas de escalado.
- **App Service F1** no tiene *Always On* y limita la CPU por día: vuelve el arranque en frío.
- **Render y Koyeb** duermen la instancia gratuita sin uso, y la de Render tiene unos 512 MB.
  **Cloud Run** exige una cuenta de facturación.
- **Todas las de Azure** dependen de las regiones que permita la suscripción (`CN-19`).

**Front (sitio estático):**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Azure Static Web Apps, plan Free | Bajo | Bajo | Alto | Bajo | Alto | Medio |
| Vercel | Bajo | Bajo | Alto | Bajo | Alto | Medio |
| Cloudflare Pages | Bajo | Bajo | Alto | Bajo | Alto | Medio |
| Netlify | Bajo | Bajo | Alto | Bajo | Alto | Medio |

**Monitor:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| UptimeRobot | Bajo | Bajo | No aplica | Bajo | Alto | Medio |
| Better Stack | Bajo | Bajo | No aplica | Bajo | Alto | Medio |
| Pruebas de disponibilidad de Application Insights | Bajo | Bajo | No aplica | Medio | Alto | Medio |

Las pruebas de Application Insights miden desde el mismo proveedor que vigilan: si la suscripción se
deshabilita (`T-9`), el monitor se apaga con ella.

## Propuesta

- **El back en Azure Container Apps**, plan de consumo, en `canadacentral`, junto a la base
  (`ADR-017`): réplicas de 0,5 vCPU y 1 GB, con **mínimo 1 y máximo 1**, es decir, siempre encendida
  y una sola (`ADR-011`). La imagen se descarga de GitHub Container Registry (`ADR-023`), las sondas
  de salud apuntan al endpoint de salud de la API, y el HTTPS va en su dominio
  `*.azurecontainerapps.io`.
- **El front en Azure Static Web Apps**, plan Free.
- **La base y los archivos** en la misma región que la API (`ADR-017`).
- **El monitor en UptimeRobot**, fuera de Azure: sigue midiendo aunque falle la suscripción.
- **Todo en un solo grupo de recursos.** La API, la base y los archivos van en una misma región de
  las que permite la política de la suscripción. El front va en la región de administración que
  admite Static Web Apps, porque se sirve desde su red global. Al terminar la evaluación se borra
  todo de una vez.
- **Alerta de presupuesto** de 30 USD al mes, con avisos al 50 %, 80 % y 100 % (`T-9`). Quedó creada
  el 13-nov-2026 (`SPK-04`).

`[:W:]` **Por qué Container Apps y no App Service B1:** B1 era la primera opción, por su costo fijo y
porque tiene menos piezas que aprender. Pero `SPK-04` encontró que la suscripción de estudiante tiene
cuota 0 de máquinas B1. Container Apps no usa esa cuota, deja la API en la misma región que la base y
trae revisiones para volver atrás. A cambio, cobra el tiempo en reposo de la réplica mínima, y ese
costo se vigila con la alerta de presupuesto.

**Volver atrás (`ESC-15`):** cada despliegue crea una revisión nueva. Volver atrás es desplegar la
etiqueta de la imagen anterior, o activar la revisión anterior, sin reconstruir nada (`ADR-023`).
`SPK-04` mide que tome ≤ 10 min.

**Costo estimado** (se confirma en la calculadora de Azure y en `SPK-04`):

| Pieza | Servicio | USD al mes, aprox. |
|---|---|---:|
| API | Container Apps: 0,5 vCPU y 1 GB, una réplica siempre encendida | ~10 a ~30: cobra en reposo y sube con la actividad |
| Base | PostgreSQL, servidor flexible B1ms (`ADR-017`) | 0 si aplica la capa gratuita de 12 meses; si no, ~15 |
| Archivos | Blob Storage (`ADR-017`) | < 1 |
| Front | Static Web Apps, plan Free | 0 |
| Registros del entorno | Log Analytics, dentro de los 5 GB gratuitos al mes | 0 |
| Observabilidad | Application Insights, dentro de los 5 GB gratuitos al mes (`ADR-024`) | 0 |
| Monitor e imágenes | UptimeRobot y GitHub Container Registry | 0 |
| **Total** | | **~10 a ~45** |

Si la base no queda en la capa gratuita y la API pasa mucho tiempo activa, el total puede pasar la
meta de 30 USD. La alerta de presupuesto lo avisa, y fuera de la evaluación la réplica mínima se baja
a 0.

**Plan B (`D-53`):** el back en Render y el front en Vercel, con el ping de salud manteniendo despierta
la API (`D-31`), y la base y los archivos según el plan B de `ADR-017`. Se activa si `SPK-04` choca
con su criterio de muerte.

`SPK-04` comprueba seis cosas: que la suscripción se activa; que deja crear la API, la base y el
almacén en una misma región; que GitHub Actions puede desplegar; que el back elegido (`ADR-015`)
arranca en la réplica de 1 GB; que el muro da p95 ≤ 1 s con 50 simultáneos (`D-39`), y que se puede
volver a la imagen anterior en ≤ 10 min (`ESC-15`).

## Consecuencias

**Se gana:** sin arranque en frío, con la API junto a la base y con revisiones para volver atrás; la
API, la base, los archivos, el front y la observabilidad en una sola suscripción y un solo grupo de
recursos, y un monitor independiente del proveedor.

**Cuesta:** gastar crédito cada mes, con un costo menos predecible que el de un plan fijo (`T-9`);
aprender Azure dentro del plazo, y depender de las regiones y cuotas de la suscripción de estudiante.

## Escenarios

`ESC-07`, `ESC-13`, `ESC-15`

## Observaciones

`[!]` Los precios, la capa gratuita de 12 meses y las regiones permitidas se confirman en `SPK-04`;
este ADR no los da por ciertos.

`[:W:]` 12-nov-2026: la propuesta anterior era el back en Render, el front en Vercel y el monitor en
UptimeRobot, todo en capas gratuitas (`D-07`). Con `D-53` pasó a ser el plan B.

`[:W:]` 13-nov-2026: `SPK-04` encontró que la suscripción no deja crear PostgreSQL en `westus2` y que
tiene cuota 0 de máquinas B1. El back pasó de App Service B1 a Container Apps, y la región de la API,
la base y los archivos pasó a `canadacentral`.

**Estado de los recursos al 13-nov-2026** (grupo `rg-uco-kudos`):

| Recurso | Servicio | Región | Estado |
|---|---|---|---|
| `ucokudos-api` | Container Apps, en el entorno `env-uco-kudos` | `canadacentral` | Responde en internet con una imagen de prueba; falta la imagen del back |
| `ucokudos-db-4632` | PostgreSQL, servidor flexible B1ms | `canadacentral` | Creado y vacío |
| `ucokudos4632` | Cuenta de almacenamiento con el contenedor privado `adjuntos` | `canadacentral` | Creado y vacío |
| `workspace-rgucokudos…` | Log Analytics, que creó el entorno de Container Apps | `canadacentral` | Recibe los registros del entorno |
| `ucokudos-web` | Static Web Apps, plan Free | `westus2` | Creado vacío, sin conexión con GitHub; muestra la página de bienvenida de Azure |
| Presupuesto | *Cost Management* | — | Creado, con avisos al correo personal |

`ADR-022` sigue en Propuesta hasta que GitHub Actions despliegue en Container Apps y en Static Web Apps
y se pruebe la vuelta atrás (pasos 10 y 11 de `SPK-04`). La identidad del pipeline (paso 9) ya existe:
la aplicación `github-uco-kudos-backend`, con permiso solo sobre `rg-uco-kudos`, y los secretos en
los dos repositorios.
