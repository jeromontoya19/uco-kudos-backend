# UCO Kudos — Alternativa de solución, bloques de construcción y spikes

> **Plantilla:** generada con IA a partir de FlorLogic y del enunciado. Las filas de ejemplo son sugerencias: revísalas o reemplázalas, y borra esta nota cuando el documento sea tuyo.

**v0.1 · 11-nov-2026 · Jerónimo Montoya Londoño**

Propone **una** alternativa de solución, la compara con las otras, declara los **bloques de
construcción** (ABB) y la tecnología que los materializa (SBB), y lista los **spikes** que deben
correr antes de escribir los ADR.

---

## 1 · Los drivers que mandan

- **Atributos de calidad (los cinco primeros de la §6.1 de los drivers):** …
- **Restricciones que cierran el espacio de diseño:** `CN-01` (plazo), `CN-02` (una persona), …
- **Funcionalidades significativas y dónde viven:** `RF-001` → …

---

## 2 · Alternativas de solución evaluadas

### `ALT-1` · Monolito modular con Clean Architecture y notificaciones asíncronas
(Un párrafo: qué es y cómo se despliega.)

### `ALT-2` · Microservicios con message broker
### `ALT-3` · Backend-as-a-Service (Supabase o Firebase como backend completo)
### `ALT-4` · Funciones serverless

### 2.1 Evaluación

| Criterio | `ALT-1` | `ALT-2` | `ALT-3` | `ALT-4` |
|---|:---:|:---:|:---:|:---:|
| Cabe en una persona y en 4,5 días (`CN-01`, `CN-02`) | | | | |
| Permite demostrar Clean Architecture (`CN-…`) | | | | |
| Responde a la alta lectura (`DRV-002`, `ESC-01`) | | | | |
| Publica sin depender de que respondan los canales (`ESC-02`) | | | | |
| Admite cambios en vivo durante la sustentación | | | | |

### 2.2 Alternativa elegida y por qué
(Por qué se elige y, sobre todo, por qué **no** cada una de las otras, citando `CN` y `ESC`.)

---

## 3 · Bloques de construcción de arquitectura (ABB): el *qué*

| ID | Bloque (ABB) | Responsabilidad | Drivers y escenarios que sostiene | ¿En la línea base? |
|---|---|---|---|---|
| `ABB-01` | Identidad y autenticación | Saber quién es el usuario y verificar su sesión | `DRV-005`, `ESC-…` | Sí |
| `ABB-02` | Autorización | Permitir cada acción solo a los roles que el enunciado autoriza | `DRV-005`, `RF-…` | Sí |
| `ABB-03` | Persistencia | | | |
| `ABB-04` | Caché de lectura | | `DRV-002`, `ESC-01` | |
| `ABB-05` | Gestión de archivos adjuntos | | `DRV-004` | |
| `ABB-06` | Notificaciones (correo, WhatsApp, push) | | `DRV-003`, `ESC-02` | |
| `ABB-07` | Mensajería asíncrona | | | |
| `ABB-08` | Auditoría | | `DRV-006` | |
| `ABB-09` | Logging y observabilidad | | `DRV-006` | |
| `ABB-10` | Configuración y secretos | | `DRV-005` | |
| `ABB-11` | DevSecOps (CI, CD, Sonar, Trivy) | | `DRV-007` | |

---

## 4 · Bloques de solución (SBB): el *con qué*

Una tabla por ABB, con los seis criterios que pide el enunciado. Escala **Bajo / Medio / Alto**: en
costo, complejidad y riesgo, Bajo es mejor; en escalabilidad, mantenibilidad y evolución, Alto es mejor.

### `ABB-06` · Notificaciones: canal WhatsApp

| SBB candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución | Veredicto |
|---|---|---|---|---|---|---|---|
| Meta WhatsApp Cloud API (número de prueba) | | | | | | | Lo decide `SPK-01` → `ADR-…` |
| Twilio Sandbox | | | | | | | Plan B |

---

## 5 · Spikes

| ID | Pregunta que responde | Tiempo | Criterio de muerte (qué te hace cambiar de camino) | Desbloquea |
|---|---|---:|---|---|
| `SPK-01` | ¿Puedo enviar un WhatsApp de prueba a un número verificado desde una llamada HTTP? | 1 h | Si en 60 min Meta no entrega un mensaje → Twilio Sandbox | `ABB-06`, `ESC-02`, `ADR-…` |
| `SPK-02` | ¿Puedo enviar un correo desde una API HTTP, sin SMTP? | 30 min | | |
| `SPK-03` | ¿Llega un push por WebSocket a un navegador conectado, y se detecta si no lo está? | 1 h | | |
| `SPK-04` | ¿Puedo desplegar un «hola mundo» del back y del front con URL pública? | 1,5 h | | |

---

## 6 · Cobertura de escenarios (se llena después de los ADR)

**Veredictos:** CUMPLE (hay un mecanismo y la medida es alcanzable) · PARCIAL (falta medir algo) ·
NO CUMPLE (se dice por qué y cuándo se reabre) · EN CONFLICTO (choca con otra decisión).

| ESC | Atributo | Medida comprometida | Mecanismo (ABB · ADR) | Veredicto | Por qué |
|---|---|---|---|---|---|
| `ESC-01` | Rendimiento | p95 ≤ 1 s | | | |

---

## 7 · Riesgos de la alternativa

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
