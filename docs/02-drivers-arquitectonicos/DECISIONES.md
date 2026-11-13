# UCO Kudos — Decisiones del autor

> **v1.6 · 13-nov-2026 · Rondas 1 a 10, con el resultado de `SPK-04` sobre la ronda 10.**
> Lo que el enunciado no dice y hubo que decidir para escribir los drivers arquitectónicos. Cada
> decisión lleva un identificador `D-nn`, que los demás documentos citan como origen.
> Manda sobre esto: el enunciado y lo que diga el docente.

---

## 0 · Reglas

1. Los identificadores nunca se renumeran.
2. Una decisión no se borra. Si cambia, se marca `[:DD:]` y se escribe la nueva con otro número,
   diciendo a cuál reemplaza.
3. Si una decisión posterior cambia el **efecto** de otra sin cambiar lo decidido, se anota en la
   misma fila con `[:R:]` (reinterpretada) y la decisión que la reinterpreta.
4. Cada decisión dice qué se descartó y qué mueve en la arquitectura.

---

## 1 · Ronda 1 · 12-nov-2026 · Funcionalidades y calidad

Tres bloques de preguntas, de lo que más mueve la arquitectura a lo más puntual.

### 1.1 Lo que define el sistema

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-01` | ¿Qué haría que UCO Kudos ya no sirva? | **Que un Kudo se pierda o que su destinatario no se entere** | Un muro lento en el celular · alguien que actúa fuera de su rol · no poder demostrar qué pasó | Confiabilidad y Disponibilidad de publicar y avisar van primero; el Rendimiento de lectura, justo detrás. `[:R:]` Reinterpretada por `D-35`: la votación dejó primera a Seguridad, y segundas y terceras a Confiabilidad y Disponibilidad. La frase sigue siendo la de §1 de los drivers |
| `D-02` | Si falla un canal al publicar, ¿qué pasa con el Kudo? | **Se publica igual y el aviso se reintenta en segundo plano** | Publicar y solo auditar el fallo, con lo que se pierden avisos · no publicar (todo o nada) | Obliga a tener mensajería asíncrona con reintentos. `RF-001`, `RF-002`, `ESC-04`, `ESC-08` |
| `D-03` | ¿Quién puede ver los Kudos? | **Toda la comunidad con sesión iniciada: todos ven el mismo muro** | Solo su programa o su facultad · cualquiera sin sesión | Un solo muro para todos se cachea una vez (`DRV-002`), y los adjuntos solo se entregan con sesión. `RF-004`, `RF-005` |
| `D-04` | Para el push, ¿qué es estar «conectado»? | **Tener la aplicación abierta** | Push aunque la aplicación esté cerrada · las dos cosas | Canal en vivo propio, sin servicio externo de push. Si el destinatario no está conectado, el push se omite y queda auditado. `RF-002` |

### 1.2 Carga, costo y auditoría

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-05` | ¿Para qué tamaño se diseña? | **10.000 usuarios, con un pico de 800 simultáneos** | 1.000 con pico de 50 · 5.000 con pico de 300 · 20.000 con pico de 2.000 | Las medidas de los escenarios de rendimiento y escalabilidad. Ver `T-1`, `T-3` y `D-15` |
| `D-06` | ¿En cuánto tiempo aparece un Kudo nuevo en el muro de los demás? | **En pocos segundos** | Hasta un minuto (caché que expira sola) · siempre al instante (sin caché) | Caché que se invalida al publicar. La medida exacta se fija en el escenario. `RF-005` |
| `D-07` | ¿Cuánto se puede gastar en infraestructura? | `[:DD:]` **0 USD: solo capas gratuitas** | Hasta unos 10 USD al mes | Se acepta el arranque en frío como trade-off de costo. Es la restricción de negocio `CN-03`. Ver `T-1`, `T-2` y `D-31`. `[:DD:]` Derogada por `D-53`: el presupuesto pasa a ser el crédito de Azure for Students, y las capas gratuitas quedan como plan B |
| `D-08` | ¿Qué se audita, y se puede modificar? | **Toda acción que cambie algo: publicaciones, avisos, vetos, aprobaciones y categorías. Los registros solo se agregan** | Solo el flujo de publicar · toda acción, pero corregible | Registro de solo inserción. `RF-003`. Ver `T-4` |

### 1.3 Identidad, contenido y reglas

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-09` | ¿Quién se encarga de la identidad? | **Un proveedor de identidad (IdP): emite los tokens y la API solo los valida** | Identidad dentro de la propia API · inicio de sesión institucional | Una pieza más, y el registro de `RF-007` se reparte entre el IdP y UCO Kudos. Ver `T-2` |
| `D-10` | ¿Qué hace un veto? | **Oculta el contenido del muro y lo conserva, con quién lo vetó, cuándo y por qué** | Borrarlo | El contenido tiene un estado (visible o vetado), y un veto invalida la caché del muro. `RF-006`, `RF-009` |
| `D-11` | ¿Qué se puede adjuntar? | **Imágenes y PDF, hasta 5 MB por archivo y como máximo 3 por Kudo** | Una sola imagen de 2 MB · cualquier tipo hasta 10 MB | Validación de tipo y tamaño antes de guardar. `RF-004` |
| `D-12` | ¿Quién le puede dar un Kudo a quién? | **Cualquier persona de la comunidad a cualquier otra, sin importar programa o rol. Autor y destinatario distintos, y el destinatario con cuenta activa** | — | `RF-001` |
| `D-13` | ¿Cuál es la consulta principal y cómo se recorre? | **El muro: Kudos del más reciente al más antiguo, que se recorre deslizando, sin números de página. Las réplicas se cargan al abrir la conversación** | Un muro por páginas numeradas · réplicas cargadas junto con el muro | La API entrega el muro en bloques sucesivos, no por número de página. `RF-005`, `RF-008` |
| `D-14` | ¿Quién aprueba al primer administrador? | **Nadie: el primero se crea con datos semilla al desplegar** | — | `RF-007`, `CN-09` |

---

## 2 · Ronda 2 · 12-nov-2026 · Restricciones

Dos bloques: primero lo que reduce o abre tensiones, después cómo se construye y se opera.

### 2.1 Alcance, datos personales y tecnología

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-15` | ¿Cómo se reducen `T-1` y `T-3` con el tiempo y el presupuesto de una materia? | **La línea base corre en una sola instancia gratuita. Los 800 simultáneos de `D-05` quedan como meta de diseño: la API no guarda estado entre peticiones para poder escalar después, y el escalado y el relevo de push entre instancias se documentan como evolución** | Bajar la escala de diseño a lo que aguanta una instancia gratuita | `CN-24`. Reduce `T-1` y `T-3`. `[:R:]` Reinterpretada por `D-53`: la instancia única deja de ser gratuita y pasa a ser una instancia pequeña, siempre encendida, pagada con el crédito |
| `D-16` | ¿Cómo se trata la protección de datos personales? | **Como restricción legal con mecanismos (Ley 1581 de 2012): consentimiento al registrarse, uso de los datos solo para UCO Kudos, el celular nunca se muestra a otros usuarios y la demo usa datos de prueba** | Solo mencionar la ley · no considerarla | `CN-04`, `DRV-009`. Abre `T-4` |
| `D-17` | ¿Cómo llega UCO Kudos al celular? | **Una aplicación web instalable (PWA): una sola URL pública, sin tiendas de aplicaciones** | Web responsive sin instalación · aplicación nativa Android | `CN-20`, `DRV-001` |
| `D-18` | ¿Se limitan las tecnologías a las que ya domina el autor? | **No: cada tecnología se elige por mérito en su ADR, comparando opciones** | Limitarlas a Java/Spring y TypeScript · limitarlas solo en el back | Ninguna restricción previa de lenguaje ni de framework. Los ADR comparan candidatos |

### 2.2 Construcción y operación

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-19` | ¿Servicios gestionados o autohospedados? | **Gestionados con capa gratuita para la base de datos, el almacenamiento de archivos y el proveedor de identidad** | Autohospedarlos en contenedores · decidir caso a caso | `CN-22`. Inclina `T-2` hacia un IdP gestionado. Abre `T-5`. `[:R:]` Reinterpretada por `D-53` y `D-54`: siguen siendo gestionados, pero la base y los archivos pasan a Azure y se pagan con el crédito o con su capa gratuita de 12 meses; el IdP sigue en la capa gratuita de su proveedor |
| `D-20` | ¿Se puede levantar todo en local con un solo comando? | **Sí, con Docker Compose. Los servicios externos se reemplazan por dobles o cuentas de prueba** | Solo el back y la base en local · solo en la nube | `CN-23`; paridad entre entornos (Twelve-Factor, factor X). Abre `T-5` |
| `D-21` | ¿En qué celulares y navegadores funciona? | **En cualquier navegador moderno, con el celular como prioridad: el diseño y las pruebas principales se hacen en celular, el escritorio se verifica con la prueba de punta a punta en Chromium y Safari se prueba a mano** | Solo Android con Chrome · Android e iPhone sin escritorio | `CN-21` |
| `D-22` | Si alguien pide borrar sus datos, ¿qué pasa con la auditoría? | **Se anonimiza: la auditoría guarda solo identificadores, nunca el nombre, el correo ni el celular. Al pedir el borrado se eliminan esos datos de la cuenta, y la auditoría queda intacta pero ya no apunta a una persona identificable** | Conservar todo · dejarlo fuera del alcance | `CN-04`, `RF-003`. Cierra `T-4` |

---

## 3 · Ronda 3 · 12-nov-2026 · Atributos y drivers

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-23` | ¿Con qué lista de atributos de calidad se trabaja? | **Los 13 del ejercicio de atributos de calidad del curso que pesan en UCO Kudos: Confiabilidad (13), Disponibilidad (7), Rendimiento (8), Escalabilidad (9), Seguridad (4), Cumplimiento (17), Interoperabilidad (5), Experiencia de usuario (3), Mantenibilidad (16), Facilidad de prueba (1), Desplegabilidad (12), Capacidad para ser Soportado (18) y Capacidad para ser Administrado (14). Entre paréntesis, el número de su definición en el ejercicio** | Los 13 de la plantilla usada en FlorLogic («Capacidad» y «Portabilidad» no están entre los 19 del ejercicio) · esos 13 más Mantenibilidad · agregar también Observabilidad | Quedan fuera Eficiencia (15), Accesibilidad (11), Integridad conceptual (6) y Flexibilidad (2), porque pesan poco o quedan dentro de otros atributos; Reusabilidad (10), porque es una sola aplicación, e Internacionalización (19), porque es solo en español. Cumplimiento cubre la auditoría y la Ley 1581. La priorización es de 1 a 13 |
| `D-24` | ¿Qué atributos mueve cada driver? | **La tabla de la §5 de los drivers. `DRV-007` protege Seguridad y Disponibilidad, además de Desplegabilidad y Facilidad de prueba. Mantenibilidad no sale de un driver, sino de `CN-05` y `CN-15`** | — | §5 de los drivers |
| `D-25` | Además de los 7 obligatorios del enunciado, ¿qué drivers identifica el autor? | **`DRV-008` Permisos sin herencia y `DRV-009` Datos personales** | «Entrega garantizada», que queda cubierta por `DRV-003` con `D-01` y `D-02` · «Cambios en vivo», que queda como `CN-05` y Mantenibilidad | §5 de los drivers |
| `D-26` | Con `D-10`, ¿alguna funcionalidad que había quedado fuera pasa a ser significativa? | **Sí: la moderación (reportar y vetar), como `RF-009`** | Calificaciones · administrar categorías | El veto cambia lo que ve toda la comunidad y obliga a invalidar la caché del muro (`DRV-002`) |

---

## 4 · Ronda 4 · 12-nov-2026 · Trade-offs

Cada pregunta enfrenta dos atributos; la §6.2 de los drivers recoge lo que se sacrifica en cada una.

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-27` | Seguridad contra Rendimiento: si a alguien le quitan el rol o le desactivan la cuenta, ¿en cuánto tiempo pierde el acceso? | **En minutos: tokens de vida corta (unos 15 minutos) que se renuevan. La API valida el token sin consultar al IdP en cada petición** | De inmediato, consultando el estado en cada petición · al cerrar sesión, con tokens largos | `RF-006`; el ADR de identidad |
| `D-28` | Experiencia de usuario contra Seguridad: ¿cada cuánto se vuelve a iniciar sesión en el celular? | **Sesión larga que se renueva sola mientras se use y caduca tras unos 30 días sin uso** | Cada 7 días · cada vez que se abre la aplicación | `DRV-001`; el ADR de identidad |
| `D-29` | Rendimiento y experiencia en el celular contra fidelidad de los adjuntos: ¿cómo se muestran las imágenes en el muro? | **Como miniatura, generada al subir la imagen. El original solo se descarga al abrir el Kudo** | Mostrar siempre el original · no mostrar imágenes en el muro | `RF-004`, `RF-005` |
| `D-30` | Facilidad de prueba contra plazo: ¿qué cobertura mínima exige la CI? | **70 % en el dominio y los casos de uso. Los adaptadores se cubren con pruebas de integración, sin umbral** | 80 % en todo el código · sin umbral | `CN-25` |
| `D-31` | Disponibilidad contra costo cero: el servidor gratuito se duerme. ¿Qué se hace? | **Un ping de salud periódico desde un monitor gratuito la mantiene despierta y sirve, además, como monitoreo de disponibilidad. Hay que confirmar que la capa gratuita lo permite** | Aceptar el arranque en frío y despertarla a mano antes de la demo | `CN-19`; el ADR de despliegue. `[:R:]` Reinterpretada por `D-53`: la instancia pagada con el crédito queda siempre encendida, así que el ping ya no la despierta; sigue solo como monitor de disponibilidad (`ESC-07`) |
| `D-32` | ¿Quién aprueba a un Docente? (la tabla del enunciado no lo dice) | **El Coordinador de Programa de cualquiera de los programas donde enseña** | El Coordinador de Facultad · el Coordinador Académico | `CN-09`, `RF-007` |

---

## 5 · Ronda 5 · 12-nov-2026 · Priorización de atributos

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-33` | ¿Quiénes votan la prioridad de los atributos? | **Cuatro actores: Comunidad académica, Directivos, Administrador del sistema y Equipo de desarrollo y operación (el autor). El orden de la lista de `D-23` no es una prioridad: la prioridad sale de la votación** | Solo los 3 roles institucionales | §6.1 de los drivers: 4 × 91 = 364 puntos |
| `D-34` | ¿Cómo ordena cada actor los 13 atributos? | **Comunidad: «lo que se ve primero». Directivos: «control y evidencia». Administrador del sistema: «operar sin sorpresas». Equipo: «lo que evalúa el docente». Los cuatro órdenes completos están en la §6.1 de los drivers** | Comunidad: «que mi Kudo llegue» · Directivos: «que la plataforma funcione» · Administrador: «que no se caiga» · Equipo: «cambiar sin miedo» | No hubo actores reales: el autor votó en nombre de cada uno |
| `D-35` | La votación pone primera a Seguridad, pero `D-01` ponía primeras a Confiabilidad y Disponibilidad. ¿Qué manda? | **Manda la votación: Seguridad queda primera, y Confiabilidad y Disponibilidad, segunda y tercera** | Corregir el voto para que mande `D-01` | Reinterpreta `D-01`. Cierra `T-6` |
| `D-36` | ¿Cómo se rompen los tres empates de la votación? | **Rendimiento le gana a Interoperabilidad (30 puntos), porque tres de los cuatro actores lo ponen por encima. Desplegabilidad le gana a Mantenibilidad (31), porque sale de un driver obligatorio (`DRV-007`). Facilidad de prueba le gana a Capacidad para ser Administrado (33), porque moldea la arquitectura y sale de la pirámide de pruebas obligatoria (`CN-16`)** | Los ganadores contrarios en cada empate | §6.1 de los drivers |

---

## 6 · Ronda 6 · 12-nov-2026 · Caracterización

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-37` | De las 30 preguntas de caracterización, ¿cuáles se vuelven escenarios? | **Por regla: todas las de los 5 drivers de calidad, al menos una por cada DRV y una por cada paso del flujo obligatorio. Quedan 16 escenarios** | Una votación de puntos entre los 4 actores, que haría una sola persona · convertir las 30 en escenarios | §7 y §8 de los drivers |
| `D-38` | ¿Qué capacidades de cambio se exigen? | **Las cuatro: cambiar de proveedor de correo o de WhatsApp tocando solo su adaptador y la configuración; agregar un canal de aviso sin modificar el caso de uso de publicar; volver a la versión anterior si un despliegue sale mal; cambiar de proveedor de identidad solo con configuración, porque la API valida tokens estándar (OIDC)** | — | Respuestas SÍ en `P-18`, `P-19`, `P-21` y `P-23`; un adaptador por canal; `ESC-15`. Ayuda a cerrar `T-5` |
| `D-39` | ¿Con qué carga se mide el rendimiento del muro? | **Los 800 simultáneos de `D-05` se justifican por diseño; en la URL pública se mide la línea base con unos 50 simultáneos. La cifra se ajusta después del spike de despliegue** | Medir 800 en local · medir las dos cosas | `ESC-13` |
| `D-40` | Si se pierde la base de datos o los adjuntos, ¿cuánto se puede perder y en cuánto se recupera? | **Hasta 24 horas de información, con recuperación en 4 horas como máximo: copia diaria automática de la base y de los adjuntos, guardada fuera del proveedor principal, y una restauración probada al menos una vez** | No perder nada y recuperar en 1 hora, que choca con `CN-03` · no garantizar la recuperación | `ESC-06`, `DRV-004` |

---

## 7 · Ronda 7 · 12-nov-2026 · Medidas de los escenarios

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-41` | ¿Qué se revisa antes de guardar un adjunto? | **En producción: el tipo real del archivo (su contenido, no solo la extensión) y su tamaño, y se sirve con encabezados que impiden ejecutarlo. En local, además, cuarentena con un antivirus (ClamAV) en Docker Compose, demostrable en la sustentación con el archivo de prueba EICAR. Un ADR deja el antivirus en producción para cuando haya presupuesto para unos 2 GB de RAM** | Antivirus también en producción, que la capa gratuita no aguanta (`CN-03`) | `ESC-03`, `RF-004`. Abre `T-7` |
| `D-42` | ¿Cuántas veces se reintenta un aviso fallido? | **Hasta 5 intentos en 30 minutos, con espera creciente. Al agotarlos, el aviso queda fallido, auditado y contado en las métricas por canal** | 3 intentos en 10 minutos · reintentar hasta 24 horas | `ESC-05`, `ESC-08`, `RF-002` |
| `D-43` | ¿Qué disponibilidad se compromete? | **99 % en la semana de evaluación, medido por el monitor del ping de salud: como máximo unas 1,7 horas fuera de servicio por semana** | 95 % · sin meta numérica | `ESC-07` |
| `D-44` | ¿Qué medidas llevan los demás escenarios? | **Las de la §8 de los drivers: muro con p95 ≤ 1 s y Kudo nuevo visible en ≤ 5 s; publicar en ≤ 2 s; push en ≤ 2 s; avisos en ≤ 1 min; diagnóstico en ≤ 5 min; despliegue en ≤ 15 min y vuelta atrás en ≤ 10 min; supresión en ≤ 15 días hábiles; historia de un Kudo en ≤ 2 s; publicar desde el celular en una pantalla y en ≤ 60 s** | Un juego de medidas más exigente, con más riesgo de no cumplirse en la capa gratuita | §8 de los drivers |

---

## 8 · Ronda 8 · 12-nov-2026 · Alternativa de solución

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-45` | ¿Qué estilo de arquitectura se elige? | **`ALT-1`: monolito modular con Clean Architecture y avisos en segundo plano** | `ALT-2` microservicios · `ALT-3` backend como servicio · `ALT-4` funciones serverless | §2 de la alternativa de solución; el futuro `ADR-001` |
| `D-46` | ¿Dónde corre el despachador de avisos? | **Dentro de la API, como tarea en segundo plano del mismo proceso** | Un proceso aparte (worker) · funciones serverless | Una sola instancia (`D-15`); en el C4 nivel 2 no es un contenedor aparte. `ABB-09` |
| `D-47` | ¿Cómo se garantiza que un aviso pendiente no se pierda? | **Con un outbox en la base: el Kudo y sus avisos pendientes se guardan en la misma transacción, y el despachador los va leyendo** | Un broker gestionado · eventos en memoria | No se adopta un broker dedicado. `ABB-09`, `ESC-04` |
| `D-48` | ¿Dónde vive la caché del muro? | **En memoria de la API. Al escalar a varias instancias pasa a una caché compartida, y eso se documenta como evolución** | Una caché compartida gestionada · sin caché, solo índices | `ABB-06`, `ESC-13` |

---

## 9 · Ronda 9 · 12-nov-2026 · Organización interna y borde

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-49` | ¿Cómo se organiza por dentro la API? | **Un hexágono por módulo: Kudos, avisos, identidad y permisos, auditoría y moderación. Cada uno tiene su dominio, su aplicación y sus adaptadores, y los módulos solo se hablan por interfaces** | Un solo hexágono para toda la API · carpetas por capas técnicas | `ABB-04`, `CN-15` |
| `D-50` | ¿Cómo se ponen de acuerdo el front y el back? | **REST con un contrato OpenAPI generado desde el código, que el front usa como fuente** | GraphQL · REST sin contrato formal | `CN-12`, `ABB-01`, `ABB-04` |
| `D-51` | ¿Qué se hace con el WAF y el API Gateway? | **Un gateway con WAF (Nginx con ModSecurity) solo en local: va en Docker Compose, se prueba en la CI y se demuestra en la sustentación. En producción la API queda directa, con el HTTPS de la plataforma y con CORS, validación y límite de peticiones dentro de la API. Un ADR deja el gateway para cuando haya presupuesto para otra instancia** | Ninguno, ni siquiera en local · un WAF gratuito delante, que exige un dominio propio | `ABB-18`. Es el mismo patrón de `D-41`. Abre `T-8` |
| `D-52` | ¿Qué bloques de construcción (ABB) tiene la solución? | **Los 21 de la §3 de la alternativa de solución, de `ABB-01` a `ABB-21`** | — | §3 de la alternativa de solución |

---

## 10 · Ronda 10 · 12-nov-2026 · Plataforma en la nube

Microsoft da a los estudiantes verificados un crédito de Azure sin tarjeta (Azure for Students). Esta
ronda decide qué cambia con ese crédito y qué no. Que la suscripción se active con el correo de la UCO
y deje desplegar lo confirma `SPK-04`.

| ID | Pregunta | Decisión | Se descartó | Qué mueve |
|---|---|---|---|---|
| `D-53` | ¿Cuánto se puede gastar en infraestructura? (reemplaza a `D-07`) | **El crédito de Azure for Students: 100 USD para 12 meses, sin tarjeta y sin pagar nada de bolsillo. La meta es gastar como máximo 30 USD al mes, con una alerta de presupuesto, para que el crédito cubra de sobra la evaluación. Si la suscripción no se activa o no deja desplegar, se vuelve a las capas gratuitas (Render y Vercel) como plan B** | Seguir en 0 USD con capas gratuitas, que queda como plan B · Pagar de bolsillo unos 10 USD al mes | Deroga `D-07` y reinterpreta `D-19` y `D-31`. Reescribe `CN-03` y `CN-19`: la instancia queda siempre encendida, sin arranque en frío. Abre `T-9`. `ADR-014`, `ADR-022` |
| `D-54` | ¿Qué se lleva a Azure? | **La plataforma, no la arquitectura. La API, la base de datos, los adjuntos, el front y la observabilidad pasan a servicios gestionados de Azure; el IdP, el correo y WhatsApp siguen con sus proveedores. No cambian el estilo (`D-45`), el outbox (`D-47`), la caché en memoria (`D-48`), el borde (`D-51`) ni la instancia única (`D-15`). Los servicios de Azure que harían falta para escalar (caché compartida, relevo de push y más instancias) quedan, con nombre propio, como la evolución de `ADR-011`** | Rehacer la solución con servicios propios de Azure (colas, funciones, Web PubSub, Redis, un WAF gestionado), que contradice ADR aceptados y gasta el crédito · Llevar a Azure solo el back · Mover también la identidad a Microsoft Entra External ID | `ADR-011`, `ADR-012`, `ADR-017`, `ADR-022`, `ADR-023`, `ADR-024`. En `T-5`, la paridad local de los archivos pasa de MinIO a Azurite |

**Lo que mostró `SPK-04` (13-nov-2026, partes 1 y 2).** `D-53` y `D-54` se sostienen: la suscripción
se activó, el presupuesto quedó creado y la API responde en internet. Cambia la forma, no la
decisión:

| Se suponía | Resultó | Dónde quedó escrito |
|---|---|---|
| Todo en una sola región, `westus2` | Esa región no deja crear PostgreSQL. La API, la base y los archivos quedaron en `canadacentral`, y el front en `westus2`, que para Static Web Apps es solo la región de administración | `ADR-017`, `ADR-022` |
| La API en App Service B1, con costo fijo de ~13 USD al mes | La cuota de B1 es 0. La API quedó en Container Apps, con una réplica siempre encendida, por ~10 a ~30 USD al mes | `ADR-022`; también `ADR-011`, `ADR-015`, `ADR-021`, `ADR-023` |

Ninguno de los dos cambios toca lo que `D-54` mantiene (estilo, outbox, caché en memoria, borde e
instancia única), y no se activó el plan B de `D-53`.

---

## Tensiones entre decisiones

Choques que las decisiones dejan abiertos a propósito. No se resuelven aquí, sino en la decisión
o en el documento que se indica. **Aceptada** quiere decir que la diferencia se asume a conciencia y
queda documentada.

| ID | Choque | Por qué choca | Dónde se resuelve | Estado |
|---|---|---|---|---|
| `T-1` | `D-05` (800 simultáneos) contra `D-07` (0 USD) | Es poco probable que una instancia de capa gratuita sostenga 800 usuarios a la vez, y no hay cómo medirlo sin pagar | `D-15`. El escenario de rendimiento separa lo que permite la arquitectura de lo que se mide en el despliegue gratuito | **Reducida** por `D-15` |
| `T-2` | `D-09` (IdP) contra `D-07` (0 USD) y `CN-01` (plazo) | Un IdP autohospedado consume una memoria que las capas gratuitas casi no dan; uno gestionado tiene capa gratuita, pero es un servicio externo más | Un spike de identidad y su ADR. `D-19` la inclina hacia un IdP gestionado | **Abierta** |
| `T-3` | `D-04` (canal en vivo) contra `D-05` (800 simultáneos) | Puede haber hasta 800 conexiones abiertas. Con más de una instancia de la API, el push necesita un relevo compartido entre instancias | `D-15`: con una sola instancia no hace falta relevo; queda documentado como evolución en el ADR de push | **Reducida** por `D-15` |
| `T-4` | `D-16` (borrar datos personales) contra `D-08` (la auditoría nunca se borra) | La ley permite pedir que se borren los datos personales, y la auditoría no se puede modificar | `D-22` | **Cerrada** por `D-22` |
| `T-5` | `D-19` (servicios gestionados) contra `D-20` (todo en local con un comando) | En local no existen los servicios gestionados, así que hay que reemplazarlos por contenedores. Solo hay paridad si producción y local hablan el mismo protocolo (SQL, almacenamiento compatible con S3, OIDC) | Los ADR de base de datos, archivos e identidad | **Abierta** |
| `T-6` | `D-01` (Confiabilidad y Disponibilidad primero) contra la votación por actor (Seguridad primero) | La frase de lo que hace inútil el sistema apunta a Confiabilidad, pero la votación de los cuatro actores le da el primer puesto a Seguridad | `D-35` | **Cerrada** por `D-35` |
| `T-7` | `D-41` (antivirus solo en local) contra `D-20` (paridad entre entornos) | Local y producción revisan los adjuntos de forma distinta: en producción no hay antivirus | El ADR de adjuntos documenta la diferencia y el disparador para eliminarla: presupuesto para unos 2 GB de RAM | **Aceptada** por `D-41` |
| `T-8` | `D-51` (gateway con WAF solo en local) contra `D-20` (paridad entre entornos) | En local las peticiones pasan por el gateway con WAF; en producción llegan directo a la API | El ADR del borde documenta la diferencia y el disparador para eliminarla: presupuesto para otra instancia | **Aceptada** por `D-51` |
| `T-9` | `D-53` (crédito finito) contra `D-43` (99 % de disponibilidad) y `CN-13` (URL pública) | Si el crédito se agota, Azure deshabilita la suscripción y la URL deja de responder | `D-53`: meta de 30 USD al mes como máximo y alerta de presupuesto; a ese ritmo el crédito dura más de tres meses, mucho más que la evaluación. Si aun así se agota, el plan B de `ADR-022`. La alerta quedó creada el 13-nov-2026 (`SPK-04`). `[!]` Con Container Apps el costo estimado sube a ~10 a ~45 USD al mes (`ADR-022`); si pasa de 30, fuera de la evaluación la réplica mínima baja a 0 | **Reducida** por `D-53` |

**Efecto de la ronda 10 sobre las tensiones anteriores.** `D-07` quedó derogada por `D-53`, pero `T-1`
y `T-2` siguen igual con el crédito: no alcanza para varias instancias siempre encendidas dentro de la
meta de gasto, ni para autohospedar el IdP sin una segunda instancia. En `T-5`, el almacén de archivos
pasa a Azure Blob Storage (`D-54`), que no habla la API de S3: la paridad en local la da Azurite, el
emulador de Azure Storage, que habla la misma API de Blob (`ADR-017`).
