# ADR de UCO Kudos

Cada ADR tiene contexto, decisión (o propuesta), alternativas, consecuencias, escenarios y
observaciones.

**Estados:**
- **Aceptada:** la decidió el autor y cita la decisión de la que sale (`D-nn` en
  [`DECISIONES.md`](../../02-drivers-arquitectonicos/DECISIONES.md)).
- **Propuesta:** tecnología todavía sin decidir. Compara sus candidatos con los seis criterios del
  enunciado (costo, complejidad, escalabilidad, riesgo, mantenibilidad y evolución), recomienda uno y
  dice qué la confirma: un spike o la construcción.
- **Depreciada** y **Rechazada:** se marcan así, sin borrar el archivo.

## Aceptadas: arquitectura

| ID | Título | Estado | Decisión (resumen) |
|---|---|---|---|
| [`ADR-001`](ADR-001-estilo-arquitectonico.md) | Estilo arquitectónico | Aceptada | Monolito modular con Clean Architecture: cumple los 8 criterios, frente a 4, 2 y 1 de microservicios, backend como servicio y serverless |
| [`ADR-002`](ADR-002-un-hexagono-por-modulo.md) | Organización interna | Aceptada | Un hexágono por módulo (Kudos, avisos, identidad y permisos, auditoría, moderación); solo se hablan por puertos |
| [`ADR-003`](ADR-003-avisos-con-outbox.md) | Avisos en segundo plano | Aceptada | Outbox en la base y despachador dentro de la API; 5 reintentos en 30 min; entregado, fallido u omitido |
| [`ADR-004`](ADR-004-cache-del-muro.md) | Caché del muro | Aceptada | Caché en memoria, una copia para todos; el muro va en bloques sucesivos y se invalida al publicar o vetar |
| [`ADR-005`](ADR-005-rest-con-openapi.md) | Contrato front–back | Aceptada | REST con JSON y contrato OpenAPI generado desde el código |
| [`ADR-006`](ADR-006-cliente-pwa.md) | Cliente | Aceptada | PWA instalable; cualquier navegador moderno, con el celular como prioridad |
| [`ADR-007`](ADR-007-identidad-con-idp-oidc.md) | Identidad | Aceptada | IdP externo con tokens OIDC; la API solo los valida; token de 15 min y sesión de 30 días |
| [`ADR-008`](ADR-008-auditoria-de-solo-insercion.md) | Auditoría | Aceptada | Toda acción, solo inserción, solo identificadores; el veto oculta y conserva |
| [`ADR-009`](ADR-009-adjuntos.md) | Adjuntos | Aceptada | Imágenes y PDF, 5 MB, máximo 3; tipo real; miniaturas; antivirus solo en local |
| [`ADR-010`](ADR-010-borde-y-gateway-local.md) | Borde de la API | Aceptada | En producción, CORS, validación y límite de peticiones en la API; gateway con WAF solo en local |
| [`ADR-011`](ADR-011-una-sola-instancia.md) | Instancias | Aceptada | Una sola instancia; 800 simultáneos como meta de diseño y 50 medidos; escalar es evolución |
| [`ADR-012`](ADR-012-gestionados-y-entorno-local.md) | Entornos | Aceptada | Servicios gestionados en producción; todo en local con un comando de Docker Compose |
| [`ADR-013`](ADR-013-ramas-y-repositorios.md) | Repositorios y ramas | Aceptada | Dos repositorios; `main`, `documentacion-desarrollo`, `codigo-desarrollo` y `codigo-testing`, con compuertas de CI |
| [`ADR-014`](ADR-014-disponibilidad-y-recuperacion.md) | Disponibilidad y recuperación | Aceptada | Instancia siempre encendida y monitor externo con meta del 99 %; copia diaria fuera del proveedor, ≤ 24 h de pérdida y ≤ 4 h de recuperación |

## Tecnología: propuestas y las ya aceptadas

| ID | Título | Estado | Propuesta (resumen) | La confirma |
|---|---|---|---|---|
| [`ADR-015`](ADR-015-lenguaje-y-framework-del-back.md) | Back | **Aceptada** el 14-nov (`D-57`) | Java 21 con Spring Boot; plan B: NestJS | Confirmada: `SPK-04`, paso 12 (unos 345 MiB de 1 GiB y 16 s de arranque) |
| [`ADR-016`](ADR-016-framework-del-front.md) | Front | **Aceptada** el 14-nov (`D-58`) | React con Vite y TypeScript; plan B: Vue | Confirmada: esqueleto desplegado como PWA instalable |
| [`ADR-017`](ADR-017-base-de-datos-y-archivos.md) | Base de datos y archivos | Propuesta | PostgreSQL en Azure (servidor flexible) y Azure Blob Storage, con Azurite en local; plan B: Neon y un almacén compatible con S3 | `SPK-06` |
| [`ADR-018`](ADR-018-proveedor-de-identidad.md) | Proveedor de identidad | **Aceptada** el 15-nov (`D-60`) | Auth0 en producción; Keycloak en local; plan B: Zitadel Cloud | Confirmada: el front inicia sesión y la API valida los tokens en producción (falta la parte local de `SPK-05`) |
| [`ADR-019`](ADR-019-proveedor-de-correo.md) | Correo | **Aceptada** el 15-nov (`D-61`) | API HTTP de Brevo con remitente verificado, sin dominio propio; Mailpit en local; plan B: Azure Communication Services Email | Confirmada: el aviso llegó a la bandeja de entrada en 4,0 s |
| [`ADR-020`](ADR-020-proveedor-de-whatsapp.md) | WhatsApp | **Aceptada** el 15-nov (`D-62`) | WhatsApp Cloud API de Meta con la plantilla `kudo_recibido` y un token permanente; plan B: Twilio Sandbox | Confirmada: `SPK-01` y el aviso real en 4,6 s |
| [`ADR-021`](ADR-021-canal-en-vivo.md) | Canal en vivo | **Aceptada** el 15-nov (`D-63`) | Server-Sent Events, abiertos desde el front con `fetch` y el token en la cabecera; plan B: WebSocket | Confirmada: el aviso en vivo llegó a la app abierta, unos 1,5 s después de publicar |
| [`ADR-022`](ADR-022-despliegue-y-monitor.md) | Despliegue y monitor | **Aceptada** el 14-nov (`D-59`) | Back en Azure Container Apps (`canadacentral`, una réplica siempre encendida), front en Azure Static Web Apps, monitor UptimeRobot; plan B: Render y Vercel | Confirmada: `SPK-04`, partes 1 a 3 y pasos 12 y 14 (falta el p95 del muro, como evidencia de `ESC-13`) |
| [`ADR-023`](ADR-023-ci-cd-y-analisis.md) | CI/CD y análisis | **Aceptada** el 14-nov (`D-56`) | GitHub Actions, SonarQube Cloud, GitHub Container Registry y Trivy; CD hacia Azure por OIDC | Confirmada: primer pipeline en verde y `SPK-04`, pasos 10 y 11 |
| [`ADR-024`](ADR-024-observabilidad.md) | Observabilidad | **Aceptada** el 15-nov (`D-64`) | El agente Java de Application Insights, hecho sobre OpenTelemetry; Grafana en local; plan B: Grafana Cloud | Confirmada: mapa de la aplicación y transacción completa de un Kudo en producción |
| [`ADR-025`](ADR-025-copias-de-seguridad.md) | Copias de seguridad | Propuesta | Tarea diaria en GitHub Actions hacia un segundo proveedor | `SPK-06` |
