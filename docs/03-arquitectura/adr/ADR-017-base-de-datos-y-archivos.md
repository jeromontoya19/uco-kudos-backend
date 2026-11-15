# `ADR-017` · Base de datos y almacenamiento de archivos

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-19`, `D-20`, `D-40`, `D-53`, `D-54`; `ADR-003`, `ADR-009`, `ADR-012`, `ADR-022`; `CN-07`, `CN-08`; `T-5`
**La confirma:** `SPK-06` (y la región, `SPK-04`)

## Contexto

La base tiene que guardar el Kudo y sus avisos pendientes en una sola transacción (`ADR-003`), hacer
cumplir reglas de unicidad (`CN-07`, `CN-08`, una calificación por usuario) y leer conversaciones sin
límite de profundidad (`RF-008`). Los archivos van fuera de la base (`ADR-009`). Los dos deben ser
gestionados (`D-19`, `D-54`), estar en la misma región que la API (`ADR-022`) y tener un equivalente
en contenedor para el entorno local (`D-20`, `T-5`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

**Motor de base de datos:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| PostgreSQL gestionado | Bajo | Bajo | Medio | Medio | Alto | Alto |
| MySQL gestionado | Bajo | Bajo | Medio | Medio | Alto | Medio |
| MongoDB gestionado | Bajo | Medio | Alto | Medio | Medio | Medio |

**Proveedor de PostgreSQL:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Azure Database for PostgreSQL, servidor flexible (B1ms) | Bajo | Bajo | Medio | Medio | Alto | Alto |
| Neon | Bajo | Bajo | Medio | Medio | Alto | Medio |
| Supabase | Bajo | Bajo | Medio | Medio | Medio | Medio |

**De dónde sale el riesgo:** en Azure, que la política de la suscripción no deje crear el servidor
flexible en la región de la API (`CN-19`), y que no aplique la capa gratuita de 12 meses (750 horas
de B1ms y 32 GB): entonces cuesta unos 15 USD al mes, que siguen dentro de la meta de `D-53`. Neon y
Supabase pausan o duermen la base gratuita sin uso.

**Almacenamiento de archivos:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Almacén gestionado de objetos | Bajo | Bajo | Alto | Medio | Alto | Alto |
| Archivos guardados dentro de la base | Bajo | Bajo | Bajo | Medio | Medio | Bajo |
| Disco de la instancia | Bajo | Bajo | Bajo | Alto | Bajo | Bajo |

El disco de la instancia tiene riesgo Alto porque en las plataformas de contenedores suele ser
efímero: se pierde en cada redespliegue.

**Proveedor del almacén de objetos:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Azure Blob Storage | Bajo | Bajo | Alto | Bajo | Alto | Alto |
| Cloudflare R2 (API de S3) | Bajo | Bajo | Alto | Medio | Alto | Alto |
| Backblaze B2 (API de S3) | Bajo | Bajo | Alto | Medio | Alto | Medio |
| Supabase Storage | Bajo | Bajo | Medio | Medio | Medio | Medio |

**De dónde sale el riesgo:** Blob Storage no habla la API de S3, así que su adaptador usa el SDK de
Azure y en local hace falta su emulador (Azurite) en vez de MinIO. Los demás son un proveedor más,
fuera de la suscripción.

## Propuesta

- **Base de datos: PostgreSQL en Azure Database for PostgreSQL, servidor flexible**, plan *Burstable*
  B1ms, en `canadacentral`, la región donde la política de la suscripción deja crear el servidor
  flexible (`SPK-04`); la API va en esa misma región. Tiene transacciones para el outbox, restricciones de unicidad y
  consultas recursivas para las réplicas. Solo acepta conexiones cifradas, desde servicios de Azure y
  desde las IP que se autoricen. En local corre el mismo motor en un contenedor, con el mismo SQL:
  cierra `T-5` para la base. **Plan B:** Neon.
- **Archivos: Azure Blob Storage**, con un contenedor privado, sin acceso anónimo. La API entrega
  cada archivo con los encabezados de `ADR-009`; servirlo directo desde Blob con una URL firmada de
  corta duración queda como optimización. En local, **Azurite**, el emulador de Azure Storage, que
  habla la misma API de Blob: cierra `T-5` para los archivos. **Plan B:** un almacén compatible con
  S3 (R2 o B2), con MinIO en local.

El almacén queda detrás del puerto de archivos del módulo de Kudos (`ADR-002`): pasar de Blob a S3 es
cambiar el adaptador y la configuración, no las reglas (`D-38`).

## Consecuencias

**Se gana:** un motor relacional para reglas que son relacionales; paridad por protocolo entre local y
producción, y la base, los archivos y la API en la misma suscripción y la misma región.

**Cuesta:** unos 15 USD al mes si no aplica la capa gratuita de la base, y un adaptador de archivos
atado a la API de Blob.

## Escenarios

`ESC-04`, `ESC-06`, `ESC-09`

## Observaciones

Si `SPK-06` confirma la propuesta, `T-5` queda cerrada para la base y los archivos; el IdP la cierra
`ADR-018`.

`[!]` Los límites de la capa gratuita de 12 meses y si la región permite el servidor flexible se
confirman en `SPK-04` y `SPK-06`; este ADR no los da por ciertos.

`[:W:]` 12-nov-2026: la propuesta anterior era PostgreSQL en Neon o Supabase y un almacén compatible
con S3 (R2, B2 o Supabase Storage) con MinIO en local. Con `D-54` pasó a Azure, y lo anterior quedó
como plan B.

`[:W:]` 14-nov-2026: así quedó conectada la base, y así la usa la API desplegada (`SPK-04`, paso 10):
- **Acceso a datos con Spring Data JDBC, no JPA.** Los repositorios guardan y leen agregados
  completos, sin carga perezosa ni caché de sesión, y arranca con menos memoria que JPA (`ADR-015`).
  El esquema lo crean solo las migraciones de Flyway, al arrancar la API.
- **Un esquema por módulo** (`D-55`): `kudos`, `avisos`, `identidad`, `auditoria` y `moderacion`, que
  crea la primera migración. La frontera entre módulos de `ADR-002` también se ve en la base.
- **Conexión cifrada y verificada.** El servidor exige TLS (`require_secure_transport` en `on`), y la
  API verifica su certificado y su nombre (`sslmode=verify-full`) contra las raíces de la JVM. La
  cadena del servidor termina en DigiCert Global Root G2, que trae el JRE de la imagen.
- **Configuración por variables de entorno:** `BD_URL`, `BD_USUARIO` y `BD_CONTRASENA`. En Container
  Apps, la contraseña es el secreto `bd-contrasena`, y la base de la aplicación es `ucokudos`.
- **Pruebas de integración** contra PostgreSQL 16 en Testcontainers, la misma versión mayor de
  Azure.

`[!]` La API entra con el usuario administrador del servidor. Falta un rol propio, con permiso solo
sobre los esquemas de la aplicación.
