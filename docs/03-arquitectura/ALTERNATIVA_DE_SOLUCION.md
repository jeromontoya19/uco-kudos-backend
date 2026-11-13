# UCO Kudos — Alternativa de solución, bloques de construcción y spikes

**v0.5 · 13-nov-2026 · Jerónimo Montoya Londoño**

> §1, §2, §3 y §7 cerradas con las decisiones de las rondas 8 y 9 (`D-45` a `D-52`). §4 y §5 resumen
> las tecnologías: las ya decididas (ADR aceptados) y las propuestas (`ADR-015` a `ADR-025`), con los
> spikes que las confirman. La §6 se llena cuando los spikes confirmen las propuestas.
> La ronda 10 (`D-53`, `D-54`) lleva la plataforma a Azure con el crédito de estudiante: cambian el
> presupuesto, varios SBB y `SPK-04`; la alternativa elegida no cambia. El 13-nov, `SPK-04` movió la
> API de App Service B1 a Container Apps y la región a `canadacentral` (§4, §5 y §7).

Propone **una** alternativa de solución, la compara con las otras, declara los **bloques de
construcción** (ABB) y la tecnología que los materializa (SBB), y lista los **spikes** que deben
correr antes de escribir los ADR. Se apoya en
[`DRIVERS_ARQUITECTONICOS.md`](../02-drivers-arquitectonicos/DRIVERS_ARQUITECTONICOS.md) y en
[`DECISIONES.md`](../02-drivers-arquitectonicos/DECISIONES.md).

---

## 1 · Los drivers que mandan

**Drivers de calidad** (§6.1 de los drivers): **Seguridad · Confiabilidad · Disponibilidad ·
Cumplimiento · Capacidad para ser Soportado.**

**Drivers:** los siete obligatorios del enunciado (`DRV-001` a `DRV-007`) y los dos que identificó el
autor (`DRV-008` Permisos sin herencia y `DRV-009` Datos personales).

**Restricciones que cierran el espacio de diseño:**

| Restricción | Qué deja por fuera |
|---|---|
| `CN-01` plazo y `CN-02` una sola persona | Todo lo que exija coordinar varios despliegues o aprender varias plataformas nuevas |
| `CN-03` crédito de 100 USD y `CN-24` una sola instancia (`D-15`) | Más de un servicio siempre encendido, y piezas autohospedadas que consumen memoria |
| `CN-12` dos repositorios y `CN-13` URL pública | Un cliente que no sea web y un contrato implícito entre el front y el back |
| `CN-15` Clean Architecture | Un backend sin código de dominio propio que mostrar |
| `CN-22` servicios gestionados y `CN-23` todo en local | Depender de un producto que no se pueda reemplazar en local |
| `CN-05` cambios en vivo | Un diseño que haya que entender completo para cambiar una parte |

**Funcionalidades significativas y dónde viven** (módulos de `D-49`):

| Módulo | Funcionalidades |
|---|---|
| Kudos | `RF-001` Publicar Kudo · `RF-004` Adjuntos · `RF-005` Muro · `RF-008` Réplicas |
| Avisos | `RF-002` Avisar al destinatario |
| Auditoría | `RF-003` Auditar toda acción |
| Identidad y permisos | `RF-006` Permisos por rol · `RF-007` Registro y activación |
| Moderación | `RF-009` Reportar y vetar |

---

## 2 · Alternativas de solución evaluadas

### `ALT-1` · Monolito modular con Clean Architecture y avisos en segundo plano

Una sola API, desplegada como un solo contenedor y organizada en cinco módulos con frontera
explícita: Kudos, avisos, identidad y permisos, auditoría y moderación. Cada módulo es un hexágono con
su dominio, sus casos de uso y sus adaptadores, y los módulos solo se hablan por interfaces
(`D-49`). Al publicar, el Kudo y sus avisos pendientes se guardan en la misma transacción (outbox,
`D-47`), y un despachador dentro del mismo proceso los envía y los reintenta (`D-46`). El muro se lee
de una caché en memoria (`D-48`). El cliente es una PWA aparte, que habla con la API por REST con un
contrato OpenAPI (`D-50`).

### `ALT-2` · Microservicios con message broker

Un servicio por capacidad (Kudos, avisos, identidad, auditoría), cada uno con su base de datos y su
despliegue, comunicados por un broker de mensajes.

### `ALT-3` · Backend como servicio (BaaS)

Supabase o Firebase hacen de backend: base de datos, identidad, archivos y funciones. Casi no hay API
propia: las reglas viven en políticas de la base y en funciones sueltas.

### `ALT-4` · Funciones serverless

Una función por operación (publicar, leer el muro, enviar un aviso) en una plataforma de funciones,
con la base de datos y los archivos como servicios gestionados.

### 2.1 Evaluación

| Criterio | `ALT-1` | `ALT-2` | `ALT-3` | `ALT-4` |
|---|:---:|:---:|:---:|:---:|
| Cabe en una persona y en 4,5 días (`CN-01`, `CN-02`) | **Sí** | No | Sí | Dudoso |
| Deja ver Clean Architecture (`CN-15`) | **Sí** | Sí | Débil | Débil |
| Cabe en el crédito con una instancia (`CN-03`, `D-15`) | **Sí** | No | Sí | Sí |
| Ningún Kudo se pierde si falla un canal (`ESC-04`) | **Sí** | Sí | Parcial | Parcial |
| Se cambia de proveedor sin tocar las reglas (`D-38`) | **Sí** | Sí | No | Parcial |
| El muro responde con p95 ≤ 1 s gracias a una caché (`ESC-13`) | **Sí** | Sí | Parcial | No |
| Todo se levanta en local con un comando (`D-20`) | **Sí** | Difícil | Parcial | Difícil |
| Admite cambios en vivo en la sustentación (`CN-05`) | **Sí** | No | Parcial | Parcial |

El criterio de costo decía «Corre con 0 USD y una instancia» hasta `D-53`. Con el crédito, los
veredictos no cambian: `ALT-2` sigue necesitando varios servicios siempre encendidos, y eso no cabe en
la meta de 30 USD al mes.

En `ALT-4`, el muro queda en «No» porque cada función tiene su propio arranque en frío y no comparte
una caché en memoria.

### 2.2 Alternativa elegida y por qué

**Se elige `ALT-1`** (`D-45`).

**`ALT-2` se descarta por `CN-01`, `CN-02` y `CN-03`.** Son varios servicios siempre encendidos, un
broker y despliegues coordinados, para una sola persona, en 4,5 días y con un crédito de 100 USD. Los microservicios
resuelven sobre todo el problema de varios equipos trabajando a la vez, y aquí hay uno solo. Lo que
ofrecen —escalar por partes— se conserva como evolución: los módulos de `ALT-1` tienen frontera
explícita y se pueden separar después.

**`ALT-3` es la más tentadora**, porque compra hechas la identidad, la base y los archivos (`D-19`).
Se descarta por tres razones concretas:

1. **`CN-15`.** Casi no queda código de dominio con el que mostrar Clean Architecture.
2. **`D-38`.** Las reglas quedan atadas al proveedor (políticas y funciones propias del producto), y
   cambiar de proveedor deja de ser solo configuración.
3. **`D-02` y `ESC-04`.** El outbox con reintentos habría que construirlo igual, con funciones
   sueltas.

> `[!]` **Queda una puerta abierta.** Sí se usan servicios gestionados para piezas concretas (la base
> de datos, los archivos y el IdP), por `D-19`, pero detrás de adaptadores y nunca como dueños de las
> reglas.

**`ALT-4` se descarta** porque pone en riesgo `ESC-13` (cada función tiene su propio arranque en frío
y no comparte una caché en memoria), porque es difícil de levantar en local con un comando (`D-20`) y
porque repartir el flujo de publicar en funciones complica la transacción del outbox.

**Lo que `ALT-1` cuesta, dicho sin adornos.** Escalar es escalar todo junto. Con una sola instancia, la
caché en memoria y el canal en vivo funcionan sin coordinarse; con dos o más, necesitan una caché
compartida y un relevo de push (`T-3`, `D-15`). Ese es el disparador para separar módulos o agregar
piezas.

---

## 3 · Bloques de construcción de arquitectura (ABB): el *qué*

Las piezas que necesita la solución, sin decir todavía con qué tecnología (`D-52`). Cada una sale de
un driver, un escenario o una decisión.

| ID | Bloque | Responsabilidad | Drivers, escenarios y decisiones | ¿En la línea base? |
|---|---|---|---|---|
| `ABB-01` | Cliente móvil (PWA) | Interfaz *mobile first* e instalable, que consume el contrato de la API | `DRV-001`; `ESC-16`; `D-17`, `D-50` | Sí |
| `ABB-02` | Proveedor de identidad (IdP) | Cuentas, inicio de sesión y emisión de tokens estándar (OIDC) | `DRV-005`; `ESC-02`; `D-09`, `D-27`, `D-28` | Sí |
| `ABB-03` | Autorización por rol | Decidir en la API si el rol activo puede hacer cada acción, sin herencia por jerarquía | `DRV-005`, `DRV-008`; `ESC-01`; `RF-006` | Sí |
| `ABB-04` | API de UCO Kudos | Casos de uso y reglas del dominio, en cinco módulos hexagonales | `CN-15`; `RF-001` a `RF-009`; `D-45`, `D-49` | Sí |
| `ABB-05` | Persistencia | Guardar Kudos, usuarios, avisos pendientes y auditoría, con transacciones | `ESC-04`, `ESC-09` | Sí |
| `ABB-06` | Caché del muro | Servir el muro desde la memoria e invalidarlo al publicar o vetar | `DRV-002`; `ESC-13`; `D-06`, `D-10`, `D-48` | Sí |
| `ABB-07` | Almacén de adjuntos y miniaturas | Guardar los archivos fuera de la base, generar la miniatura al subir una imagen y entregarlos solo con sesión | `DRV-004`; `D-03`, `D-29` | Sí |
| `ABB-08` | Validación de adjuntos | Revisar el tipo real y el tamaño antes de guardar; en local, además, cuarentena con antivirus | `DRV-004`; `ESC-03`; `D-11`, `D-41` | Sí (el antivirus, solo en local) |
| `ABB-09` | Outbox y despachador de avisos | Guardar los avisos pendientes junto con el Kudo y enviarlos con reintentos, en segundo plano | `DRV-003`; `ESC-04`, `ESC-05`, `ESC-08`; `D-02`, `D-42`, `D-46`, `D-47` | Sí |
| `ABB-10` | Canal de correo | Enviar el aviso por correo a través de un proveedor externo | `DRV-003`; `ESC-05`; `D-38` | Sí |
| `ABB-11` | Canal de WhatsApp | Enviar el aviso por WhatsApp a través de un proveedor externo | `DRV-003`; `ESC-05`; `CN-18`; `D-38` | Sí |
| `ABB-12` | Canal en vivo (push) | Entregar el push a quien tiene la aplicación abierta y saber si no está conectado | `DRV-003`; `ESC-14`; `D-04` | Sí |
| `ABB-13` | Auditoría de solo inserción | Registrar toda acción con identificadores, sin datos personales y sin permitir cambios | `DRV-006`, `DRV-009`; `ESC-09`, `ESC-10`; `D-08`, `D-22` | Sí |
| `ABB-14` | Observabilidad | Registros estructurados, trazas unidas por un identificador de publicación y métricas por canal | `DRV-006`; `ESC-12`; `P-28` | Sí |
| `ABB-15` | Monitor de disponibilidad | Ping de salud periódico, desde fuera de Azure, que mide la disponibilidad de la API y avisa si se cae (con capas gratuitas, además, la mantiene despierta) | `ESC-07`; `D-31`, `D-43`, `D-53` | Sí |
| `ABB-16` | Copias de seguridad | Copia diaria de la base y de los adjuntos fuera del proveedor principal, con restauración probada | `DRV-004`; `ESC-06`; `D-40` | Sí |
| `ABB-17` | Configuración y secretos | Toda la configuración por variables de entorno, y los secretos fuera del repositorio | `DRV-005`; `CN-23` | Sí |
| `ABB-18` | Borde de la API | HTTPS, CORS, validación de entrada y límite de peticiones; en local, además, gateway con WAF | `DRV-005`; `D-51` | Sí (el gateway, solo en local) |
| `ABB-19` | Integración continua | Compilar, probar, medir la cobertura y analizar con Sonar y Trivy en cada cambio | `DRV-007`; `CN-11`, `CN-16`, `CN-25` | Sí |
| `ABB-20` | Despliegue continuo | Construir la imagen, guardarla en un registro, desplegarla y poder volver a la anterior | `DRV-007`; `ESC-15`; `D-38` | Sí |
| `ABB-21` | Entorno local | Levantar todo con un comando, con dobles de los servicios externos | `CN-23`; `D-20` | Sí |

### 3.1 Lo que deliberadamente no se adopta

Piezas que la guía de evaluación o una arquitectura «de manual» esperarían, y por qué no están. Cada
una tiene un disparador que obliga a reconsiderarla.

| Pieza | Decisión | Qué se usa en su lugar | Disparador para reconsiderarla |
|---|---|---|---|
| Message broker dedicado | No se adopta (`D-47`) | Outbox en la base y despachador dentro de la API | Varias instancias de la API, o más avisos de los que el despachador alcanza a enviar |
| Caché compartida | No se adopta en la línea base (`D-48`) | Caché en memoria de la API | Más de una instancia (`D-15`) |
| API Gateway y WAF en producción | Solo en local (`D-51`) | El HTTPS de la plataforma; CORS, validación y límite de peticiones en la API | Presupuesto para otra instancia. En Azure, un WAF gestionado (Application Gateway o Front Door Premium) cuesta varios cientos de dólares al mes |
| Antivirus en producción | Solo en local (`D-41`) | Revisar el tipo real y el tamaño | Presupuesto para unos 2 GB de RAM |
| Relevo de push entre instancias | No se adopta en la línea base (`D-15`) | Una sola instancia con su canal en vivo | Más de una instancia (`T-3`) |
| Service mesh | No se adopta; se deriva de `D-45` | Un solo servicio: no hay tráfico entre servicios que gobernar | Separar módulos en servicios (`ALT-2`) |
| Orquestador de contenedores | No se adopta; se deriva de `D-45` y `CN-03` | La plataforma gestionada corre un solo contenedor | Varios servicios o instancias que coordinar |
| Data mesh o bodega de datos | No se adopta; se deriva de `D-45` | Las consultas van sobre la misma base | Un dominio analítico aparte, con su propio equipo |

---

## 4 · Bloques de solución (SBB): el *con qué*

Qué tecnología materializa cada bloque. **La evaluación de cada candidato con los seis criterios del
enunciado** (costo, complejidad, escalabilidad, riesgo, mantenibilidad y evolución) **está en su ADR**;
aquí va el resumen. **Aceptada** quiere decir que ya está decidida; **Propuesta**, que la confirma un
spike de la §5 o la construcción.

| ABB | SBB (con qué) | Estado | Otros candidatos evaluados | ADR |
|---|---|---|---|---|
| `ABB-01` Cliente móvil | React con Vite y TypeScript, como PWA | Propuesta | Vue, Angular, Svelte | `ADR-016` (el tipo de cliente, en `ADR-006`) |
| `ABB-02` Proveedor de identidad | Auth0 o Zitadel Cloud en producción; Keycloak en local | Propuesta | Keycloak autohospedado, Supabase Auth | `ADR-018` (el patrón, en `ADR-007`) |
| `ABB-03` Autorización por rol | La propia API, con el framework del back | Propuesta | — | `ADR-015` (la regla, en `ADR-007`) |
| `ABB-04` API | Java 21 con Spring Boot | Propuesta | NestJS, ASP.NET Core, FastAPI | `ADR-015` (la forma, en `ADR-001` y `ADR-002`) |
| `ABB-05` Persistencia | PostgreSQL gestionado: Azure Database for PostgreSQL, servidor flexible (plan B: Neon) | Propuesta | MySQL, MongoDB; Neon, Supabase | `ADR-017` |
| `ABB-06` Caché del muro | Caché en la memoria de la API | Aceptada (la librería, con `ADR-015`) | Redis gestionado, sin caché | `ADR-004` |
| `ABB-07` Almacén de adjuntos | Azure Blob Storage; Azurite en local (plan B: un almacén compatible con S3, con MinIO en local) | Propuesta | Archivos en la base, disco de la instancia; R2, B2, Supabase Storage | `ADR-017` (las reglas, en `ADR-009`) |
| `ABB-08` Validación de adjuntos | Tipo real y tamaño en la API; ClamAV solo en local | Aceptada | Antivirus en producción | `ADR-009` |
| `ABB-09` Outbox y despachador | Tabla de outbox en la base y tarea en segundo plano de la API | Aceptada | Broker gestionado, eventos en memoria | `ADR-003` |
| `ABB-10` Canal de correo | Resend o Brevo por API HTTP; Mailpit en local | Propuesta | SMTP personal, Amazon SES | `ADR-019` |
| `ABB-11` Canal de WhatsApp | WhatsApp Cloud API de Meta | Propuesta | Twilio Sandbox | `ADR-020` |
| `ABB-12` Canal en vivo | Server-Sent Events | Propuesta | WebSocket, consulta periódica | `ADR-021` |
| `ABB-13` Auditoría | Tabla de solo inserción en la base | Aceptada (el motor, con `ADR-017`) | — | `ADR-008` |
| `ABB-14` Observabilidad | OpenTelemetry hacia Application Insights (Azure Monitor); Grafana en local | Propuesta | Grafana Cloud, solo registros de la plataforma, Sentry | `ADR-024` |
| `ABB-15` Monitor de disponibilidad | UptimeRobot | Propuesta | Better Stack | `ADR-022` (la meta, en `ADR-014`) |
| `ABB-16` Copias de seguridad | Tarea diaria en GitHub Actions hacia otro proveedor | Propuesta | Copias del proveedor, copia manual | `ADR-025` (la meta, en `ADR-014`) |
| `ABB-17` Configuración y secretos | Variables de entorno; secretos en GitHub y en los secretos de Container Apps (Key Vault si sobra tiempo) | Propuesta | — | `ADR-022`, `ADR-023` |
| `ABB-18` Borde de la API | HTTPS de la plataforma; CORS, validación y límite de peticiones en la API; Nginx con ModSecurity solo en local | Aceptada | WAF gratuito delante | `ADR-010` |
| `ABB-19` Integración continua | GitHub Actions, SonarQube Cloud y Trivy | Propuesta | GitLab CI, CircleCI | `ADR-023` |
| `ABB-20` Despliegue continuo | GitHub Actions y GitHub Container Registry, hacia Azure Container Apps y Azure Static Web Apps (plan B: Render y Vercel) | Propuesta | Docker Hub, Azure Container Registry; Azure App Service B1 (cuota 0 en la suscripción), Koyeb, Cloud Run | `ADR-023`, `ADR-022` |
| `ABB-21` Entorno local | Docker Compose con dobles de los servicios externos | Aceptada | Solo en la nube | `ADR-012` |

---

## 5 · Spikes

Un spike responde **una sola** pregunta, tiene un límite de tiempo y termina con un criterio de
muerte: el resultado que obliga a cambiar de camino. Cada uno se documenta en `spikes/` con la
plantilla de esa carpeta.

| ID | Pregunta que responde | Tiempo | Criterio de muerte (qué te hace cambiar de camino) | Desbloquea |
|---|---|---:|---|---|
| `SPK-01` | ¿Llega un WhatsApp con la plantilla del aviso a un número verificado, desde una llamada HTTP a la API de Meta? | 1 h | Si en 60 min Meta no entrega un mensaje, se pasa a Twilio Sandbox | `ADR-020`, `ESC-05`, `CN-18` |
| `SPK-02` | ¿Llega un correo por la API HTTP del proveedor a cualquier destinatario, en ≤ 1 min? | 30 min | Si el proveedor exige un dominio propio para escribirle a cualquiera, se pasa al otro candidato | `ADR-019`, `ESC-05` |
| `SPK-03` | ¿Llega un aviso por SSE a un navegador conectado en ≤ 2 s, con el token validado, y se detecta cuando se desconecta? | 1 h | Si no se puede autenticar la conexión de forma segura, o la plataforma corta las conexiones abiertas, se pasa a WebSocket | `ADR-021`, `ESC-14` |
| `SPK-04` | `[:W:]` ¿Se activa Azure for Students y deja crear la API, la base y el almacén en una misma región; despliega GitHub Actions en la plataforma; arranca el back en una réplica siempre encendida, da el muro con p95 ≤ 1 s con 50 simultáneos y vuelve a la imagen anterior en ≤ 10 min? | 3 h | Si la suscripción no se activa, no deja crear los servicios en una región común o GitHub Actions no puede desplegar, se pasa al plan B (Render y Vercel, `D-53`). Si el back no aguanta, el plan B del back (NestJS), y la cifra de `D-39` se ajusta con datos | `ADR-015`, `ADR-022`, `ADR-023`, `ESC-07`, `ESC-13`, `ESC-15`, `T-1`, `T-9` |
| `SPK-05` | ¿Emite el IdP gestionado tokens que la API valida con la misma configuración que usa con Keycloak en local? ¿Quién verifica el correo y quién el celular? | 2 h | Si pasar de Keycloak al gestionado exige tocar código, se prueba el otro candidato | `ADR-018`, `ESC-02`, `RF-007`, `T-2`, `T-5` |
| `SPK-06` | ¿Se conectan la base y el almacén gestionados con el mismo código que sus contenedores locales (PostgreSQL y Azurite)? ¿Se restaura una copia completa en ≤ 4 h? | 1,5 h | Si el proveedor pausa o borra los datos, o la restauración tarda más de 4 h, se prueba el siguiente proveedor | `ADR-017`, `ADR-025`, `ESC-06`, `T-5` |

`SPK-01` va primero: es el canal más riesgoso, y la aprobación de la plantilla puede tardar.
`SPK-04` va en paralelo: la activación de la suscripción y las regiones permitidas pueden bloquear todo
el despliegue, y conviene saberlo hoy.

**Estado al 13-nov-2026:**

| Spike | Estado | Qué falta |
|---|---|---|
| `SPK-04` | Partes 1 y 2 terminadas: suscripción activa, presupuesto creado, y la API (Container Apps), la base y los archivos en `canadacentral`, con el front en `westus2`. La API de prueba responde desde el celular. De la parte 3, el paso 9: la identidad del pipeline y los secretos en los dos repositorios | Primer despliegue desde GitHub, vuelta atrás y las medidas con el back real |
| `SPK-01` | Respondido: el `hello_world` llegó al celular desde la página y desde una llamada HTTP a la Cloud API de Meta (v25.0), con el número de prueba. La plantilla `kudo_recibido` está en revisión | La aprobación de la plantilla y el token permanente para producción |
| `SPK-02`, `SPK-03`, `SPK-05`, `SPK-06` | Sin empezar | Se corren al construir cada adaptador |

---

## 6 · Cobertura de escenarios (se llena después de los ADR)

**Veredictos:** CUMPLE (hay un mecanismo y la medida es alcanzable) · PARCIAL (falta medir algo) ·
NO CUMPLE (se dice por qué y cuándo se reabre) · EN CONFLICTO (choca con otra decisión).

| ESC | Atributo | Medida comprometida | Mecanismo (ABB · ADR) | Veredicto | Por qué |
|---|---|---|---|---|---|
| `ESC-01` | Seguridad | 100 % de las acciones no autorizadas rechazadas y auditadas | | | |

---

## 7 · Riesgos de la alternativa

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| La réplica de 0,5 vCPU y 1 GB no aguanta los 50 simultáneos de `ESC-13` | Baja | Alto | Medirlo en el spike de despliegue y ajustar la cifra de `D-39` con datos; si hace falta, subir a 1 vCPU y 2 GB |
| La suscripción de estudiante no deja crear los servicios en una región común, o GitHub Actions no puede autenticarse en Azure (`CN-19`) | Media | Alto: no hay despliegue | `SPK-04` con límite de 3 h; si falla, plan B con Render y Vercel (`D-53`). **Pasó en parte:** PostgreSQL estaba restringido en `westus2` y la cuota de B1 era 0; se resolvió con `canadacentral` y Container Apps |
| El crédito se agota antes o durante la evaluación (`T-9`) | Baja | Alto: la URL deja de responder | Meta de 30 USD al mes, alerta de presupuesto (creada el 13-nov) y revisar el saldo antes de sustentar |
| La réplica de Container Apps siempre encendida cuesta más de lo estimado (`ADR-022`) | Media | Medio: se gasta el crédito más rápido | La alerta de presupuesto; revisar *Cost Management* a diario durante la semana de evaluación, y después bajar la réplica mínima a 0 |
| La SIC no considera a Canadá un país con nivel adecuado de protección (`CN-04`) | Baja | Medio: los datos reales no podrían quedar en `canadacentral` | La demo usa datos de prueba. Si se confirma el problema, la base y los archivos se pasan a otra región permitida |
| WhatsApp no aprueba la plantilla, o el número de prueba limita los envíos (`CN-18`) | Media | Alto: se incumple `CN-14` | Solicitar la plantilla el primer día; si falla, otro proveedor detrás del mismo adaptador (`D-38`) |
| No hay un IdP gratuito que funcione bien con la API (`T-2`) | Media | Alto | Spike de identidad antes del ADR |
| `[:DD:]` La plataforma gratuita prohíbe o limita el ping de salud (`D-31`) | Baja | Medio: vuelve el arranque en frío | Con `D-53` la instancia no se duerme. Vuelve a aplicar solo si se pasa al plan B: confirmarlo entonces y, si no se puede, despertar la API antes de la demo |
| Local y producción se comportan distinto (`T-5`, `T-7`, `T-8`) | Media | Medio | Servicios que hablen protocolos estándar, y la CI probando con el mismo Docker Compose |
| El monolito crece sin respetar las fronteras entre módulos | Media | Medio: se pierden `CN-15` y `CN-05` | Pruebas de arquitectura que fallen si un módulo usa el interior de otro |
| La caché en memoria sigue mostrando un Kudo vetado | Baja | Medio | Invalidar la caché en el mismo caso de uso que publica o veta (`D-10`) |
