# `ADR-024` · Observabilidad: registros, trazas y métricas

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-38`, `D-42`, `D-54`; `ADR-008`, `ADR-014`; `DRV-006`; `P-13`, `P-28`
**La confirma:** la instrumentación de la API funcionando en producción

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

## Propuesta

**Instrumentar la API con OpenTelemetry** y enviar los datos a **Application Insights** (Azure
Monitor) con su distribución de OpenTelemetry. Las métricas y las trazas propias se escriben contra la
API estándar, no contra un SDK de Azure, así que cambiar de destino es configuración (en el espíritu
de `D-38`). Se pone un **límite diario de ingesta** para no pasar de los 5 GB gratuitos al mes.
**En local**, Grafana con Prometheus, Loki y Tempo corre en un perfil aparte de Docker Compose.

- Registros en JSON, todos con el identificador de traza de la publicación y el del Kudo.
- Métricas: Kudos publicados, y avisos por canal y por estado (entregado, fallido u omitido).
- `ESC-12` se resuelve buscando el identificador del Kudo en Application Insights, que muestra la
  transacción completa: la publicación, el despachador y cada canal.

**Plan B:** Grafana Cloud con la misma instrumentación. **Plan C:** si ninguno alcanza, solo registros
y métricas, con el identificador del Kudo como hilo conductor.

## Consecuencias

**Se gana:** una sola traza une la publicación, el despachador y cada canal, que es lo que pide
`ESC-12`, en la misma suscripción que la API y sin otra cuenta que administrar.

**Cuesta:** instrumentar la API y vigilar el volumen de datos que se envía.

## Escenarios

`ESC-12`, `ESC-07`

## Observaciones

`[:W:]` 12-nov-2026: la propuesta anterior enviaba los datos a Grafana Cloud. Con `D-54` el destino
pasa a Application Insights, y Grafana Cloud queda como plan B con la misma instrumentación.
