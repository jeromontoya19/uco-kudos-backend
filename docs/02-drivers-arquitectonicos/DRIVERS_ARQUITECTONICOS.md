# UCO Kudos — Drivers arquitectónicos

> **Plantilla:** generada con IA a partir de FlorLogic y del enunciado. Las filas de ejemplo son sugerencias: revísalas o reemplázalas, y borra esta nota cuando el documento sea tuyo.

> **v0.1 · 11-nov-2026 · Borrador.**
> Reúne todo lo que es driver arquitectónico: funcionalidades significativas, restricciones de
> negocio y técnicas, drivers del enunciado, atributos de calidad priorizados, preguntas de
> caracterización y escenarios de calidad.

---

## 0 · Cómo leer este documento

**Fuente.** El enunciado del proyecto integrador. No hay cliente que entrevistar: el enunciado es la
voz del cliente, y lo que no dice queda marcado como **PROPUESTA** del arquitecto.

**Reglas.** 1) Los identificadores nunca se renumeran. 2) No se borra nada: lo que cambia se marca y
se explica. 3) Cada entrada dice de qué parte del enunciado sale.

| Marca | Significado |
|---|---|
| `[!]` | Problema abierto, contradicción o cabo suelto |
| `[:V:]` | Vigente |
| `[:W:]` | Reescrito respecto a la versión anterior |
| `[:DD:]` | Descartado o derogado |
| **PROPUESTA** | Lo decide el arquitecto: el enunciado no lo dice |

---

## 1 · El marco: qué se está construyendo

(De tres a seis líneas: qué es UCO Kudos, quién lo usa y desde dónde.)

> ### **«…»**

(La frase que ordena las prioridades. En FlorLogic fue «certeza de los datos». Aquí sale de la
*Funcionalidad obligatoria* y de los DRV. Escríbela tú.)

**Alcance de la línea base.**
- **Se construye:** …
- **Se diseña, pero no se construye:** …

---

## 2 · Funcionalidades significativas `RF-nnn`

**Tipo:** `Valor de negocio` · `Reto técnico` · `Ambos`.
**Estado:** `[:V:]` vigente · `[:W:]` reescrito · `[:DD:]` descartado.

### 2.1 Cuadro de mando

| ID | Enunciado en una línea | Tipo | Estado |
|---|---|---|---|
| `RF-001` | Publicar un Kudo y notificar al destinatario por correo, WhatsApp y push, con auditoría | Ambos | [:V:] |
| `RF-002` | | | |

### 2.2 Detalle

| ID | Funcionalidad significativa | Especificación | Tipo | Justificación | Origen | Estado | Observación |
|---|---|---|---|---|---|---|---|
| `RF-001` | Publicar Kudo | El sistema debe permitir a un usuario activo publicar un Kudo con destinatario, categoría, fecha, contenido sin límite de longitud y adjuntos; guardarlo; notificar al destinatario por correo y por WhatsApp; enviarle una notificación push en tiempo real si está conectado, y auditar todo el proceso. | Ambos | Es el flujo obligatorio de la línea base: si falla, el proyecto no cumple. | Enunciado, *Funcionalidad obligatoria de línea base*; `DRV-003` | [:V:] | `[!]` Falta decidir qué pasa con el Kudo si un canal falla. Lo responde un escenario de la §8. |

---

## 3 · Restricciones de negocio `CN-01` …

**Tipos:** Tiempo · Presupuesto · Legal · Proceso · Humano.

| ID | Tipo | Restricción | Justificación | Plan de acción |
|---|---|---|---|---|
| `CN-01` | Tiempo | Entregar documentación, Front-End, Back-End, manual y evidencias, con la aplicación desplegada y la sustentación hecha, a más tardar el **lunes 16 de noviembre de 2026 a las 12:00** | Plazo del curso, ampliado por el docente desde el viernes 13 | Recortar alcance antes que mover la fecha; el código se congela el domingo 15 a las 20:00 |
| `CN-02` | Humano | El trabajo es **individual** | Modalidad definida en el enunciado | Pocas tecnologías, conocidas, y servicios gestionados |

---

## 4 · Restricciones técnicas `CN-10` …

**Tipos:** `Técnica impuesta` (viene del enunciado y no se negocia) · `Técnica adoptada` (la elige
el arquitecto). **Estado:** `DENTRO` · `EN DUDA` · `CERRADO`.

### 4.1 Impuestas

| ID | Tipo | Restricción técnica | Justificación | Origen | Estado | Plan de acción |
|---|---|---|---|---|---|---|
| `CN-10` | Técnica impuesta | Toda la documentación se entrega en Markdown (`.md`) | Formato exigido | Enunciado, *Información general* y *Entregables* | DENTRO | Diagramas en Mermaid dentro de los `.md`, o draw.io exportado a PNG en `imagenes/` |
| `CN-11` | Técnica impuesta | CI, CD, análisis estático con Sonar y escaneo con Trivy desde la línea base | DevSecOps obligatorio | Enunciado, *DevSecOps obligatorio*; `DRV-007` | DENTRO | |

### 4.2 Adoptadas

| ID | Tipo | Restricción técnica | Justificación | Origen | Estado | Plan de acción |
|---|---|---|---|---|---|---|
| `CN-…` | Técnica adoptada | **PROPUESTA** Preferir tecnologías que el autor ya usó en FlorLogic | Una sola persona, poco tiempo y cambios en vivo durante la sustentación | `CN-02`; enunciado, *Uso de Inteligencia Artificial* | DENTRO | |

---

## 5 · Drivers del enunciado `DRV-001` … `DRV-007`

| DRV | Qué dice el enunciado | Atributos de calidad que mueve | Escenarios |
|---|---|---|---|
| `DRV-001` Movilidad | ~90 % de los usuarios desde el celular; *mobile first* | Experiencia de Usuario · Portabilidad | `ESC-…` |
| `DRV-002` Alta demanda de lectura | La mayoría de las operaciones son consultas | Rendimiento · Escalabilidad | `ESC-01` |
| `DRV-003` Notificaciones multicanal | Correo, WhatsApp y push | Interoperabilidad · Disponibilidad · Confiabilidad | `ESC-02` |
| `DRV-004` Adjuntos | | | |
| `DRV-005` Seguridad | | | |
| `DRV-006` Observabilidad | | | |
| `DRV-007` Automatización | | | |

---

## 6 · Atributos de calidad

Los 13 atributos de la plantilla de clase: Confiabilidad · Disponibilidad · Rendimiento · Capacidad
para ser Auditado · Capacidad · Capacidad para ser Administrado · Experiencia de Usuario · Seguridad ·
Interoperabilidad · Escalabilidad · Capacidad para ser Soportado · Portabilidad · Accesibilidad.

### 6.1 Priorización por actor (la que manda)

Cada actor reparte del 1 al 13 (menor = más prioritario): 91 puntos por actor y 273 en total.
`[!]` **PROPUESTA:** no hubo votación real. El arquitecto vota en nombre de cada actor con lo que el
enunciado dice de él.

| # | Atributo | Comunidad académica (estudiantes y docentes) | Directivos (rector y coordinadores) | Administrador del sistema | Total | % |
|---|---|:--:|:--:|:--:|:--:|:--:|
| 1 | | | | | | |
| … | | | | | | |
| | **Total** | 91 | 91 | 91 | 273 | 100 % |

> ## **Los drivers de calidad: … · … · … · … · …**

### 6.2 Trade-offs: qué se sacrifica cuando dos atributos chocan

| Choque | Gana | Se sacrifica | Por qué | Deriva de |
|---|---|---|---|---|
| Responder rápido al publicar vs. confirmar que llegaron las notificaciones | | | | `DRV-002`, `DRV-003` |

---

## 7 · Preguntas de caracterización

Preguntas de sí o no por atributo, en el orden del ranking de la §6.1. **Respuesta:** `SÍ`, `NO` o
`NO DICE`, según el enunciado y con la sección. Cada actor reparte 20 puntos, con un máximo de 5 por
pregunta. Las de mayor puntaje forman el **Top**, y cada pregunta del Top se vuelve un escenario en la §8.

| # | Atributo | Pregunta | Respuesta del enunciado | Comunidad | Directivos | Admin | Total | ¿Top? |
|---|---|---|---|:--:|:--:|:--:|:--:|:--:|
| 1 | Rendimiento | ¿Se necesita que el muro de Kudos responda rápido en hora pico desde el celular? | SÍ — `DRV-001`, `DRV-002` | | | | | |
| 2 | Disponibilidad | ¿Se necesita que un Kudo quede publicado aunque falle un canal de notificación? | NO DICE → **PROPUESTA** | | | | | |

---

## 8 · Escenarios de calidad `ESC-nn`

Seis partes (Bass, Clements y Kazman), agrupados **por atributo** y no por requisito. El número del
escenario es el puesto de su pregunta en el Top de la §7. Las medidas que no salen del enunciado son
**PROPUESTA**, y se dice.

### 8.1 Rendimiento

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-01` | 500 estudiantes y docentes conectados desde el celular | consultan el muro de Kudos al mismo tiempo | la API de consulta del muro y su caché de lectura | operación normal, hora pico | entrega la primera página del muro con sus categorías y autores | p95 ≤ 1 s y 0 errores **(PROPUESTA)** | `DRV-002`, pregunta 1 |

### 8.2 Disponibilidad

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-02` | El proveedor de WhatsApp | no responde o rechaza el envío cuando se publica un Kudo | el despachador de notificaciones | operación normal | el Kudo queda publicado; el envío se reintenta con espera creciente y cada intento queda auditado | 0 Kudos perdidos; la publicación responde en ≤ 2 s sin esperar al canal; hasta 3 reintentos en 15 min; 100 % de los intentos auditados **(PROPUESTA)** | `DRV-003`, pregunta 2 |

---

## 9 · Trazabilidad (se llena después de los ADR)

| Driver o escenario | ADR que lo resuelve | Dónde se ve en el código o en una prueba |
|---|---|---|
| `ESC-01` | | |
