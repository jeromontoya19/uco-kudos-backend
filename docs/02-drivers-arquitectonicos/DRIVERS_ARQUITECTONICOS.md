# UCO Kudos — Drivers arquitectónicos

> **v0.8 · 13-nov-2026 · §1 a §8 cerradas con las decisiones de las rondas 1 a 7; la ronda 10 (`D-53`, `D-54`) reescribe `CN-03`, `CN-19`, `CN-22`, `ESC-07` y un trade-off de la §6.2. `CN-04` y `CN-19` anotan lo que encontró `SPK-04`. La §9 ya enlaza cada escenario con sus ADR, y falta la evidencia, que llega al construir.**
> Reúne todo lo que es driver arquitectónico: funcionalidades significativas, restricciones de
> negocio y técnicas, drivers del enunciado, atributos de calidad priorizados, preguntas de
> caracterización y escenarios de calidad.

---

## 0 · Cómo leer este documento

**Fuentes.** El enunciado del proyecto integrador y las decisiones del autor. No hay cliente que
entrevistar: el enunciado es la voz del cliente, y lo que no dice se decide en rondas de preguntas
registradas en [`DECISIONES.md`](DECISIONES.md). Cada entrada cita la decisión de la que sale (`D-nn`).

**Reglas.** 1) Los identificadores nunca se renumeran. 2) No se borra nada: lo que cambia se marca y
se explica. 3) Cada entrada dice de qué parte del enunciado sale.

| Marca | Significado |
|---|---|
| `[!]` | Problema abierto, contradicción o cabo suelto |
| `[:V:]` | Vigente |
| `[:W:]` | Reescrito respecto a la versión anterior |
| `[:DD:]` | Descartado o derogado |
| `D-nn` | Decisión del autor, registrada en `DECISIONES.md` |
| `T-nn` | Tensión entre dos decisiones; su estado (abierta, reducida, cerrada o aceptada) está en la sección *Tensiones* de `DECISIONES.md` |
| **PROPUESTA** | Todavía no decidido: va a la siguiente ronda de preguntas |

---

## 1 · El marco: qué se está construyendo

**UCO Kudos** es la plataforma institucional de reconocimientos de la Universidad Católica de
Oriente. Una persona de la comunidad académica le reconoce a otra un logro o un comportamiento —un
**Kudo**— dentro de una categoría institucional, y el reconocido se entera enseguida: por correo, por
WhatsApp y, si tiene la aplicación abierta, con un aviso en pantalla. La usan siete roles organizados
en una jerarquía, y casi todos entran desde el celular y para leer.

**Lo que fijan el enunciado y las decisiones, en cifras.**

| Hecho | Cifra | Origen |
|---|---|---|
| Usuarios que entran desde el celular | ~90 % | `DRV-001` |
| Tipo de carga | Predominantemente de lectura | `DRV-002` |
| Roles | 7, y cada persona tiene **uno solo** activo | *Roles institucionales*, *Exclusividad* |
| Cargos únicos | 1 Rector, 1 Coordinador Académico, 1 coordinador por facultad y 1 por programa | *Unicidad* |
| Categorías de Kudos | 15 como mínimo | *Categorías de Kudos* |
| Canales de aviso por cada Kudo | 3: correo, WhatsApp y push (este último solo si el destinatario está conectado) | *Funcionalidad obligatoria de línea base* |
| Condiciones para activar una cuenta | 3: solicitud aprobada, correo verificado y celular verificado | *Registro y activación* |
| Límites de las réplicas | Ninguno: ni de profundidad, ni de cantidad, ni de longitud | *Réplicas* |
| Calificación | De 1 a 5 estrellas, una sola activa por usuario y contenido | *Calificaciones* |
| Roles que pueden vetar | 2 de 7: Rector y Coordinador Académico | *Moderación* |
| Usuarios para los que se diseña | 10.000, con un pico de 800 simultáneos | `D-05` |
| Archivos por Kudo | Hasta 3, imágenes o PDF de máximo 5 MB cada uno | `D-11` |
| Presupuesto de infraestructura | Crédito de Azure for Students: 100 USD para 12 meses, sin tarjeta; meta de 30 USD al mes como máximo | `D-53` (reemplaza a `D-07`) |
| Entrega | Lunes 16-nov-2026, 12:00, desplegado y sustentado | `CN-01` |

**¿Qué hace que el sistema ya no sea útil?**

> ### **«Que un Kudo se pierda o que su destinatario nunca se entere.»**

Decidida en `D-01`. En FlorLogic esta frase fue del cliente; aquí sale del enunciado: la única
funcionalidad que se exige construir completa es publicar un Kudo **y** avisarle al destinatario por
tres canales.

**Por qué importa para la arquitectura, en una frase:** publicar y avisar son dos cosas distintas:
el Kudo tiene que quedar guardado aunque falle un canal, y el aviso tiene que llegar aunque tarde
(`D-02`).
Por eso la confiabilidad y la disponibilidad de publicar y avisar quedaron entre los tres primeros
atributos de calidad, detrás de la seguridad, que ganó la votación por actor (§6.1, `D-35`).

**Alcance de la línea base.**
- **Se construye:** publicar un Kudo con adjuntos (`RF-001`, `RF-004`), avisar por los tres canales
  (`RF-002`), auditar todo el proceso (`RF-003`), el muro de Kudos (`RF-005`) y los permisos por rol
  en lo que se construye (`RF-006`). Los usuarios (uno por rol) y las 15 categorías entran como
  datos semilla.
- **Se construye solo si sobra tiempo:** réplicas (`RF-008`), reporte y veto (`RF-009`),
  calificaciones y administración de categorías.
- **Se diseña, pero no se construye:** registro y activación con la cadena de aprobaciones
  (`RF-007`) y la gestión académica de facultades, programas, docentes y estudiantes.

---

## 2 · Funcionalidades significativas `RF-nnn`

**Qué entra aquí.** Una funcionalidad es significativa si, cuando falla, el sistema deja de servir,
o si obliga a la arquitectura a tener un mecanismo propio. Las demás funcionalidades del enunciado
existen igual, pero no mueven la arquitectura: la §2.5 dice cuáles quedaron fuera y por qué.

**Tipo:** `Valor de negocio` · `Reto técnico` · `Ambos`.
**Estado:** `[:V:]` vigente · `[:W:]` reescrito · `[:DD:]` descartado.
**Línea base:** `Se construye` · `Extra` (solo si sobra tiempo) · `Se diseña`.

### 2.1 Cuadro de mando

| ID | Enunciado en una línea | Tipo | Línea base | Estado |
|---|---|---|---|---|
| `RF-001` | Publicar un Kudo y dejarlo guardado aunque fallen los avisos | Valor de negocio | Se construye | [:V:] |
| `RF-002` | Avisar al destinatario por correo, WhatsApp y push en tiempo real | Ambos | Se construye | [:V:] |
| `RF-003` | Auditar toda acción, incluido el resultado de cada aviso | Ambos | Se construye | [:V:] |
| `RF-004` | Adjuntar archivos con almacenamiento, seguridad, recuperación y trazabilidad | Reto técnico | Se construye | [:V:] |
| `RF-005` | Consultar el muro de Kudos desde el celular | Ambos | Se construye | [:V:] |
| `RF-006` | Permitir cada acción solo al rol que el enunciado autoriza | Valor de negocio | Se construye en parte | [:V:] |
| `RF-007` | Activar una cuenta solo con aprobación, correo y celular verificados | Valor de negocio | Se diseña | [:V:] |
| `RF-008` | Replicar a Kudos y a réplicas sin límite de profundidad | Reto técnico | Extra | [:V:] |
| `RF-009` | Reportar contenido y vetarlo (solo Rector y Coordinador Académico) | Ambos | Extra | [:V:] |

**9 funcionalidades significativas: 6 entran en la línea base, 2 son extra y 1 solo se diseña.**

### 2.2 Publicar y avisar

Las cuatro funcionalidades del flujo obligatorio. El enunciado lo describe como **un solo flujo de
cinco pasos**; aquí se parte en cuatro porque cada parte necesita un mecanismo distinto —guardar,
avisar hacia afuera, auditar y guardar archivos— y porque cada una falla por razones distintas.

| ID | Funcionalidad significativa | Especificación | Tipo | Justificación | Origen | Estado | Observación |
|---|---|---|---|---|---|---|---|
| `RF-001` | Publicar Kudo | El sistema debe permitir a un usuario con cuenta activa publicar un Kudo dirigido a otra persona de la comunidad, con categoría, fecha, contenido descriptivo sin límite de longitud y, si quiere, archivos adjuntos, y debe dejarlo guardado aunque después falle alguno de los avisos. | Valor de negocio | Es el centro del producto y el paso 1 del flujo obligatorio: sin Kudo guardado no hay nada que avisar ni que auditar. | Enunciado, *Gestión de Kudos* y *Funcionalidad obligatoria de línea base* (paso 1) | [:V:] | Cualquier persona de la comunidad puede reconocer a cualquier otra, sin importar su programa o su rol; autor y destinatario son distintos y el destinatario tiene la cuenta activa (`D-12`). La fecha es la de publicación. |
| `RF-002` | Avisar al destinatario | Al publicarse un Kudo, el sistema debe enviarle al destinatario un correo electrónico y un mensaje de WhatsApp y, si en ese momento tiene la aplicación abierta, una notificación push en tiempo real. | Ambos | Sin aviso, el reconocido no se entera y el Kudo pierde su efecto. Además son tres integraciones externas que fallan cada una por su lado. | Enunciado, *Funcionalidad obligatoria de línea base* (pasos 2, 3 y 4); `DRV-003` | [:V:] | Si un canal falla, el Kudo queda publicado igual y el aviso se reintenta en segundo plano (`D-02`; lo miden `ESC-04` y `ESC-08`). «Conectado» es tener la aplicación abierta: si no lo está, el push se omite y queda auditado (`D-04`). |
| `RF-003` | Auditar toda acción | El sistema debe registrar toda acción que cambie algo —la publicación de cada Kudo, el resultado de cada aviso (enviado, fallido, reintentado u omitido porque el destinatario no estaba conectado), los vetos, las aprobaciones y los cambios de categorías—, con quién, qué y cuándo. Los registros solo se agregan: nunca se editan ni se borran. | Ambos | El enunciado exige que todo el proceso de publicación quede auditado, y ese registro es la evidencia de que los cinco pasos ocurrieron. | Enunciado, *Funcionalidad obligatoria de línea base* (paso 5); `DRV-006`; `D-08` | [:V:] | El enunciado solo exige auditar la publicación; `D-08` lo extiende a toda acción. En la línea base se audita lo que se construye: la publicación y sus avisos. |
| `RF-004` | Adjuntar archivos | El sistema debe permitir adjuntar archivos a un Kudo, guardarlos de forma segura, entregarlos solo a usuarios con sesión iniciada, poder recuperarlos ante una falla y registrar quién subió cada archivo, cuándo y con qué huella digital. | Reto técnico | El enunciado pide diseñar cuatro estrategias para los adjuntos: almacenamiento, seguridad, recuperación y trazabilidad. Un archivo no se guarda igual que un dato. | Enunciado, *Gestión de Kudos* y *Archivos adjuntos*; `DRV-004` | [:V:] | Imágenes y PDF, hasta 5 MB por archivo y como máximo 3 por Kudo (`D-11`). Solo se entregan con sesión iniciada porque los Kudos solo los ve la comunidad (`D-03`). En el muro se muestran miniaturas generadas al subir la imagen; el original se descarga al abrir el Kudo (`D-29`). |

### 2.3 Consultar

Las dos sostienen la carga de lectura (`DRV-002`): una es la consulta que más se repite; la otra es
la que puede crecer sin tope.

| ID | Funcionalidad significativa | Especificación | Tipo | Justificación | Origen | Estado | Observación |
|---|---|---|---|---|---|---|---|
| `RF-005` | Consultar el muro de Kudos | El sistema debe mostrar desde el celular, a toda la comunidad con sesión iniciada, los Kudos publicados del más reciente al más antiguo, con su autor, destinatario, categoría, fecha y contenido. El muro se recorre deslizando, sin números de página, y un Kudo nuevo aparece en el muro de los demás en pocos segundos. | Ambos | Es la operación que más se va a repetir (`DRV-002`) y casi siempre desde un celular (`DRV-001`): si es lenta, la plataforma no se usa. | Enunciado, `DRV-001` y `DRV-002`; `D-03`, `D-06`, `D-13` | [:V:] | El enunciado no nombra un «muro»: habla de una carga orientada a la lectura, y `D-13` fija el muro como la consulta principal. Como todos ven el mismo muro (`D-03`), se puede cachear una sola vez para todos. |
| `RF-008` | Réplicas anidadas | El sistema debe permitir responder a un Kudo y a cualquier réplica, sin límite de profundidad, de cantidad ni de longitud, y mostrar cada conversación con su estructura de respuestas. | Reto técnico | El modelo es recursivo y no tiene tope: define cómo se guardan y cómo se leen las conversaciones sin volver lento el muro (`DRV-002`). | Enunciado, *Réplicas*; `DRV-002` | [:V:] | Extra: se construye solo si sobra tiempo. Las réplicas se cargan cuando el usuario abre la conversación, no junto con el muro (`D-13`). |

### 2.4 Identidad, permisos y moderación

Quién entra, qué puede hacer y quién decide qué se ve. Las tres salen de reglas explícitas del
enunciado y sostienen `DRV-005` y `DRV-008`.

| ID | Funcionalidad significativa | Especificación | Tipo | Justificación | Origen | Estado | Observación |
|---|---|---|---|---|---|---|---|
| `RF-006` | Permisos por rol | El sistema debe permitir cada acción solo a los roles que el enunciado autoriza: administrar categorías, solo Administrador, Rector y Coordinador Académico; vetar contenido, solo Rector y Coordinador Académico; aprobar una solicitud, solo el aprobador que fija la tabla de aprobaciones. Cada persona actúa con su único rol activo. | Valor de negocio | Son reglas explícitas del enunciado y el núcleo de `DRV-005`: una sola regla mal aplicada, como un docente que veta, es una falla de seguridad. | Enunciado, *Administración de categorías*, *Moderación*, *Flujo de aprobaciones* y *Exclusividad*; `DRV-005`, `DRV-008` | [:V:] | `[!]` **Los permisos no se heredan por la jerarquía:** el Administrador está por encima del Rector y aun así no puede vetar. Por eso se asignan por rol y no por nivel. Un veto oculta el contenido del muro y lo conserva, con quién lo vetó, cuándo y por qué (`D-10`). La identidad la maneja un proveedor de identidad y la API solo valida sus tokens (`D-09`). Si a alguien le quitan el rol, pierde el acceso en minutos, cuando vence su token (`D-27`). En la línea base, los permisos se aplican a lo que se construye. |
| `RF-007` | Registro y activación | El sistema debe permitir que una persona solicite su cuenta con su correo institucional y su número celular, y activarla solo cuando se cumplan las tres condiciones: la solicitud fue aprobada por el aprobador que le corresponde según su rol, el correo fue verificado y el celular fue verificado. | Valor de negocio | Define quién entra a la plataforma. Combina una cadena de aprobación jerárquica con dos verificaciones que pueden usar los mismos canales externos de los avisos (correo y WhatsApp). | Enunciado, *Registro y activación* y *Flujo de aprobaciones*; `DRV-008`, `DRV-009` | [:V:] | Se diseña, pero no se construye: la línea base usa un usuario semilla por rol. Al Docente, que no aparece en la tabla del enunciado, lo aprueba el Coordinador de Programa (`D-32`). Al Administrador lo aprueba otro Administrador, así que el primero se crea con datos semilla al desplegar (`D-14`). `[!]` Con un proveedor de identidad (`D-09`), el registro se reparte entre el IdP y UCO Kudos: falta decidir cuál de los dos verifica el correo y el celular. Lo decide el ADR de identidad (`T-2`). |
| `RF-009` | Moderar contenido | El sistema debe permitir que cualquier usuario reporte un Kudo o una réplica, y que solo el Rector y el Coordinador Académico la veten. Un veto oculta el contenido del muro para toda la comunidad y lo conserva, con quién lo vetó, cuándo y por qué. | Ambos | El veto cambia lo que ve toda la comunidad, así que obliga a invalidar la caché del muro (`DRV-002`), y es una de las reglas de permisos más finas del enunciado. | Enunciado, *Moderación*; `DRV-008`; `D-10`, `D-26` | [:V:] | Extra: se construye solo si sobra tiempo. El permiso de vetar está en `RF-006`, y cada reporte y cada veto quedan auditados (`D-08`). |

### 2.5 Lo que quedó fuera de las significativas, y por qué

| Funcionalidad del enunciado | Dónde quedó | Por qué no es significativa |
|---|---|---|
| Calificar Kudos y réplicas de 1 a 5, con una sola calificación activa por usuario | Funcionalidad normal · extra | Es una restricción de unicidad en la base de datos; no pide ningún mecanismo nuevo |
| Administrar categorías | Funcionalidad normal · extra | Es crear, editar y borrar. Lo significativo es quién puede hacerlo, y eso está en `RF-006` |
| Un solo rol activo por persona y unicidad de cargos | §3, restricciones de negocio | Son reglas de la organización, no funcionalidades |
| Gestión académica: qué programas estudia cada estudiante, en cuáles enseña cada docente y qué acompaña cada coordinador | Se diseña en la arquitectura empresarial | Es administración de datos de referencia; no impone ningún mecanismo |

`[:W:]` «Reportar contenido» estaba en esta tabla. Pasó a ser significativa junto con el veto, como
`RF-009`, porque el veto oculta contenido para toda la comunidad e invalida la caché del muro (`D-26`).

---

## 3 · Restricciones de negocio `CN-01` …

**Tipos:** Tiempo · Presupuesto · Legal · Proceso · Humano.

| ID | Tipo | Restricción | Justificación | Plan de acción |
|---|---|---|---|---|
| `CN-01` | Tiempo | Entregar documentación, Front-End, Back-End, manual y evidencias, con la aplicación desplegada y la sustentación hecha, a más tardar el **lunes 16 de noviembre de 2026 a las 12:00** | Plazo del curso, ampliado por el docente desde el viernes 13 | Recortar alcance antes que mover la fecha; el código se congela el domingo 15 a las 20:00 |
| `CN-02` | Humano | El trabajo es **individual**: una sola persona diseña, construye, despliega, documenta y sustenta | Enunciado, *Modalidad* | Pocas piezas que operar: servicios gestionados (`D-19`) y una sola instancia en la línea base (`D-15`) |
| `CN-03` | Presupuesto | `[:W:]` El **crédito de Azure for Students**: 100 USD para 12 meses, sin tarjeta, y nada pagado de bolsillo | Es un trabajo de una materia, sin presupuesto propio; el crédito lo da Microsoft a los estudiantes verificados (`D-53`, que reemplaza a `D-07`) | Gastar como máximo 30 USD al mes, con alerta de presupuesto (`T-9`); correr una sola instancia, siempre encendida; los 800 simultáneos de `D-05` quedan como meta de diseño (`D-15`). Si la suscripción no se activa, plan B con capas gratuitas (`ADR-022`) |
| `CN-04` | Legal | El correo institucional y el celular son **datos personales**, y su tratamiento se rige por la Ley 1581 de 2012 | Ley de protección de datos personales de Colombia (`D-16`) | Consentimiento al registrarse, uso de los datos solo para UCO Kudos, el celular nunca visible para otros usuarios, datos de prueba en la demo y supresión por anonimización (`D-22`). Los datos quedan en Canadá (`canadacentral`, `SPK-04`): `[!]` falta confirmar que la SIC lo trate como país con nivel adecuado de protección; Estados Unidos sí está en su lista |
| `CN-05` | Proceso | La **sustentación** es obligatoria e individual, y en ella pueden pedir cambios de código, refactorizaciones, implementaciones en vivo y corrección de errores | Enunciado, *Uso de Inteligencia Artificial* y *Sustentación* | Código que el autor entienda y pueda cambiar sin ayuda; ensayar un cambio en vivo antes de sustentar |
| `CN-06` | Proceso | Se permite usar IA, pero todo debe poder **comprenderse, explicarse y sustentarse** | Enunciado, *Uso de Inteligencia Artificial* | Cada decisión queda escrita con su porqué (`DECISIONES.md` y los ADR); nada entra al repositorio sin que el autor lo pueda explicar |
| `CN-07` | Proceso | **Exclusividad:** cada persona tiene un solo rol activo | Enunciado, *Restricciones organizacionales* | Regla del dominio que se valida al asignar un rol; sostiene `RF-006` |
| `CN-08` | Proceso | **Unicidad:** un Rector y un Coordinador Académico activos en la universidad, un Coordinador de Facultad activo por facultad y un Coordinador de Programa activo por programa | Enunciado, *Restricciones organizacionales* | Regla del dominio que se valida al activar un cargo; se diseña junto con `RF-007` |
| `CN-09` | Proceso | **Cadena de aprobaciones fija:** al Estudiante lo aprueba el Coordinador de Programa; al Coordinador de Programa, el de Facultad; al de Facultad, el Académico; al Académico, el Rector; al Rector, el Administrador, y al Administrador, otro Administrador | Enunciado, *Flujo de aprobaciones* | Se diseña con `RF-007`; el primer Administrador entra por datos semilla (`D-14`). La tabla del enunciado no dice quién aprueba a un Docente: lo aprueba el Coordinador de Programa de cualquiera de los programas donde enseña (`D-32`) |

---

## 4 · Restricciones técnicas `CN-10` …

**Tipos:** `Técnica impuesta` (viene del enunciado o de un tercero, y no se negocia) · `Técnica
adoptada` (la decide el autor). **Estado:** `DENTRO` · `EN DUDA` · `CERRADO`.

### 4.1 Impuestas por el enunciado

| ID | Tipo | Restricción técnica | Justificación | Origen | Estado | Plan de acción |
|---|---|---|---|---|---|---|
| `CN-10` | Técnica impuesta | Toda la documentación se entrega en Markdown (`.md`) | Formato exigido | Enunciado, *Información general* y *Entregables* | DENTRO | Diagramas en Mermaid dentro de los `.md`, o draw.io exportado a PNG en `imagenes/` |
| `CN-11` | Técnica impuesta | CI, CD, análisis estático con Sonar y escaneo de vulnerabilidades con Trivy desde la línea base | DevSecOps obligatorio, con evidencia de cada uno | Enunciado, *DevSecOps obligatorio* y *Evidencias obligatorias*; `DRV-007` | DENTRO | Un pipeline de CI y uno de CD por repositorio; Sonar y Trivy corren en la CI de `codigo-testing` |
| `CN-12` | Técnica impuesta | Un repositorio de Front-End y otro de Back-End | Son entregables obligatorios por separado | Enunciado, *Entregables* | DENTRO | La documentación vive en `docs/` del back, y el README del front enlaza a ella |
| `CN-13` | Técnica impuesta | La solución debe estar publicada en una **URL pública** donde el docente use el sistema y valide el flujo de publicar un Kudo | Condición de entrega | Enunciado, *Publicación obligatoria* | DENTRO | Desplegar desde el primer esqueleto y probar la URL desde un celular |
| `CN-14` | Técnica impuesta | El correo, WhatsApp y el push en tiempo real deben funcionar de verdad, con evidencia | Los exigen el flujo obligatorio y las evidencias | Enunciado, *Funcionalidad obligatoria de línea base* y *Evidencias obligatorias*; `DRV-003` | DENTRO | Un spike por canal antes de escribir su ADR |
| `CN-15` | Técnica impuesta | La solución debe demostrar **Clean Architecture**: capas, dependencias, responsabilidades y reglas | Criterio de evaluación | Enunciado, *Clean Architecture* | DENTRO | Una prueba de arquitectura que falle si una capa interna depende de una externa |
| `CN-16` | Técnica impuesta | Pruebas automatizadas **unitarias, de integración y de punta a punta** | Pirámide de pruebas exigida | Enunciado, *Pirámide de pruebas* | DENTRO | Las tres corren en la CI |
| `CN-17` | Técnica impuesta | Diseño ***mobile first*** | ~90 % de los usuarios entra desde el celular | Enunciado, `DRV-001` | DENTRO | Se diseña y se prueba primero en celular (`D-21`) |

### 4.2 Impuestas por terceros

| ID | Tipo | Restricción técnica | Justificación | Origen | Estado | Plan de acción |
|---|---|---|---|---|---|---|
| `CN-18` | Técnica impuesta | WhatsApp para empresas solo deja iniciar una conversación con **plantillas aprobadas**, y un número de prueba solo envía a unos pocos números verificados | Son reglas del proveedor, no del proyecto | `CN-14`, `RF-002` | EN DUDA | Confirmarlo en el spike de WhatsApp; solicitar la plantilla el primer día y registrar los números de la demo |
| `CN-19` | Técnica impuesta | `[:W:]` La suscripción Azure for Students tiene límites: cuando el crédito se agota, deshabilita los servicios; una política de la suscripción solo deja desplegar en unas pocas regiones, que cambian de un estudiante a otro; las cuotas son bajas, y Azure bloquea el correo saliente por el puerto 25 | Son reglas del proveedor y consecuencia de `CN-03` | `D-53` | EN DUDA | Confirmarlo en `SPK-04`: revisar las regiones permitidas antes de crear nada y poner todo en una misma región; alerta de presupuesto desde el primer día; enviar el correo por la API HTTP del proveedor. Con capas gratuitas (plan B) vuelve el arranque en frío, y se compensa con el ping de salud (`D-31`). **`SPK-04` (13-nov-2026) confirmó los límites:** la política permite cinco regiones (`brazilsouth`, `westus2`, `canadacentral`, `francecentral` y `spaincentral`); `westus2` no deja crear PostgreSQL, y la cuota de App Service B1 es 0. Se resolvió con la API, la base y los archivos en `canadacentral`, la API en Container Apps y el front en `westus2`. Sigue EN DUDA hasta que GitHub Actions despliegue (paso 10 del spike) |

### 4.3 Adoptadas

**Lenguaje y frameworks:** no se adopta ninguna restricción previa. Cada tecnología se elige por
mérito en su ADR, comparando opciones (`D-18`).

| ID | Tipo | Restricción técnica | Justificación | Origen | Estado | Plan de acción |
|---|---|---|---|---|---|---|
| `CN-20` | Técnica adoptada | El cliente es una **PWA**: una sola URL pública que se puede instalar en la pantalla de inicio, sin tiendas de aplicaciones | Cubre `DRV-001` y `CN-13` con un solo cliente | `D-17` | DENTRO | Manifiesto y service worker; probar la instalación en Android y en iPhone |
| `CN-21` | Técnica adoptada | Funciona en **cualquier navegador moderno, con el celular como prioridad**: el diseño y las pruebas principales se hacen en celular, el escritorio se verifica con la prueba de punta a punta en Chromium y Safari se prueba a mano | Cubre `DRV-001` sin dejar a nadie por fuera | `D-21` | DENTRO | La prueba de punta a punta corre en la CI con Chromium en tamaño de celular y de escritorio |
| `CN-22` | Técnica adoptada | `[:W:]` La base de datos, el almacenamiento de archivos y el proveedor de identidad son **servicios gestionados**: la base y los archivos en Azure, pagados con el crédito o con su capa gratuita de 12 meses; el IdP, en la capa gratuita de su proveedor | Una sola persona no puede operar servidores, y autohospedarlos gastaría el crédito en memoria e instancias | `D-19`, `D-53`, `D-54`, `CN-02`, `CN-03` | DENTRO | `[!]` En local se reemplazan por contenedores que hablan el mismo protocolo: ver `T-5` |
| `CN-23` | Técnica adoptada | Todo el sistema se levanta en un computador local con **un solo comando** (Docker Compose); los servicios externos se reemplazan por dobles o cuentas de prueba | Un manual de puesta a punto simple y paridad entre entornos (Twelve-Factor, factor X) | `D-20` | DENTRO | El manual de puesta a punto se escribe sobre ese comando |
| `CN-24` | Técnica adoptada | La línea base corre en **una sola instancia** de la API, que no guarda estado entre peticiones para poder escalar después | El tiempo y el presupuesto de una materia (`T-1` y `T-3` reducidas) | `D-15`, `CN-03` | DENTRO | El escalado horizontal y el relevo de push entre instancias se documentan como evolución |
| `CN-25` | Técnica adoptada | La CI exige al menos **70 % de cobertura** en el dominio y los casos de uso; los adaptadores se cubren con pruebas de integración, sin umbral | Asegura las reglas del negocio sin gastar el plazo probando infraestructura | `D-30`, `CN-16` | DENTRO | El umbral se configura en la CI, y Sonar lo reporta |

---

## 5 · Drivers `DRV-001` … `DRV-009`

Los siete primeros los da el enunciado y son **obligatorios**: no se votan ni se quitan, aunque el
atributo que mueven quede abajo en la priorización de la §6. `DRV-008` y `DRV-009` los identifica el
autor a partir de sus decisiones (`D-25`). Los atributos usan la lista de la §6 (`D-23`) y la
traducción de `D-24`.

| DRV | Origen | Qué exige | Atributos de calidad que mueve | Funcionalidades y restricciones | Escenarios |
|---|---|---|---|---|---|
| `DRV-001` Movilidad | Enunciado · **obligatorio** | ~90 % de los usuarios entra desde el celular; la estrategia de diseño es *mobile first* | Experiencia de usuario | `RF-005`; `CN-17`, `CN-20`, `CN-21` | `ESC-13`, `ESC-16` |
| `DRV-002` Alta demanda de lectura | Enunciado · **obligatorio** | La mayoría de las operaciones son consultas, y la solución debe justificar cómo responde a ellas | Rendimiento · Escalabilidad | `RF-005`, `RF-008`, `RF-009`; `CN-24` | `ESC-13` |
| `DRV-003` Notificaciones multicanal | Enunciado · **obligatorio** | Correo electrónico, WhatsApp y push | Disponibilidad · Interoperabilidad · Confiabilidad | `RF-002`, `RF-003`; `CN-14`, `CN-18` | `ESC-04`, `ESC-05`, `ESC-08`, `ESC-14` |
| `DRV-004` Adjuntos | Enunciado · **obligatorio** | Los Kudos soportan carga de archivos, con estrategias de almacenamiento, seguridad, recuperación y trazabilidad | Escalabilidad (almacenamiento) · Seguridad (seguridad) · Confiabilidad (recuperación) · Cumplimiento (trazabilidad) | `RF-004` | `ESC-03`, `ESC-06` |
| `DRV-005` Seguridad | Enunciado · **obligatorio** | Mecanismos de seguridad apropiados en la arquitectura | Seguridad | `RF-006`, `RF-007` | `ESC-01`, `ESC-02` |
| `DRV-006` Observabilidad | Enunciado · **obligatorio** | Monitoreo, métricas, trazas y auditoría | Capacidad para ser Soportado (logs y trazas) · Capacidad para ser Administrado (métricas y monitoreo) · Cumplimiento (auditoría) | `RF-003` | `ESC-09`, `ESC-12` |
| `DRV-007` Automatización | Enunciado · **obligatorio** | Capacidades DevSecOps desde la línea base | Desplegabilidad · Facilidad de prueba · Seguridad · Disponibilidad | `CN-11`, `CN-16`, `CN-25` | `ESC-15` |
| `DRV-008` Permisos sin herencia | Autor (`D-25`) | Los permisos se asignan por rol dentro de una jerarquía que no los hereda: el Administrador está arriba y aun así no puede vetar | Seguridad | `RF-006`, `RF-007`, `RF-009`; `CN-07`, `CN-08`, `CN-09` | `ESC-01` |
| `DRV-009` Datos personales | Autor (`D-25`) | El correo y el celular están bajo la Ley 1581: consentimiento, uso limitado y supresión por anonimización | Cumplimiento · Seguridad | `RF-003`, `RF-007`; `CN-04` | `ESC-10`, `ESC-11` |

**Mantenibilidad no sale de ningún driver.** La exigen `CN-05` (cambios en vivo durante la
sustentación) y `CN-15` (Clean Architecture), y por eso está en la lista de la §6.

---

## 6 · Atributos de calidad

### 6.0 La lista: 13 atributos del ejercicio del curso

Se parte de los 19 atributos del ejercicio de atributos de calidad del curso, y se usan los 13 que
pesan en UCO Kudos (`D-23`). El número es el de su definición en el ejercicio; la definición está
resumida. La tabla va en el orden final de la §6.1.

| Atributo | En inglés | N.º | Qué significa | Por qué pesa en UCO Kudos |
|---|---|:--:|---|---|
| Seguridad | Security | 4 | Proteger los datos de accesos no autorizados sin negarle el acceso a quien sí lo tiene | `DRV-005`, `DRV-008`, `DRV-009`, `D-09` |
| Confiabilidad | Reliability | 13 | Seguir operando en el tiempo y entregar información correcta | `D-01`, `DRV-003`, `DRV-004` |
| Disponibilidad | Availability | 7 | Estar listo para funcionar en el momento en que se necesita | `DRV-003`, `DRV-007`; los límites de la suscripción (`CN-19`) |
| Cumplimiento | Compliance | 17 | Poder ser auditado en configuración, entornos y regulaciones que lo alcanzan | Ley 1581 (`CN-04`), auditoría (`D-08`), `DRV-006`, `DRV-009` |
| Capacidad para ser Soportado | Supportability | 18 | Dar información útil para encontrar y resolver incidentes | `DRV-006` (logs y trazas) |
| Rendimiento | Performance | 8 | Cumplir los requisitos de tiempo | `DRV-002`, `D-06` |
| Interoperabilidad | Interoperability | 5 | Intercambiar información con otros sistemas por medio de interfaces | Correo, WhatsApp e IdP (`DRV-003`, `D-09`) |
| Desplegabilidad | Deployability | 12 | Desplegar en producción de forma rápida, repetible y con pocos errores | `CN-11`, `CN-13`, `DRV-007` |
| Mantenibilidad | Maintainability | 16 | Incorporar cambios por funcionalidad nueva o por corrección de errores | `CN-05`, `CN-15` |
| Experiencia de usuario | User Experience | 3 | Qué tan fácil le resulta al usuario cumplir su tarea y cuánto lo ayuda el sistema | `DRV-001` |
| Facilidad de prueba | Testability | 1 | Comprobar el sistema y sus componentes con criterios de aceptación, de la forma más automática posible | `CN-16`, `CN-25`, `DRV-007` |
| Capacidad para ser Administrado | Manageability | 14 | Administrar y configurar el sistema con instrumentación expuesta a monitoreo y análisis | `DRV-006` (métricas y monitoreo) |
| Escalabilidad | Scalability | 9 | Aumentar recursos lógicos o físicos para mejorar de forma medible su ejecución | `DRV-002`, `DRV-004`; meta de diseño (`D-05`, `D-15`) |

**Fuera de la lista.** Eficiencia (15), Accesibilidad (11), Integridad conceptual (6) y Flexibilidad
(2) pesan poco en UCO Kudos o quedan dentro de otros atributos. Reusabilidad (10) no aplica porque es
una sola aplicación, e Internacionalización (19) tampoco, porque es solo en español. «Capacidad»,
«Portabilidad» y «Capacidad para ser Auditado», que se usaron en FlorLogic, no están entre los 19 del
ejercicio: la auditoría queda dentro de Cumplimiento.

### 6.1 Priorización por actor (la que manda)

Cada actor reparte del 1 al 13, sin repetir (menor = más prioritario): 91 puntos por actor y 364 en
total. Votan cuatro actores (`D-33`). No hubo actores reales: el autor votó en nombre de cada uno,
eligiendo el orden que mejor representa lo que el enunciado dice de él (`D-34`).

| Actor | Quién es | Su orden, en una frase |
|---|---|---|
| Comunidad académica | Estudiantes y docentes, casi siempre desde el celular | «Lo que se ve primero»: la experiencia y la velocidad |
| Directivos | Rector y coordinadores: aprueban, moderan y administran personas | «Control y evidencia»: los permisos y la auditoría |
| Administrador del sistema | Administra administradores y rectores | «Operar sin sorpresas»: diagnosticar, monitorear y desplegar |
| Equipo de desarrollo y operación | El autor: construye, prueba, despliega y sustenta | «Lo que evalúa el docente»: CD, pruebas, Sonar, Trivy y el flujo funcionando |

| # | Atributo | Comunidad | Directivos | Administrador | Equipo | Total | % |
|---|---|:--:|:--:|:--:|:--:|:--:|:--:|
| **1** | **Seguridad** | 6 | 1 | 4 | 3 | **14** | 3,85 % |
| **2** | **Confiabilidad** | 4 | 3 | 6 | 4 | **17** | 4,67 % |
| **3** | **Disponibilidad** | 3 | 4 | 3 | 8 | **18** | 4,95 % |
| **4** | **Cumplimiento** | 7 | 2 | 9 | 10 | **28** | 7,69 % |
| **5** | **Capacidad para ser Soportado** | 10 | 11 | 1 | 7 | **29** | 7,97 % |
| 6 | Rendimiento | 2 | 6 | 11 | 11 | 30 | 8,24 % |
| 7 | Interoperabilidad | 5 | 7 | 12 | 6 | 30 | 8,24 % |
| 8 | Desplegabilidad | 12 | 13 | 5 | 1 | 31 | 8,52 % |
| 9 | Mantenibilidad | 9 | 10 | 7 | 5 | 31 | 8,52 % |
| 10 | Experiencia de usuario | 1 | 5 | 13 | 13 | 32 | 8,79 % |
| 11 | Facilidad de prueba | 11 | 12 | 8 | 2 | 33 | 9,07 % |
| 12 | Capacidad para ser Administrado | 13 | 9 | 2 | 9 | 33 | 9,07 % |
| 13 | Escalabilidad | 8 | 8 | 10 | 12 | 38 | 10,44 % |
| | **Total** | 91 | 91 | 91 | 91 | 364 | 100 % |

> ## **Los drivers de calidad: Seguridad · Confiabilidad · Disponibilidad · Cumplimiento · Capacidad para ser Soportado.**

**Cómo leer el resultado.**
- **Seguridad gana por consenso, no por un solo voto.** Solo los Directivos la ponen primera, pero
  ningún actor la pone por debajo del puesto 6. A Confiabilidad le pasa lo mismo. Disponibilidad queda
  tercera aunque el Equipo la ponga en el puesto 8.
- **Manda la votación, no `D-01`** (`D-35`). La frase de la §1 sigue diciendo qué hace inútil el
  sistema; la votación dice que, cuando la seguridad choca con la confiabilidad, gana la seguridad.
- **Los drivers del enunciado siguen siendo obligatorios** aunque su atributo quede abajo. Rendimiento
  (6) y Escalabilidad (13) sostienen `DRV-002` y se cumplen igual: el puesto solo dice qué gana cuando
  dos atributos chocan.
- **Escalabilidad queda última**, y es coherente con `D-15`: los 800 simultáneos son una meta de
  diseño, no algo que la línea base tenga que demostrar.
- **Experiencia de usuario queda décima** a pesar de `DRV-001`. Es la divergencia más grande de la
  votación: la Comunidad la pone primera, y el Administrador y el Equipo la ponen última.

**Los tres empates, y cómo quedaron (`D-36`):**

| Empate | Puntos | Gana | Por qué |
|---|:--:|---|---|
| Rendimiento / Interoperabilidad | 30 | Rendimiento | Tres de los cuatro actores lo ponen por encima, y sostiene `DRV-002` |
| Desplegabilidad / Mantenibilidad | 31 | Desplegabilidad | Dos actores para cada uno; gana el que sale de un driver obligatorio (`DRV-007`) |
| Facilidad de prueba / Capacidad para ser Administrado | 33 | Facilidad de prueba | Dos actores para cada uno; gana la que moldea la arquitectura y sale de la pirámide de pruebas obligatoria (`CN-16`) |

### 6.2 Trade-offs: qué se sacrifica cuando dos atributos chocan

**Regla:** cuando dos atributos chocan, gana el de mejor puesto en la §6.1, y las restricciones de las
§3 y §4 mandan sobre cualquier atributo. La única excepción es la duración de la sesión, y está
justificada abajo.

| Choque | Gana | Se sacrifica | Por qué | Deriva de |
|---|---|---|---|---|
| Confiabilidad del Kudo contra la entrega inmediata de los avisos | Confiabilidad y Disponibilidad: el Kudo se publica aunque falle un canal | Que el aviso llegue en el mismo instante: puede llegar después de un reintento | Un Kudo perdido deja inútil el sistema; un aviso que tarda, no | `D-01`, `D-02` |
| Rendimiento de lectura contra frescura del muro | Rendimiento: el muro se lee de una caché | Un Kudo nuevo tarda unos segundos en aparecer en el muro de los demás | Es la operación que más se repite. No choca con la Confiabilidad: el muro nunca muestra información incorrecta, solo tarda unos segundos en mostrar la nueva | `D-06`, `DRV-002` |
| Seguridad contra Rendimiento al revocar un acceso | Seguridad, con un margen de minutos | La revocación inmediata: a quien le quitan el rol puede actuar hasta que vence su token, unos 15 minutos | Revocar al instante obligaría a consultar al IdP en cada lectura y castigaría al muro; 15 minutos es el margen que se acepta | `D-27` |
| Experiencia de usuario contra Seguridad en la duración de la sesión | Experiencia de usuario | Volver a iniciar sesión: la sesión dura hasta 30 días sin uso, también en un celular compartido | `[!]` Es el único choque donde gana el atributo de peor puesto (10 contra 1). Se acepta porque el acceso sigue controlado: los tokens cortos y revocables de `D-27` siguen vigentes | `D-28`, `DRV-001` |
| Rendimiento y experiencia en el celular contra fidelidad de los adjuntos | Rendimiento y Experiencia de usuario | El muro muestra miniaturas; para ver el original hay que abrir el Kudo | Un Kudo puede pesar hasta 15 MB, y casi todos entran con datos móviles | `D-29`, `DRV-001`, `DRV-004` |
| `[:W:]` Disponibilidad contra crédito finito | Disponibilidad: la instancia queda siempre encendida, pagada con el crédito | Una parte del crédito cada mes; si se agotara, la suscripción se deshabilita y la URL deja de responder | Con el crédito, el presupuesto ya no obliga a dormir el servidor. La meta de gasto y la alerta de presupuesto protegen la semana de evaluación. Antes de `D-53` ganaba el costo cero, compensado con un ping de salud | `D-53`, `D-31`, `T-9` |
| Escalabilidad contra tiempo y costo | El tiempo y el costo (`CN-01`, `CN-03`) | Una sola instancia: la línea base no demuestra los 800 simultáneos | Escalabilidad quedó última en la votación | `D-05`, `D-15` |
| Facilidad de prueba contra plazo | El plazo (`CN-01`), con un piso de calidad | La cobertura solo se exige en el dominio y los casos de uso (70 %) | Ahí están las reglas que más cuesta romper | `D-30` |
| Cumplimiento consigo mismo: borrar datos personales contra una auditoría que no se borra | Los dos, por anonimización | La auditoría deja de identificar a la persona que pidió el borrado | La Ley 1581 y `D-08` tienen que cumplirse a la vez | `D-22`, `T-4` |

---

## 7 · Preguntas de caracterización `P-nn`

Preguntas de sí o no por atributo, en el orden de la votación de la §6.1. Convierten cada atributo en
una necesidad concreta. **Respuesta:** `SÍ` o `NO`, con la sección del enunciado o la decisión de
donde sale. Ninguna quedó sin responder.

**Cómo se eligió el Top (`D-37`).** Entran todas las preguntas de los 5 drivers de calidad, al menos
una por cada DRV y una por cada paso del flujo obligatorio de publicar. No se hizo votación de puntos:
con una sola persona votando por los cuatro actores, una regla explícita es más honesta. Quedan **16
preguntas en el Top**, y cada una se vuelve un escenario de la §8. Otras 5 quedan dentro de un
escenario del Top porque miden lo mismo, y las 9 restantes no generan escenario.

| # | Atributo | Pregunta | Respuesta | De dónde sale | Top |
|---|---|---|:--:|---|---|
| `P-01` | Seguridad | ¿Se necesita que cada acción la haga solo el rol que el enunciado autoriza, aunque otro rol esté más arriba en la jerarquía? | SÍ | *Administración de categorías*, *Moderación*; `DRV-008` | `ESC-01` |
| `P-02` | Seguridad | ¿Se necesita que quien pierde su rol o su cuenta deje de actuar en un tiempo acotado? | SÍ | `D-27` | `ESC-02` |
| `P-03` | Seguridad | ¿Se necesita que un archivo de un tipo no permitido o demasiado grande nunca llegue a guardarse? | SÍ | *Archivos adjuntos*; `D-11` | `ESC-03` |
| `P-04` | Confiabilidad | ¿Se necesita que un Kudo publicado no se pierda aunque falle un canal de aviso? | SÍ | `D-01`, `D-02` | `ESC-04` |
| `P-05` | Confiabilidad | ¿Se necesita que cada aviso termine entregado, fallido u omitido, y nunca quede pendiente para siempre? | SÍ | *Funcionalidad obligatoria de línea base* (pasos 2 a 5); `D-42` | `ESC-05` |
| `P-06` | Confiabilidad | ¿Se necesita recuperar los Kudos y sus adjuntos si se pierde la base de datos o el almacenamiento? | SÍ | *Archivos adjuntos* (estrategia de recuperación); `D-40` | `ESC-06` |
| `P-07` | Disponibilidad | ¿Se necesita que la plataforma responda cuando alguien entra, sin esperar a que despierte? | SÍ | *Publicación obligatoria*; `D-31` | `ESC-07` |
| `P-08` | Disponibilidad | ¿Se necesita poder seguir publicando aunque WhatsApp o el correo estén caídos? | SÍ | `D-02` | `ESC-08` |
| `P-09` | Cumplimiento | ¿Se necesita saber quién hizo cada acción, cuándo y con qué resultado, sin que nadie pueda alterarlo? | SÍ | *Funcionalidad obligatoria de línea base* (paso 5); `D-08` | `ESC-09` |
| `P-10` | Cumplimiento | ¿Se necesita borrar los datos personales de quien lo pida sin romper la auditoría? | SÍ | `CN-04`; `D-22` | `ESC-10` |
| `P-11` | Cumplimiento | ¿Se necesita que ningún usuario pueda ver el celular de otro? | SÍ | `D-16` | `ESC-11` |
| `P-12` | Capacidad para ser Soportado | ¿Se necesita encontrar rápido qué pasó con un aviso que no llegó? | SÍ | `DRV-006` | `ESC-12` |
| `P-13` | Capacidad para ser Soportado | ¿Se necesita seguir de punta a punta los registros de cada publicación? | SÍ | `DRV-006` (trazas) | Dentro de `ESC-12` |
| `P-14` | Rendimiento | ¿Se necesita que el muro responda rápido en hora pico desde el celular? | SÍ | `DRV-001`, `DRV-002` | `ESC-13` |
| `P-15` | Rendimiento | ¿Se necesita que publicar responda rápido aunque los avisos tarden? | SÍ | `D-02` | Dentro de `ESC-04` |
| `P-16` | Rendimiento | ¿Se necesita que un Kudo nuevo aparezca en pocos segundos en el muro de los demás? | SÍ | `D-06` | Dentro de `ESC-13` |
| `P-17` | Rendimiento | ¿Se necesita que el push llegue al instante si el destinatario tiene la aplicación abierta? | SÍ | *Funcionalidad obligatoria de línea base* (paso 4); `D-04` | `ESC-14` |
| `P-18` | Interoperabilidad | ¿Se necesita cambiar de proveedor de correo o de WhatsApp sin tocar las reglas del negocio? | SÍ | `D-38` | No |
| `P-19` | Interoperabilidad | ¿Se necesita cambiar de proveedor de identidad solo con configuración? | SÍ | `D-09`, `D-38` | No |
| `P-20` | Desplegabilidad | ¿Se necesita que un cambio aprobado llegue a producción sin pasos manuales? | SÍ | *DevSecOps obligatorio*; `DRV-007` | `ESC-15` |
| `P-21` | Desplegabilidad | ¿Se necesita volver a la versión anterior si un despliegue sale mal? | SÍ | `D-38` | Dentro de `ESC-15` |
| `P-22` | Mantenibilidad | ¿Se necesita hacer en poco tiempo un cambio pedido en vivo durante la sustentación? | SÍ | *Uso de Inteligencia Artificial*; `CN-05` | No |
| `P-23` | Mantenibilidad | ¿Se necesita agregar un canal de aviso nuevo sin modificar el caso de uso de publicar? | SÍ | `D-38` | No |
| `P-24` | Experiencia de usuario | ¿Se necesita publicar un Kudo desde el celular en pocos pasos? | SÍ | `DRV-001` | `ESC-16` |
| `P-25` | Experiencia de usuario | ¿Se necesita poder instalar la aplicación en la pantalla de inicio del celular? | SÍ | `D-17` | No |
| `P-26` | Facilidad de prueba | ¿Se necesita probar las reglas del dominio sin base de datos ni servicios externos? | SÍ | *Clean Architecture*, *Pirámide de pruebas*; `D-30` | No |
| `P-27` | Facilidad de prueba | ¿Se necesita probar el flujo de publicar de punta a punta en la CI sin enviar mensajes reales? | SÍ | *Pirámide de pruebas*; `D-20` | No |
| `P-28` | Capacidad para ser Administrado | ¿Se necesita ver cuántos Kudos se publican y cuántos avisos fallan por canal? | SÍ | `DRV-006`; `D-42` | No |
| `P-29` | Capacidad para ser Administrado | ¿Se necesita enterarse cuando la API se cae? | SÍ | `D-31` | Dentro de `ESC-07` |
| `P-30` | Escalabilidad | ¿Se necesita que la arquitectura pueda pasar a varias instancias sin reescribirse? | SÍ | `D-15` | No |

`[!]` **Cuatro atributos quedan sin escenario propio:** Interoperabilidad, Mantenibilidad, Facilidad
de prueba y Escalabilidad. No es un olvido: sus preguntas quedaron fuera del Top por la regla de
`D-37`. Mantenibilidad y Facilidad de prueba se exigen como restricciones (`CN-05`, `CN-15`, `CN-25`);
Interoperabilidad, con las capacidades de cambio de `D-38`; y Escalabilidad es una meta de diseño
(`D-15`) dentro de `ESC-13`. Las preguntas que dicen «No» siguen siendo necesidades: se cumplen en el
diseño, aunque no tengan una medida propia.

---

## 8 · Escenarios de calidad `ESC-nn`

Seis partes (Bass, Clements y Kazman): **fuente, estímulo, artefacto, entorno, respuesta y medida de
respuesta**, agrupados **por atributo** y en el orden de la §6.1. El número del escenario es el puesto
de su pregunta en el Top de la §7.

Los artefactos se nombran sin tecnología: la tecnología la eligen los ADR, y estos escenarios son la
medida con la que se acepta o se descarta cada opción. Todas las medidas salen del enunciado o de una
decisión (`D-27`, `D-39` a `D-44`).

### 8.1 Seguridad

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-01` | Un usuario con sesión iniciada, por ejemplo un Administrador o un Docente | intenta una acción que su rol no permite, como vetar un Kudo o administrar categorías | la autorización de la API | operación normal | rechaza la acción, no cambia nada y deja el intento en la auditoría | 100 % de las acciones no autorizadas rechazadas y auditadas; 0 cambios hechos por un rol sin permiso, verificado con una prueba automatizada por regla de permisos | `P-01`; `DRV-005`, `DRV-008`; `RF-006` |
| `ESC-02` | Quien administra a una persona según la jerarquía | le quita el rol o le desactiva la cuenta mientras ella tiene la sesión abierta | la sesión y los tokens de esa persona | operación normal | la persona deja de poder actuar en cuanto vence su token, y su sesión ya no se renueva | acceso perdido en ≤ 15 min; 0 renovaciones de sesión después de la revocación | `P-02`; `D-27`, `D-28`; `RF-006` |
| `ESC-03` | Un usuario que publica un Kudo | adjunta un archivo de un tipo no permitido, de más de 5 MB o con la extensión cambiada para que parezca una imagen | la validación de adjuntos y el almacén de archivos | operación normal; en local, con el antivirus activo | rechaza el archivo antes de guardarlo y dice por qué; en local, además, pone en cuarentena el archivo de prueba EICAR | 100 % de los archivos no permitidos rechazados, incluso con la extensión cambiada; 0 guardados; en local, EICAR detectado el 100 % de las veces | `P-03`; `DRV-004`; `D-11`, `D-41`; `RF-004` |

### 8.2 Confiabilidad

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-04` | Un usuario | publica un Kudo justo cuando uno de los canales de aviso está fallando | el caso de uso de publicar y su registro de avisos pendientes | operación normal, con un canal externo fallando | guarda el Kudo, deja los avisos pendientes para enviarlos en segundo plano y responde sin esperar a los canales | 0 Kudos perdidos; respuesta en ≤ 2 s sin adjuntos | `P-04`, `P-15`; `D-01`, `D-02`; `RF-001` |
| `ESC-05` | El despachador de avisos | envía el correo y el WhatsApp de un Kudo recién publicado | el despachador de avisos y su política de reintentos | operación normal, con fallos ocasionales de los proveedores | entrega cada aviso o lo reintenta con espera creciente; si agota los intentos, lo marca como fallido, lo audita y lo cuenta en las métricas | correo y WhatsApp entregados en ≤ 1 min en operación normal; hasta 5 intentos en 30 min; 100 % de los avisos en un estado final (entregado, fallido u omitido) al terminar los reintentos | `P-05`; `D-02`, `D-42`; `RF-002`, `RF-003` |
| `ESC-06` | Una falla del proveedor | borra o corrompe la base de datos o el almacén de adjuntos | las copias de seguridad de la base y de los adjuntos | desastre: se perdió el almacenamiento principal | restaura la base y los adjuntos desde la última copia diaria, guardada fuera del proveedor principal | ≤ 24 h de información perdida; recuperación completa en ≤ 4 h; restauración probada al menos una vez antes de la entrega | `P-06`; `DRV-004`; `D-40`; `RF-004` |

### 8.3 Disponibilidad

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-07` | Un usuario o el docente | entra a la URL pública después de un rato sin uso | la API desplegada en una instancia siempre encendida y el monitor de salud | semana de evaluación | responde sin arranque en frío, porque la instancia no se duerme; si se cae, el monitor lo detecta en su siguiente comprobación | 99 % de disponibilidad en la semana de evaluación, medida por el monitor (como máximo unas 1,7 h fuera de servicio) | `P-07`, `P-29`; `D-31`, `D-43`, `D-53`; `CN-19` |
| `ESC-08` | El proveedor de WhatsApp o el de correo | deja de responder durante un rato largo | el caso de uso de publicar y el despachador de avisos | operación con un proveedor externo caído | se sigue publicando con normalidad; los avisos del canal caído se reintentan y, si se agotan los intentos, quedan como fallidos y auditados | 100 % de las publicaciones aceptadas mientras el proveedor está caído; 0 Kudos perdidos; 0 avisos sin estado final | `P-08`; `D-02`, `D-42`; `RF-001`, `RF-002` |

### 8.4 Cumplimiento

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-09` | Cualquier usuario o proceso del sistema | hace una acción que cambia algo: publica, avisa, veta, aprueba o cambia una categoría | el registro de auditoría | operación normal | registra quién, qué, cuándo y el resultado, y no permite editar ni borrar registros | 100 % de las acciones registradas; 0 registros modificados o borrados; la historia completa de un Kudo se consulta en ≤ 2 s | `P-09`; `DRV-006`; `D-08`; `RF-003` |
| `ESC-10` | Una persona de la comunidad | pide que se borren sus datos personales | la cuenta de la persona y el registro de auditoría | operación normal | elimina su nombre, correo y celular de la cuenta; la auditoría queda intacta porque solo guarda identificadores | solicitud atendida en ≤ 15 días hábiles (plazo de la Ley 1581 para reclamos); 0 datos personales dentro de la auditoría | `P-10`; `DRV-009`; `D-22`; `CN-04` |
| `ESC-11` | Un usuario con sesión iniciada | consulta el muro, un Kudo, un perfil o cualquier otra respuesta de la API | las respuestas de la API | operación normal | nunca incluye el celular de otra persona | 0 respuestas con el celular de otro usuario, verificado con una prueba automatizada sobre todas las consultas | `P-11`; `DRV-009`; `D-16`; `CN-04` |

### 8.5 Capacidad para ser Soportado

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-12` | El equipo de desarrollo y operación | recibe el reporte de que el destinatario de un Kudo no recibió su aviso | los registros, las trazas y la auditoría de esa publicación | operación normal, con el sistema desplegado | con el identificador del Kudo encuentra la traza de la publicación y el resultado de cada canal | causa identificada en ≤ 5 min; 100 % de las publicaciones con un identificador que une sus registros, trazas y auditoría | `P-12`, `P-13`; `DRV-006`; `RF-003` |

### 8.6 Rendimiento

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-13` | 50 usuarios simultáneos desde el celular (meta de diseño: 800) | consultan el muro y lo recorren deslizando | la consulta del muro y su caché de lectura | operación normal en la URL pública, hora pico | entrega cada bloque del muro desde la caché; un Kudo nuevo aparece para los demás en cuanto se invalida la caché | p95 ≤ 1 s por bloque con 50 simultáneos; un Kudo nuevo visible en ≤ 5 s. Los 800 se justifican por diseño: API sin estado y escalado horizontal documentado | `P-14`, `P-16`; `DRV-001`, `DRV-002`; `D-05`, `D-06`, `D-15`, `D-39`; `RF-005` |
| `ESC-14` | Un usuario | publica un Kudo para alguien que tiene la aplicación abierta | el canal en vivo | operación normal | el destinatario ve el aviso en pantalla sin recargar; si no está conectado, el push se omite y se audita | push recibido en ≤ 2 s desde la publicación; 100 % de los push omitidos registrados en la auditoría | `P-17`; `DRV-003`; `D-04`; `RF-002` |

### 8.7 Desplegabilidad

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-15` | El equipo de desarrollo y operación | aprueba un pull request a `main` | el pipeline de despliegue continuo | operación normal | construye, verifica y despliega la nueva versión sin pasos manuales; si sale mal, vuelve a la versión anterior sin reconstruirla | ≤ 15 min desde el merge hasta producción; 0 pasos manuales; vuelta a la versión anterior en ≤ 10 min | `P-20`, `P-21`; `DRV-007`; `D-38`; `CN-11` |

### 8.8 Experiencia de usuario

| ID | Fuente | Estímulo | Artefacto | Entorno | Respuesta | Medida de respuesta | Origen |
|---|---|---|---|---|---|---|---|
| `ESC-16` | Un estudiante o un docente desde su celular | publica un Kudo sin adjuntos | el formulario de publicar de la aplicación | operación normal, con datos móviles | permite elegir destinatario y categoría y escribir el contenido en una sola pantalla, y confirma la publicación | una sola pantalla; ≤ 60 s de principio a fin | `P-24`; `DRV-001`; `D-17`, `D-44`; `RF-001` |

### 8.9 Resumen

| Atributo | Puesto | Escenarios |
|---|:--:|---|
| Seguridad | 1 | `ESC-01`, `ESC-02`, `ESC-03` |
| Confiabilidad | 2 | `ESC-04`, `ESC-05`, `ESC-06` |
| Disponibilidad | 3 | `ESC-07`, `ESC-08` |
| Cumplimiento | 4 | `ESC-09`, `ESC-10`, `ESC-11` |
| Capacidad para ser Soportado | 5 | `ESC-12` |
| Rendimiento | 6 | `ESC-13`, `ESC-14` |
| Interoperabilidad | 7 | — |
| Desplegabilidad | 8 | `ESC-15` |
| Mantenibilidad | 9 | — |
| Experiencia de usuario | 10 | `ESC-16` |
| Facilidad de prueba | 11 | — |
| Capacidad para ser Administrado | 12 | Parte de `ESC-07` |
| Escalabilidad | 13 | Meta de diseño dentro de `ESC-13` |

**Los 5 drivers de calidad concentran 12 de los 16 escenarios**, y cada DRV tiene al menos uno (ver la
§5).

---

## 9 · Trazabilidad: escenario → ADR

Qué ADR responde a cada escenario. La última columna se llena al construir, con la prueba o la
evidencia que demuestra la medida.

| Escenario | Atributo | ADR que lo resuelve | Prueba o evidencia |
|---|---|---|---|
| `ESC-01` | Seguridad | `ADR-007`, `ADR-008` | Pendiente |
| `ESC-02` | Seguridad | `ADR-007`, `ADR-018` | Pendiente |
| `ESC-03` | Seguridad | `ADR-009` | Pendiente |
| `ESC-04` | Confiabilidad | `ADR-001`, `ADR-003` | Pendiente |
| `ESC-05` | Confiabilidad | `ADR-003`, `ADR-019`, `ADR-020` | Pendiente |
| `ESC-06` | Confiabilidad | `ADR-014`, `ADR-017`, `ADR-025` | Pendiente |
| `ESC-07` | Disponibilidad | `ADR-011`, `ADR-014`, `ADR-022` | Pendiente |
| `ESC-08` | Disponibilidad | `ADR-003` | Pendiente |
| `ESC-09` | Cumplimiento | `ADR-008` | Pendiente |
| `ESC-10` | Cumplimiento | `ADR-008` | Pendiente |
| `ESC-11` | Cumplimiento | `ADR-008` | Pendiente |
| `ESC-12` | Capacidad para ser Soportado | `ADR-024` | Pendiente |
| `ESC-13` | Rendimiento | `ADR-004`, `ADR-011`, `ADR-022` | Pendiente |
| `ESC-14` | Rendimiento | `ADR-003`, `ADR-021` | Pendiente |
| `ESC-15` | Desplegabilidad | `ADR-013`, `ADR-022`, `ADR-023` | Pendiente |
| `ESC-16` | Experiencia de usuario | `ADR-006`, `ADR-016` | Pendiente |
