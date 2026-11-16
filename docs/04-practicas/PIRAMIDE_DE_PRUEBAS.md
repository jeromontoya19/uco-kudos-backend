# UCO Kudos — Pirámide de pruebas

**v1.0 · 16-nov-2026 · Jerónimo Montoya Londoño**

> Qué pruebas automatizadas tiene la solución en cada nivel, qué cubre cada una y dónde corren. Es lo
> que pide el enunciado en *Pirámide de pruebas* (`CN-16`): unitarias, de integración y de punta a
> punta. Los números son los de la última CI en verde (back `f226edd`, front `ba8140a`).

## Resumen

| Nivel | Back | Front | Dónde corren | Evaluación |
|---|---|---|---|---|
| **Unitarias** | 51 | 147 pruebas en total, la mayoría de este nivel | En cada push y en cada PR | **Cumple** |
| **Integración** | 21, contra PostgreSQL 16 real (Testcontainers) | Componentes con su hook y su cliente HTTP, con la API simulada | En cada push y en cada PR | **Cumple** |
| **Punta a punta** | Los escenarios completos del flujo obligatorio, por HTTP y contra la base real | — | En cada PR, y la verificación del CD en producción después de cada despliegue | **Parcial:** no hay una prueba automatizada que maneje el navegador |

Además corren en cada build:
- **Reglas de arquitectura:** `ReglasDeArquitecturaTest`, con ArchUnit.
- **Cobertura:** JaCoCo exige al menos 70 % en dominio y aplicación (`CN-25`), y el front va en 98,8 %.
- **Sonar y Trivy:** sobre el código y sobre la imagen (`ADR-023`).

---

## 1 · Unitarias: componentes aislados

Sin base de datos, sin red y sin Spring. Los puertos se reemplazan por dobles en memoria
(`AvisosEnMemoria`, `PersonasEnMemoria`, `CategoriasEnMemoria`).

| Qué se prueba | Pruebas |
|---|---|
| El dominio: un Kudo válido, autor distinto del destinatario, contenido no vacío | `KudoTest` |
| Los casos de uso de publicar y del muro | `PublicacionDeKudosTest`, `ConsultaDelMuroTest`, `ConsultaDeCategoriasTest`, `ConsultaDePersonasTest` |
| La máquina de estados del aviso y su política de reintentos (5 intentos en 30 min) | `AvisoTest` |
| Programar y despachar avisos, incluido el despacho inmediato | `ProgramacionDeAvisosTest`, `DespachoDeAvisosTest`, `ProgramarAvisosConDespachoInmediatoTest`, `DespachadorProgramadoTest` |
| Los adaptadores de Brevo y de Meta contra un servidor HTTP simulado, y el push en vivo | `CanalesHttpTest`, `PushEnVivoTest` |
| La auditoría | `AnotacionEnAuditoriaTest` |
| Front: el lector SSE, la reconexión, el cliente HTTP con sus errores, las fechas y las notificaciones | `sse.test.ts`, `flujoSse.test.ts`, `clienteHttp.test.ts`, `apiHttp.test.ts`, `ErrorDeApi.test.ts`, `fechas.test.ts`, `notificaciones.test.ts`, `version.test.ts` |

## 2 · Integración: componentes juntos

**Back:** Spring Boot completo, contra un **PostgreSQL 16 real** que Testcontainers levanta en Docker.
Se usan las mismas migraciones de Flyway que en producción (`BaseDeDatosDePrueba`).

| Qué se prueba | Pruebas |
|---|---|
| Las migraciones: esquemas por módulo, personas y categorías de prueba | `MigracionesIT` |
| La salud, con sus sondas, y la versión | `SaludIT` |
| Guardar un Kudo con sus avisos y su auditoría, y que la base no deje cambiar la auditoría | `PublicarKudoIT` |

**Front:** cada pantalla con sus componentes, sus hooks y el cliente de la API simulado (Vitest con
Testing Library): `App`, `PantallaPrincipal`, `Muro`, `FormularioDePublicacion`, `PanelFlotante`,
`Cabecera`, `AvisoFlotante`, `OfertaDeNotificaciones`, `useSesion`, `useConsulta` y
`useInstalacionPwa`.

## 3 · Punta a punta: escenarios completos

### 3.1 · Automatizadas, en la CI del back

Recorren un escenario completo, de la petición HTTP a la base de datos y a la auditoría. Solo se
reemplazan las piezas externas: el validador de tokens de Auth0 y los proveedores de correo y WhatsApp.

| Escenario completo | Prueba | Qué comprueba |
|---|---|---|
| Publicar desde la API y verlo en el muro | `ApiDeKudosIT` | `POST /api/kudos` responde 201 y el Kudo queda primero en `GET /api/kudos`. Además: 401 sin token, 403 para una cuenta sin persona, 422 con su motivo, 400 para una petición incompleta, `/api/yo` con el rol y el preflight de CORS |
| Publicar es todo o nada | `PublicacionAtomicaIT` | Si el último paso falla, no quedan ni el Kudo, ni sus avisos, ni su auditoría (`ESC-04`) |
| El flujo obligatorio completo: publicar → avisar → auditar | `DespachoDeAvisosIT` | Con los proveedores fallando, el Kudo se publica, el correo y WhatsApp quedan para reintento, el push se omite, y todo queda auditado contra el Kudo (`ESC-05`, `ESC-08`) |
| El despacho inmediato | `DespachoInmediatoIT` | Con la vuelta periódica puesta en 1 h, los avisos se intentan en segundos después de publicar (`ESC-14`) |

### 3.2 · Automatizada, en producción después de cada despliegue

| Qué | Dónde | Qué comprueba |
|---|---|---|
| CD del back | `.github/scripts/verificar-despliegue.sh` | Que la API en Azure responda con el SHA del commit recién desplegado y con la salud en UP, que incluye la conexión con la base. Si no, el CD queda en rojo |
| CD del front | Workflow `CD` de `uco-kudos-frontend` | Que el sitio sirva la versión nueva, su manifiesto y su *service worker* |
| Monitor | UptimeRobot | Que `/actuator/health` responda cada 5 min (`ESC-07`) |

### 3.3 · Manual, en producción, con evidencia

El escenario completo con usuarios reales se probó el 15-nov-2026:
- El estudiante tenía la app abierta en el celular, y el docente publicó desde otro navegador.
- Llegaron el aviso flotante, el correo y el WhatsApp.
- La auditoría y la traza de Application Insights lo confirman.

Evidencias: [`avisos-push.jpg`](../evidencias/notificaciones/avisos-push.jpg),
[`avisos-correo.png`](../evidencias/notificaciones/avisos-correo.png),
[`avisos-whatsapp.png`](../evidencias/notificaciones/avisos-whatsapp.png),
[`avisos-auditoria.png`](../evidencias/notificaciones/avisos-auditoria.png) y
[`observabilidad-transaccion.webp`](../evidencias/observabilidad/observabilidad-transaccion.webp).

### Lo que falta

**Una prueba automatizada que maneje el navegador:** iniciar sesión en Auth0, publicar desde la PWA
y ver el aviso en otra sesión, por ejemplo con Playwright. No se construyó, por dos razones:
- Necesita cuentas de prueba de Auth0 guardadas como secretos de la CI.
- Llegó tarde frente al plazo (`CN-01`).

El disparador para construirla es el primer cambio grande del front después de la entrega.
