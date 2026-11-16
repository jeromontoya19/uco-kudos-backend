# UCO Kudos — Twelve-Factor App y Manifiesto Reactivo

**v1.0 · 16-nov-2026 · Jerónimo Montoya Londoño**

> Las dos evaluaciones que pide el enunciado: la matriz de los doce factores, con cada factor
> analizado, evaluado y justificado, y cómo la solución aborda las cuatro propiedades del Manifiesto
> Reactivo. Todo se refiere a lo que está en producción: la API `10ebe87` y el front `17246be`.

**Escala:**
- **Cumple:** la práctica está aplicada y se puede mostrar.
- **Parcial:** está aplicada en parte, y lo que falta es una decisión consciente, con su ADR y su
  disparador.
- **No cumple:** no está aplicada, y se dice por qué.

---

## 1 · Twelve-Factor App

Doce prácticas para que una aplicación se despliegue en la nube sin sorpresas: configurable sin
recompilar, igual en todos los entornos y escalable agregando procesos.

| # | Factor | Qué pide | Cómo está en UCO Kudos | Evaluación | Justificación o qué falta |
|---|---|---|---|---|---|
| I | **Código base** | Un repositorio por aplicación, con muchos despliegues | Dos aplicaciones y dos repositorios: `uco-kudos-backend` y `uco-kudos-frontend` (`CN-12`, `ADR-013`). De cada uno sale un solo artefacto que se despliega | **Cumple** | La documentación vive en el repositorio del back, en `documentacion-desarrollo`, y el CD la ignora |
| II | **Dependencias** | Declararlas y aislarlas | Back: `pom.xml` con las versiones de Spring Boot y Maven Wrapper (`mvnw`). Front: `package-lock.json` instalado con `npm ci`. La imagen Docker trae el JRE y el jar; las acciones de GitHub van fijadas por SHA | **Cumple** | No depende de nada instalado en la máquina que construye ni en la que ejecuta |
| III | **Configuración** | La configuración en el entorno, no en el código | Back: todo lo que cambia entre entornos llega por variables (`BD_URL`, `BD_USUARIO`, `BD_CONTRASENA`, `IDP_EMISOR`, `IDP_AUDIENCIA`, `CORS_ORIGENES`, las llaves de Brevo y Meta, `PORT`, `APP_VERSION` y la cadena de Application Insights). Los secretos están en Container Apps y en GitHub, nunca en el repositorio (`ABB-17`) | **Cumple** en el back · **Parcial** en el front | El front es un sitio estático: la URL de la API y los datos públicos de Auth0 quedan en el JavaScript al compilar (`VITE_API_URL` o los valores por defecto de `configuracion.ts`). Ninguno es secreto, pero cambiarlos exige compilar de nuevo |
| IV | **Servicios de respaldo** | La base, el correo y los demás servicios son recursos que se conectan por configuración | PostgreSQL por `BD_URL`, Auth0 por `IDP_EMISOR`, Brevo y Meta por sus llaves, y Application Insights por su cadena de conexión. Cada uno está detrás de un puerto (`RepositorioDe…`, `CanalDeAviso`) | **Cumple** | Cambiar de proveedor es cambiar un adaptador y la configuración (`D-38`). Si falta la llave de un canal, la API arranca igual y sus avisos se reintentan |
| V | **Construir, publicar, ejecutar** | Separar las tres etapas | **Construir:** el CD crea la imagen y la etiqueta con el SHA del commit en GitHub Container Registry. **Publicar:** Container Apps crea una revisión con esa imagen y sus variables. **Ejecutar:** la réplica corre esa revisión | **Cumple** | La vuelta atrás despliega una imagen que ya existe, sin reconstruirla (47 s, `ESC-15`). En el front, Static Web Apps no guarda versiones: la vuelta atrás recompila el commit con su `package-lock` (38 s) |
| VI | **Procesos** | Procesos sin estado; lo que debe persistir va a un servicio de respaldo | No hay sesión en el servidor: cada petición trae su token (`SessionCreationPolicy.STATELESS`). Los Kudos, los avisos pendientes y la auditoría están en PostgreSQL | **Parcial** | Dos cosas viven en la memoria del proceso: las conexiones abiertas del canal en vivo y el candado que deja correr una sola tanda del despachador. Con una instancia funcionan; con varias necesitan un relevo de push y un bloqueo en la base (`ADR-011`, `T-3`) |
| VII | **Asignación de puertos** | La aplicación se expone sola en un puerto | Tomcat embebido en `${PORT:8080}`; la imagen expone el 8080 y el ingreso de Container Apps apunta allí | **Cumple** | No necesita un servidor de aplicaciones externo |
| VIII | **Concurrencia** | Escalar agregando procesos | Hoy, una réplica (mínimo y máximo 1, `ADR-011`). Dentro del proceso, las peticiones web y el despachador corren en hilos distintos (`D-46`) | **Parcial** | La API está preparada (sin estado de sesión), pero no se demostró con varias réplicas. Los 800 simultáneos son meta de diseño (`D-15`). El disparador está en `ADR-011` |
| IX | **Desechabilidad** | Arrancar rápido y apagarse en orden | Arranca en unos 16 s en Azure (`D-57`), con `server.shutdown: graceful` y sondas de *liveness* y *readiness* | **Cumple** | Si el proceso muere a mitad de un envío, el aviso sigue PENDIENTE en el outbox y se reintenta al volver. No se pierde ningún aviso (`ADR-003`) |
| X | **Paridad entre entornos** | Desarrollo y producción lo más parecidos posible | La misma imagen Docker en local y en Azure. PostgreSQL 16 en las pruebas (Testcontainers) y en Azure, con las mismas migraciones de Flyway | **Parcial** | El Docker Compose de `ADR-012` no está construido: en local se usan los pasos del manual, con Auth0 real y sin dobles de correo ni de WhatsApp. El WAF y el antivirus solo irían en local (`T-7`, `T-8`) |
| XI | **Registros** | Escribirlos como un flujo de eventos a la salida estándar | La API escribe en la consola. Container Apps los lleva a Log Analytics, y el agente de Application Insights los une a la traza de cada petición (`ADR-024`) | **Cumple** | Falta el formato JSON estructurado (`D-64`), pero el flujo ya es de eventos y nadie escribe archivos de registro |
| XII | **Procesos de administración** | Las tareas de administración, como procesos puntuales con el mismo código | Flyway corre las migraciones al arrancar, con el mismo código y la misma configuración, y siembra las personas y las categorías de prueba (`D-14`). La vuelta atrás es un workflow manual | **Cumple** | Nadie cambia el esquema a mano. La copia diaria fuera del proveedor (`ADR-025`) sigue propuesta |

**Resultado:** 8 cumplen y 4 son parciales (III en el front, VI, VIII y X); ninguno queda sin cumplir.
Los parciales vienen de decisiones conscientes: una sola instancia (`D-15`, `ADR-011`), un front estático
(`ADR-006`) y un entorno local que todavía no está automatizado (`ADR-012`).

---

## 2 · Manifiesto Reactivo

El manifiesto pide cuatro propiedades que dependen unas de otras: el **fin** es responder rápido
(*responsivo*); para lograrlo hay que aguantar fallos (*resiliente*) y cambios de carga (*elástico*); y
el **medio** es comunicarse por mensajes asíncronos (*orientado a mensajes*).

> «Reactivo» aquí **no** quiere decir programación reactiva (WebFlux o Reactor). La API usa Spring MVC;
> las propiedades salen de la arquitectura, no de una librería.

| Propiedad | Qué exige | Cómo la aborda UCO Kudos | Evidencia | Evaluación y límite |
|---|---|---|---|---|
| **Responsivo** | Responder rápido y de forma consistente | Publicar no espera a ningún canal: guarda el Kudo y sus avisos en una transacción y responde (`ADR-003`). La instancia está siempre encendida, sin arranque en frío (`D-53`). El muro se pagina, con un máximo de 50 por bloque, y se lee con un índice por fecha | `POST /api/kudos` en **353 ms** (`ESC-04`); push en unos **1,5 s** (`ESC-14`) | **Cumple** en publicar y avisar. **Parcial** en el muro: la caché de `ADR-004` no se construyó y el p95 con 50 simultáneos no se midió (`ESC-13`) |
| **Resiliente** | Seguir respondiendo aunque algo falle | El outbox aísla la publicación de los proveedores: si Brevo o Meta fallan, el Kudo se publica igual y el aviso se reintenta a los 1, 2, 4, 8 y 15 min, hasta un estado final auditado (`D-42`). Cada aviso va en su propia transacción, y uno que revienta no detiene a los demás. Las llamadas a los proveedores tienen tiempos máximos (5 s para conectar y 10 s para responder). La API expone sus sondas de salud (`/actuator/health/liveness` y `/readiness`) para que la plataforma la reinicie si se cuelga, y la vuelta atrás tarda menos de 1 min | `PublicacionAtomicaIT`, `DespachoDeAvisosIT`, `DespachadorProgramadoTest`, `AvisoTest`; vuelta atrás en 47 s (`ESC-15`) | **Parcial.** Contiene los fallos de los proveedores, pero la API es una sola instancia: si se cae, nadie responde hasta que la plataforma la reinicia. La copia fuera del proveedor (`ADR-025`) no está |
| **Elástico** | Ajustar los recursos a la carga | El front se sirve desde la red global de Static Web Apps. La API no guarda sesión, así que Container Apps podría agregar réplicas | Diseño: `ADR-011`, `D-15`, `D-54` | **No cumple en la línea base, a propósito.** Hay una réplica fija por plazo y crédito (`CN-01`, `CN-03`). Para escalar hacen falta tres piezas, ya nombradas en `ADR-011`: una caché compartida, un relevo de push entre instancias y que el despachador bloquee cada aviso en la base |
| **Orientado a mensajes** | Comunicarse con mensajes asíncronos, sin bloquear | El Kudo no llama a los canales: deja mensajes en el outbox (`avisos.aviso`). Al confirmarse la transacción se publica el evento `AvisosProgramados`, y el despachador lo atiende en otro hilo (`@TransactionalEventListener`). El aviso en vivo llega a la PWA como un evento de Server-Sent Events | Secuencias 1 y 2 de `DISENO_DETALLADO.md` | **Parcial.** Los mensajes son asíncronos y durables, pero viajan dentro del proceso y en la base, no por un broker (`D-47`). El broker es la evolución si hay varias instancias o más avisos de los que el despachador alcanza a enviar |

**Lectura de conjunto:** la línea base es reactiva donde el enunciado lo exige, en el flujo de publicar
y avisar: responde sin esperar a terceros, aguanta que un canal falle y se comunica por mensajes. La
elasticidad se cambió a conciencia por una sola instancia, y el camino para recuperarla está escrito en
`ADR-011`.
