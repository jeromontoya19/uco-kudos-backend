# UCO Kudos — Evidencias

**15-nov-2026 · Jerónimo Montoya Londoño**

Una fila por cada evidencia obligatoria del enunciado. Cada una dice dónde está la prueba y qué
demuestra. Las capturas están en esta carpeta, y las ejecuciones enlazadas son las de GitHub Actions.
Las versiones en producción son la API `10ebe87` y el front `17246be`.

| # | Evidencia obligatoria | Dónde está | Qué demuestra |
|---|---|---|---|
| 1 | **CI** | Back: [CI del PR #26](https://github.com/jeromontoya19/uco-kudos-backend/actions/runs/43773494870). Front: [CI del PR #16](https://github.com/jeromontoya19/uco-kudos-frontend/actions/runs/43773438672) | Cada PR a `codigo-testing` y a `main` compila, corre las pruebas, Sonar y Trivy, y se frena si algo falla (`ADR-023`) |
| 2 | **CD** | Back: [CD de `10ebe87`](https://github.com/jeromontoya19/uco-kudos-backend/actions/runs/43784895608) y [`despliegue/spk-04-paso-10-cd-base.png`](despliegue/spk-04-paso-10-cd-base.png). Front: [CD de `17246be`](https://github.com/jeromontoya19/uco-kudos-frontend/actions/runs/43784939818) | Cada merge a `main` despliega solo, entra a Azure por OIDC sin contraseñas y comprueba que la versión nueva responda (`ESC-15`) |
| 3 | **Sonar** | [`ci/ci-pr1-sonar-quality-gate.png`](ci/ci-pr1-sonar-quality-gate.png) (back) y [`ci/ci-pr4-front-sonar-quality-gate.png`](ci/ci-pr4-front-sonar-quality-gate.png) (front). Proyectos en SonarQube Cloud: [back](https://sonarcloud.io/project/overview?id=jeromontoya19_uco-kudos-backend) y [front](https://sonarcloud.io/project/overview?id=jeromontoya19_uco-kudos-frontend) | El *quality gate* pasa en la CI; el front tiene 100 % de cobertura en el PR #4 |
| 4 | **Trivy** | Pasos «Trivy» de la [CI del PR #26](https://github.com/jeromontoya19/uco-kudos-backend/actions/runs/43773494870) | Revisa el código y la imagen, ya con el agente de Application Insights, y frena el cambio si hay una vulnerabilidad CRÍTICA o ALTA con corrección. Salió en verde |
| 5 | **Publicación** | [`notificaciones/avisos-auditoria.png`](notificaciones/avisos-auditoria.png) y [`observabilidad/observabilidad-transaccion.webp`](observabilidad/observabilidad-transaccion.webp) | Un Kudo publicado desde el front: `POST /api/kudos` respondió 201 en 353 ms, y quedó `KUDO_PUBLICADO` en la auditoría (`ESC-04`) |
| 6 | **Despliegue** | [`despliegue/`](despliegue/): la API desde el celular, la PWA instalada y en el navegador, la vuelta atrás del back y del front y el monitor. URL pública: [happy-bush-0fa58021e.4.azurestaticapps.net](https://happy-bush-0fa58021e.4.azurestaticapps.net) | La API en Container Apps y la PWA en Static Web Apps, con vuelta atrás en menos de 1 min (`SPK-04`, `ESC-15`) |
| 7 | **Correo electrónico** | [`notificaciones/avisos-correo.png`](notificaciones/avisos-correo.png) | El aviso por Brevo llegó a la bandeja de entrada 4,0 s después de publicar (`ESC-05`, `ADR-019`) |
| 8 | **WhatsApp** | [`notificaciones/avisos-whatsapp.png`](notificaciones/avisos-whatsapp.png), [`spk-01-hello-world.png`](notificaciones/spk-01-hello-world.png) y [`spk-01-http.png`](notificaciones/spk-01-http.png) | La plantilla `kudo_recibido` llegó al celular verificado 4,6 s después de publicar (`ESC-05`, `ADR-020`, `SPK-01`) |
| 9 | **Push Notifications** | [`notificaciones/avisos-push.jpg`](notificaciones/avisos-push.jpg) | Con la app abierta en el celular, el estudiante vio el aviso flotante unos 1,5 s después de que la docente publicó (`ESC-14`, `ADR-021`) |
| 10 | **Auditoría** | [`notificaciones/avisos-auditoria.png`](notificaciones/avisos-auditoria.png) | Cada paso queda en `auditoria.registro`: `KUDO_PUBLICADO` y cada canal (`AVISO_CORREO`, `AVISO_WHATSAPP` y `AVISO_PUSH`), con su resultado. La base rechaza cambios y borrados (`ADR-008`, `PublicarKudoIT`) |
| 11 | **Observabilidad** | [`observabilidad/observabilidad-mapa.png`](observabilidad/observabilidad-mapa.png), [`observabilidad-transaccion.webp`](observabilidad/observabilidad-transaccion.webp) y [`observabilidad-despachador.webp`](observabilidad/observabilidad-despachador.webp). Monitor: [página de estado](https://stats.uptimerobot.com/Kg3YSa2rj9) | Application Insights muestra el mapa (API, PostgreSQL, Brevo, Meta y Auth0) y la transacción completa de un Kudo bajo un solo identificador (`ADR-024`, `ESC-12`). UptimeRobot vigila la salud cada 5 min (`ESC-07`) |
| 12 | **Pruebas** | Back: [CI del push de `f226edd`](https://github.com/jeromontoya19/uco-kudos-backend/actions/runs/43772929114). Front: [CI del push de `ba8140a`](https://github.com/jeromontoya19/uco-kudos-frontend/actions/runs/43772969371) | **Back:** 51 pruebas unitarias y 21 de integración contra PostgreSQL real (Testcontainers), las reglas de ArchUnit y JaCoCo con al menos 70 % en dominio y aplicación. **Front:** 147 pruebas con 98,8 % de cobertura, lint y compilación de tipos |

## Qué hay en cada carpeta

| Carpeta | Contenido |
|---|---|
| [`ci/`](ci/) | *Quality gates* de Sonar del back y del front |
| [`despliegue/`](despliegue/) | `SPK-04`: la API desde el celular, el CD, la vuelta atrás, la PWA y UptimeRobot |
| [`notificaciones/`](notificaciones/) | `SPK-01` y los avisos reales: auditoría, correo, WhatsApp y push |
| [`observabilidad/`](observabilidad/) | Application Insights: el mapa, la transacción de un Kudo y una vuelta del despachador |

**Datos personales:** ninguna captura muestra el celular ni el correo de una persona real. En el
WhatsApp solo se ve el número de prueba de Meta, y en el correo, «para mí».
