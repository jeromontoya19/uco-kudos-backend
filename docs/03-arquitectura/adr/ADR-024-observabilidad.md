# `ADR-024` · Observabilidad: registros, trazas y métricas

**Estado:** Aceptada el 15-nov-2026 (`D-64`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-38`, `D-42`, `D-54`, `D-64`; `ADR-008`, `ADR-014`; `DRV-006`; `P-13`, `P-28`
**La confirmó:** la API instrumentada en producción (15-nov-2026): el mapa de la aplicación y la
transacción completa de un Kudo, con la base, Brevo y Meta

## Contexto

`DRV-006` pide monitoreo, métricas, trazas y auditoría. `ESC-12` exige encontrar en ≤ 5 min qué pasó
con un aviso a partir del identificador del Kudo, y `P-28` pide ver los Kudos publicados y los avisos
fallidos por canal. La auditoría ya está decidida (`ADR-008`), y la plataforma es Azure (`D-54`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| OpenTelemetry + Application Insights (Azure Monitor) | Bajo | Medio | Alto | Bajo | Alto | Alto |
| OpenTelemetry + Grafana Cloud | Bajo | Medio | Alto | Medio | Alto | Alto |
| Solo los registros de la plataforma + métricas de la API | Bajo | Bajo | Bajo | Bajo | Medio | Bajo |
| Sentry | Bajo | Bajo | Medio | Medio | Alto | Medio |

**De dónde sale el riesgo:** con Application Insights, pasarse de los 5 GB gratuitos al mes, que se
evita con un límite diario de ingesta. Con Grafana Cloud, los límites de su capa gratuita y una cuenta
más fuera de la suscripción. Con solo los registros de la plataforma no hay trazas, y `ESC-12` queda
en riesgo.

## Decisión

**Instrumentar la API con OpenTelemetry y enviar los datos a Application Insights** (Azure Monitor).
Cambiar de destino es configuración, en el espíritu de `D-38`. **Plan B:** Grafana Cloud con la misma
instrumentación. **Plan C:** solo registros y métricas, con el identificador del Kudo como hilo
conductor.

**En local**, Grafana con Prometheus, Loki y Tempo corre en un perfil aparte de Docker Compose.

**Cómo quedó construido** (15-nov-2026, back `10ebe87`):

| Pieza | Cómo |
|---|---|
| Instrumentación | El **agente Java de Application Insights 3.7.10**, que está hecho sobre OpenTelemetry. Va en la imagen y se descarga de Maven Central con versión fija. Instrumenta solo, **sin tocar el código**: las peticiones HTTP, las consultas a PostgreSQL, las llamadas a Brevo, Meta y Auth0, los registros y las métricas de la JVM |
| Activación | El `ENTRYPOINT` de la imagen carga el agente solo si existe `APPLICATIONINSIGHTS_CONNECTION_STRING`. En local y en las pruebas, la API arranca sin él |
| Destino | El recurso `ucokudos-insights` de Application Insights, en `canadacentral`, sobre el mismo espacio de Log Analytics que ya usaba Container Apps. La cadena de conexión es el secreto `appinsights-conexion` de Container Apps |
| Configuración | `docker/applicationinsights.json`: el rol `ucokudos-api`, el 100 % de muestreo y los registros desde el nivel INFO |
| Seguridad | Trivy revisa la imagen con el agente en la CI, y salió en verde. Las consultas SQL se registran sin sus parámetros |

## Consecuencias

**Se gana:** una sola traza une la publicación, el despachador y cada canal, que es lo que pide
`ESC-12`, en la misma suscripción que la API y sin otra cuenta que administrar.

**Cuesta:**
- Vigilar el volumen de datos que se envía.
- Unos MB más en la imagen y algo de memoria y arranque en la réplica.
- La vuelta del despachador cada 5 s también deja una traza, con una o dos consultas, y eso es ruido
  en las búsquedas.

## Escenarios

`ESC-12`, `ESC-07`

## Observaciones

`[:W:]` 12-nov-2026: la propuesta anterior enviaba los datos a Grafana Cloud. Con `D-54` el destino
pasa a Application Insights, y Grafana Cloud queda como plan B con la misma instrumentación.

`[:W:]` **Diferencias con la propuesta (15-nov-2026):**
- **El agente en vez de la distribución.** La propuesta era la distribución de OpenTelemetry para Azure
  Monitor como dependencia del código. Se usó el agente Java, también hecho sobre OpenTelemetry, porque
  instrumenta todo sin tocar el código y se activa por configuración. Faltaban horas para congelar el
  código.
- **Aún no están:**
  - Las métricas propias: Kudos publicados y avisos por canal y por estado.
  - Los registros en JSON con el identificador del Kudo.
  - El límite diario de ingesta.
  - Grafana en local.

  Mientras tanto, los avisos por canal y por estado se cuentan en la auditoría (`ADR-008`).

`[:W:]` **Evidencia (15-nov-2026, producción),** en `docs/evidencias/observabilidad/`:
- **`observabilidad-mapa.png`:** el mapa de la aplicación, con `ucokudos-api` conectada a PostgreSQL,
  `api.brevo.com`, `graph.facebook.com` y Auth0.
- **`observabilidad-transaccion.webp`:** la transacción completa de un Kudo, bajo un solo identificador
  de operación.
  - El `POST /api/kudos` respondió **201 en 353 ms**, con sus consultas a la base.
  - En la misma traza, el despacho inmediato llamó a Brevo (`POST /v3/smtp/email`, **201**, 1,1 s) y a
    Meta (`POST /v25.0/…/messages`, **200**, 1,2 s), y guardó el estado de cada aviso y su auditoría.
- **`observabilidad-despachador.webp`:** una vuelta periódica del despachador sin avisos pendientes:
  2,5 ms y dos consultas.
