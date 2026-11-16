# UCO Kudos — Diseño detallado del back

> **Borrador:** preparado con IA a partir del código del back. Revísalo y borra esta nota cuando el
> documento sea tuyo.

**v0.2 · 15-nov-2026 · Jerónimo Montoya Londoño**

> Cómo está organizado por dentro el código de la API. Complementa el nivel 3 del
> [modelo C4](C4.md), que muestra los componentes.
>
> **Los diagramas están en draw.io:** [`imagenes/UCO-Kudos-diseno.drawio`](imagenes/UCO-Kudos-diseno.drawio).
> Los PNG de este documento se exportan de ese archivo.

## Paquetes del back

![Paquetes del back de UCO Kudos](imagenes/paquetes-del-back.png)

El código vive en `co.edu.uco.ucokudos` y se divide en cinco módulos, uno por cada hexágono de
`ADR-002`: `kudos`, `avisos`, `identidad`, `auditoria` y `moderacion`. Hoy `moderacion` solo está
diseñado, y sus paquetes están vacíos.

**Dentro de cada módulo** hay cuatro partes:

| Paquete | Qué guarda | De qué depende |
|---|---|---|
| `aplicacion.puerto.entrada` | Los contratos que usan los controladores y los otros módulos | Del dominio, si hace falta |
| `aplicacion` | Los casos de uso y los puertos de salida (por ejemplo, los repositorios) | Del dominio |
| `infraestructura` | Los adaptadores: web, persistencia, configuración y, según el módulo, seguridad, canales, canal en vivo o tareas programadas | De la aplicación y de sus puertos |
| `dominio` | Las reglas del negocio | De nada: ni de Spring ni de la base |

**Entre módulos** solo se usa el `aplicacion.puerto.entrada` del otro:

| Módulo | Usa de otro módulo |
|---|---|
| `kudos` | `ProgramarAvisos` y `KudoPorAvisar` de `avisos`; `ConsultarPersonas` de `identidad`; `RegistrarEnAuditoria` de `auditoria` |
| `avisos` | `ConsultarContactos` y `Contacto` de `identidad`; `RegistrarEnAuditoria` de `auditoria` |
| `identidad` | Nada |
| `auditoria` | Nada |

Así no hay ciclos entre módulos, y cada uno se puede probar con dobles de los puertos de los demás.

**Cómo se vigila:** `ReglasDeArquitecturaTest` (ArchUnit) corre en cada build de la CI y la detiene si
se rompe una de estas reglas:
1. El dominio no depende de frameworks (Spring, JPA, Servlet, Jackson).
2. El dominio no depende de `aplicacion` ni de `infraestructura`.
3. La `aplicacion` no depende de la `infraestructura`.
4. Los módulos no tienen ciclos entre sí.
5. Un módulo solo usa el `aplicacion.puerto.entrada` de otro.

## Diagramas de secuencia

Los tres flujos principales, tal como quedaron en producción el 15-nov-2026 (back `10ebe87`, front
`17246be`). Están en Mermaid, que GitHub dibuja directamente desde este Markdown. Los tiempos son los
de la traza real de Application Insights
(`docs/evidencias/observabilidad/observabilidad-transaccion.webp`).

### 1 · Publicar un Kudo (el flujo obligatorio)

Todo lo que queda guardado (el Kudo, sus tres avisos pendientes y la auditoría) va en **una sola
transacción** (`ADR-003`, `ADR-008`). La respuesta no espera a ningún canal: en producción tardó
353 ms.

```mermaid
sequenceDiagram
    autonumber
    actor Docente
    participant PWA
    participant Auth0
    participant Seg as Seguridad<br/>(ConvertidorDeTokenAPersona)
    participant Ctrl as ControladorDeKudos
    participant Tx as PublicarKudoEnTransaccion
    participant Pub as PublicacionDeKudos
    participant Per as ConsultarPersonas<br/>(identidad)
    participant Av as ProgramarAvisos<br/>(avisos)
    participant Aud as RegistrarEnAuditoria<br/>(auditoria)
    participant BD as PostgreSQL

    Docente->>PWA: Elige destinatario, categoría y mensaje
    PWA->>Auth0: Token de acceso (silencioso, 15 min)
    Auth0-->>PWA: JWT firmado (RS256)
    PWA->>Seg: POST /api/kudos + Bearer
    Seg->>Seg: Valida firma, emisor y audiencia
    Seg->>Per: porSujeto(sub)
    Per->>BD: persona activa con ese sujeto_idp
    Per-->>Seg: persona y rol (si no hay → 403)
    Seg->>Ctrl: Sesión = id de la persona
    Ctrl->>Tx: publicar(solicitud)
    Tx->>BD: BEGIN
    Tx->>Pub: publicar(solicitud)
    Pub->>Per: autor y destinatario activos
    Pub->>BD: categoría activa
    Note over Pub: Kudo: autor ≠ destinatario, contenido no vacío<br/>(si no → 422 con su motivo)
    Pub->>BD: INSERT kudos.kudo
    Pub->>Av: programar(KudoPorAvisar)
    Av->>BD: INSERT 3 avisos PENDIENTE (correo, WhatsApp, push)
    Av-)Av: anuncia AvisosProgramados
    Pub->>Aud: KUDO_PUBLICADO · EXITO
    Aud->>BD: INSERT auditoria.registro
    Tx->>BD: COMMIT
    Tx-->>Ctrl: KudoPublicado
    Ctrl-->>PWA: 201 Created (353 ms)
    PWA-->>Docente: El Kudo aparece primero en el muro
```

Si cualquier paso falla antes del `COMMIT`, no queda nada: ni el Kudo, ni sus avisos, ni su
auditoría (`PublicacionAtomicaIT`).

### 2 · Despachar los avisos (correo, push y WhatsApp)

Apenas se confirma la transacción, el despachador atiende el anuncio en otro hilo. La vuelta periódica
de cada 5 s hace lo mismo para los reintentos. Un candado deja correr una sola tanda a la vez.

```mermaid
sequenceDiagram
    autonumber
    participant Ev as AvisosProgramados<br/>(después del COMMIT)
    participant Desp as DespachadorProgramado
    participant Caso as DespachoDeAvisos<br/>(una transacción por aviso)
    participant Cont as ConsultarContactos<br/>(identidad)
    participant Brevo
    participant Vivo as PushEnVivo<br/>(ConexionesEnVivo)
    participant Meta as WhatsApp Cloud API
    participant BD as PostgreSQL

    Ev-)Desp: alProgramarAvisos (otro hilo)
    Desp->>Desp: toma el candado de la tanda
    Desp->>Caso: listosParaEnviar()
    Caso->>BD: avisos PENDIENTE con su hora cumplida (máx. 20)
    loop por cada aviso
        Desp->>Caso: despachar(aviso)
        Caso->>Cont: contactoDe(destinatario) y nombre del autor
        alt canal CORREO
            Caso->>Brevo: POST /v3/smtp/email (api-key)
            Brevo-->>Caso: 201 → ENTREGADO (1,1 s)
        else canal PUSH
            Caso->>Vivo: evento kudo-recibido a sus pestañas abiertas
            Vivo-->>Caso: ENTREGADO, u OMITIDO si no tiene la app abierta
        else canal WHATSAPP
            Caso->>Meta: POST /v25.0/{número}/messages (plantilla kudo_recibido)
            Meta-->>Caso: 200 → ENTREGADO (1,2 s)
        end
        Caso->>BD: UPDATE aviso (estado e intentos)
        Caso->>BD: INSERT auditoría AVISO_<CANAL> · EXITO / OMITIDO / FALLO
    end
    Desp->>Desp: suelta el candado
    Note over Desp,BD: Si un envío falla, el aviso sigue PENDIENTE y vuelve a intentarse<br/>a los 1, 2, 4, 8 y 15 min (5 intentos en 30 min). Después queda FALLIDO.
```

### 3 · Iniciar sesión y escuchar los avisos en vivo

```mermaid
sequenceDiagram
    autonumber
    actor Estudiante
    participant PWA
    participant Auth0
    participant API as API<br/>(ControladorEnVivo)
    participant Con as ConexionesEnVivo

    Estudiante->>PWA: Iniciar sesión
    PWA->>Auth0: Código de autorización con PKCE
    Auth0-->>PWA: Token de acceso (15 min) y refresh token (30 días sin uso)
    PWA->>API: GET /api/yo + Bearer
    API-->>PWA: nombre y rol
    PWA->>API: GET /api/avisos/en-vivo + Bearer (fetch, no EventSource)
    API->>Con: conectar(persona) · la conexión dura 30 min
    loop cada 25 s
        Con-->>PWA: :latido
    end
    Note over Con,PWA: Cuando alguien le da un Kudo (flujo 2)
    Con-->>PWA: event: kudo-recibido {kudo, autor, categoria}
    PWA-->>Estudiante: Aviso flotante y muro actualizado (~1,5 s después de publicar)
    Note over PWA,API: Si el servidor cierra a los 30 min o se cae la red,<br/>la PWA pide un token fresco y reconecta con espera de 1 s a 30 s
```

## Patrones de diseño

Solo los que resuelven un problema real del proyecto. Para cada uno: dónde está, qué problema resuelve
y qué se gana.

| Patrón | Dónde está | Problema que resuelve | Beneficio obtenido |
|---|---|---|---|
| **Puertos y adaptadores** (arquitectura hexagonal) | Cada módulo: `aplicacion.puerto.entrada`, puertos de salida en `aplicacion` y adaptadores en `infraestructura` (`ADR-002`) | Los casos de uso no deben depender de Spring, de la base ni de los proveedores | Los casos de uso se prueban sin infraestructura, y cambiar de base o de proveedor no toca el negocio. ArchUnit lo vigila |
| **Strategy** | `CanalDeAviso`, con `CorreoPorBrevo`, `WhatsAppPorMeta` y `PushEnVivo` | Cada canal se envía distinto, y el despachador no debe tener un `switch` por canal | Agregar un canal es agregar una clase (`D-38`). Si un canal no tiene adaptador, la API no arranca |
| **Adapter** | `CorreoPorBrevo`, `WhatsAppPorMeta`, los `Repositorio…Jdbc` y `ConvertidorDeTokenAPersona` | Las API de Brevo y Meta, la base y el token de Auth0 hablan su propio idioma | El resto del código habla en términos de UCO Kudos (`MensajeDeAviso`, `Persona`, `Kudo`). Cambiar Brevo por otro proveedor es reemplazar un adaptador (plan B de `ADR-019`) |
| **Decorator** | `PublicarKudoEnTransaccion`, `DespacharAvisosEnTransaccion` y `ProgramarAvisosConDespachoInmediato` | La transacción y el despacho inmediato son transversales, y los casos de uso no deben depender de Spring | Los casos de uso quedan puros y se prueban sin transacción; la transacción y el anuncio se agregan por fuera |
| **Transactional Outbox** | La tabla `avisos.aviso` y `DespachadorProgramado` (`ADR-003`) | Publicar no puede perder un aviso ni depender de que los proveedores respondan | Ningún Kudo pierde su aviso (`ESC-04`). Los fallos se reintentan solos y todo termina en un estado final (`ESC-05`, `ESC-08`) |
| **Observer** (eventos de aplicación) | El evento `AvisosProgramados` y `@TransactionalEventListener` en el despachador | Despachar apenas se confirma la publicación, sin que `kudos` conozca al despachador | El push llega en ~1,5 s (`ESC-14`). Si la transacción se revierte, el evento se descarta |
| **Repository** | `RepositorioDeKudos`, `RepositorioDeAvisos`, `RepositorioDePersonas` y sus versiones `…Jdbc` | El dominio no debe saber de SQL | Las pruebas usan dobles en memoria (`AvisosEnMemoria`, `PersonasEnMemoria`); producción usa Spring Data JDBC |
| **Value Object** | `KudoId`, `IdPersona`, `IdCategoria`, `Contenido` | Un UUID o un texto sueltos no dicen qué son ni validan nada | Un Kudo no puede existir con contenido vacío ni con autor igual al destinatario (`KudoInvalido`) |
| **DTO** | Los `record` de `puerto.entrada`: `SolicitudDePublicacion`, `KudoPublicado`, `KudoDelMuro`, `PersonaActiva`, `Contacto` | Las entidades del dominio no deben salir por la API ni cruzar módulos | La API y los otros módulos ven solo lo necesario. Por ejemplo, el muro no expone correos ni celulares |
| **State** (máquina de estados del aviso) | `Aviso` y `EstadoDelAviso`: PENDIENTE → ENTREGADO, OMITIDO o FALLIDO | Las reglas de reintento no deben quedar repartidas en el despachador | Una sola clase decide cuándo reintentar y cuándo rendirse (`AvisoTest`) |
| **Composition Root** | Las clases `Configuracion…` de cada módulo | Los casos de uso no deben crear sus dependencias | Todo el cableado está en un solo lugar por módulo, y los casos de uso reciben todo por constructor |

**Lo que no se usó, a propósito:**
- Un **broker de mensajes**: el outbox en la misma base basta con una sola instancia (`ADR-003`, `ADR-011`).
- **JPA**: se usó Spring Data JDBC, sin carga perezosa ni caché de sesión (`ADR-017`).
- Un **Service Locator**: todas las dependencias entran por constructor.
