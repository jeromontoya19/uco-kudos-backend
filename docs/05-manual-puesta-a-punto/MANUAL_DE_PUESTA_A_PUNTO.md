# UCO Kudos — Manual de puesta a punto

**v0.1 · 15-nov-2026 · Jerónimo Montoya Londoño**

> Cómo correr UCO Kudos en un computador y cómo montarlo desde cero en Azure, con sus servicios
> externos. Los nombres son los reales del proyecto: si se monta otra copia, se cambian los que deben
> ser únicos, como el servidor de la base, el de la API y el dominio de Auth0.
>
> **Los secretos nunca se escriben en un comando visible ni en el repositorio:** se leen con `read -rs`
> en Cloud Shell y van como secretos de Container Apps o de GitHub.

## Contenido

1. [Lo que se necesita](#1--lo-que-se-necesita)
2. [Correrlo en local](#2--correrlo-en-local)
3. [Configuración de la API](#3--configuración-de-la-api)
4. [Montarlo en Azure desde cero](#4--montarlo-en-azure-desde-cero)
5. [Servicios externos](#5--servicios-externos)
6. [Desplegar y volver atrás](#6--desplegar-y-volver-atrás)
7. [Comprobar que funciona](#7--comprobar-que-funciona)
8. [Problemas conocidos](#8--problemas-conocidos)

---

## 1 · Lo que se necesita

| Para | Herramienta |
|---|---|
| Los dos repositorios | Git y una cuenta de GitHub: [`uco-kudos-backend`](https://github.com/jeromontoya19/uco-kudos-backend) y [`uco-kudos-frontend`](https://github.com/jeromontoya19/uco-kudos-frontend) |
| El back en local | Docker Desktop. No hace falta instalar Java: Maven y Java 21 corren en contenedores |
| El front en local | Node.js 24 (`.nvmrc`) y npm, o Docker |
| La nube | Una suscripción de Azure (se usó Azure for Students) y el **Cloud Shell de Azure**, que ya trae `az` y `psql` |
| Los servicios externos | Cuentas en Auth0, Brevo, Meta for Developers y UptimeRobot (todas con capa gratuita) |

---

## 2 · Correrlo en local

### 2.1 · Las pruebas del back

Desde la carpeta de `uco-kudos-backend` (Git Bash en Windows). Testcontainers levanta un PostgreSQL 16
de verdad para las pruebas de integración:

```bash
MSYS_NO_PATHCONV=1 docker run --rm -v "$PWD:/fuente" -v uco-kudos-m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal -w /fuente eclipse-temurin:21-jdk-noble sh mvnw -B -ntp verify
```

Debe terminar en `BUILD SUCCESS`, con las pruebas unitarias, las de integración (`*IT`), las reglas
de ArchUnit y la cobertura mínima de JaCoCo (70 % en dominio y aplicación). La primera vez tarda más,
porque descarga las dependencias en el volumen `uco-kudos-m2`.

### 2.2 · La API en local

1. Una red y una base PostgreSQL 16:
   ```bash
   docker network create uco-kudos
   ```
   ```bash
   docker run -d --name uco-kudos-db --network uco-kudos -e POSTGRES_DB=ucokudos -e POSTGRES_USER=ucokudos -e POSTGRES_PASSWORD=local -p 5432:5432 postgres:16-alpine
   ```
2. La imagen de la API:
   ```bash
   docker build -t uco-kudos-api:local .
   ```
3. La API, conectada a esa base. En local no hay certificado, así que la URL lleva `sslmode=disable`.
   Las variables del IdP pueden ser las de Auth0 (sección 5.1):
   ```bash
   docker run -d --name uco-kudos-api --network uco-kudos -p 8080:8080 -e BD_URL="jdbc:postgresql://uco-kudos-db:5432/ucokudos?sslmode=disable" -e BD_USUARIO=ucokudos -e BD_CONTRASENA=local -e IDP_EMISOR="https://<dominio-de-auth0>/" -e IDP_AUDIENCIA="https://api.ucokudos" -e CORS_ORIGENES="http://localhost:5173" uco-kudos-api:local
   ```
4. `http://localhost:8080/actuator/health` debe responder `{"status":"UP"}`. Al arrancar, Flyway crea
   los esquemas y las personas y categorías de prueba.

Sin las llaves de Brevo y Meta, la API arranca igual: los avisos por correo y WhatsApp fallan y quedan
para reintento. El agente de Application Insights tampoco se carga, porque falta su cadena de
conexión.

> `ADR-012` plantea levantar todo con un solo comando de Docker Compose, con dobles de los servicios
> externos. Todavía no está construido: hoy se usan los pasos de arriba.

### 2.3 · El front en local

Está en el [README del front](https://github.com/jeromontoya19/uco-kudos-frontend#cómo-correrla-en-local).
En resumen:

```bash
npm ci
```
```bash
npm run dev
```

Con `npm run dev`, el proxy de Vite lleva `/api` a la API de producción. Para usar la API local, se
copia `.env.example` como `.env.local` y se pone `VITE_API_URL=http://localhost:8080`.

---

## 3 · Configuración de la API

Todo entra por variables de entorno. Los secretos en Azure son secretos de Container Apps, y la
variable los referencia con `secretref:`.

| Variable | Qué es | ¿Secreto? | Si falta |
|---|---|:---:|---|
| `BD_URL` | `jdbc:postgresql://<servidor>.postgres.database.azure.com:5432/ucokudos` (en Azure va con TLS `verify-full`) | No | La API no arranca |
| `BD_USUARIO` | `ucokudos` | No | La API no arranca |
| `BD_CONTRASENA` | Contraseña de la base → `secretref:bd-contrasena` | **Sí** | La API no arranca |
| `IDP_EMISOR` | `https://<dominio-de-auth0>/`, con la barra final | No | La API no arranca |
| `IDP_AUDIENCIA` | `https://api.ucokudos` | No | La API no arranca |
| `CORS_ORIGENES` | La URL del front, por ejemplo `https://happy-bush-0fa58021e.4.azurestaticapps.net` | No | El navegador no deja llamar a la API |
| `BREVO_API_KEY` | Llave de API de Brevo → `secretref:brevo-api-key` | **Sí** | Los correos fallan y se reintentan |
| `CORREO_REMITENTE` | La dirección verificada en Brevo | No | Los correos fallan y se reintentan |
| `URL_DEL_FRONT` | El enlace que va en el correo | No | El correo sale sin enlace |
| `WHATSAPP_TOKEN` | Token permanente de Meta → `secretref:whatsapp-token` | **Sí** | Los WhatsApp fallan y se reintentan |
| `WHATSAPP_ID_TELEFONO` | El *Phone number ID* del número de WhatsApp | No | Los WhatsApp fallan y se reintentan |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | Cadena de conexión → `secretref:appinsights-conexion` | **Sí** | La API arranca sin el agente, y no se envían trazas |

---

## 4 · Montarlo en Azure desde cero

Todo se corre en el **Cloud Shell de Azure** (Bash). Si Cloud Shell se reinicia, las variables de la
sesión se pierden y hay que volver a definirlas.

```bash
GRUPO=rg-uco-kudos; REGION=canadacentral; SERVIDOR=ucokudos-db-4632; APP=ucokudos-api; ENTORNO=env-uco-kudos; WEB=ucokudos-web
```

### 4.1 · Suscripción y grupo de recursos

1. Activar la suscripción (en el proyecto, Azure for Students). Entrar con una cuenta Microsoft
   personal, para poder registrar aplicaciones en Entra ID (sección 4.6).
2. Revisar en *Policy → Assignments → Allowed resource deployment regions* qué regiones permite la
   suscripción. En la del proyecto, `westus2` no deja crear PostgreSQL, así que todo quedó en
   `canadacentral` (`SPK-04`).
3. Crear un presupuesto en *Cost Management → Budgets*: 30 USD al mes, con avisos al 50, 80 y 100 %
   (`D-53`).
4. Registrar los proveedores de recursos y crear el grupo:
   ```bash
   for p in Microsoft.App Microsoft.OperationalInsights Microsoft.DBforPostgreSQL Microsoft.Web Microsoft.Insights Microsoft.Storage; do az provider register --namespace $p; done
   ```
   ```bash
   az group create --name $GRUPO --location $REGION
   ```

### 4.2 · Base de datos PostgreSQL

La contraseña se escribe sin que se vea:

```bash
read -rs -p "Contraseña nueva de la base: " CLAVE_BD; echo
```
```bash
az postgres flexible-server create --resource-group $GRUPO --name $SERVIDOR --location $REGION --tier Burstable --sku-name Standard_B1ms --storage-size 32 --version 16 --admin-user ucokudos --admin-password "$CLAVE_BD" --public-access 0.0.0.0
```
```bash
az postgres flexible-server db create --resource-group $GRUPO --server-name $SERVIDOR --name ucokudos
```

`--public-access 0.0.0.0` solo deja entrar a los servicios de Azure (la API y Cloud Shell). El servidor
solo acepta TLS, y la API verifica su certificado (`sslmode=verify-full`, `ADR-017`). Las tablas las
crea Flyway cuando la API arranca por primera vez.

### 4.3 · La API en Container Apps

La imagen sale del CD hacia GitHub Container Registry, con el SHA del commit como etiqueta. La
primera vez se puede crear la app con cualquier imagen publicada y dejar que el CD la reemplace.

```bash
az containerapp env create --name $ENTORNO --resource-group $GRUPO --location $REGION
```
```bash
az containerapp create --name $APP --resource-group $GRUPO --environment $ENTORNO --image ghcr.io/jeromontoya19/uco-kudos-backend:<sha-de-main> --target-port 8080 --ingress external --cpu 0.5 --memory 1.0Gi --min-replicas 1 --max-replicas 1
```

La réplica queda siempre encendida (mínimo y máximo 1, `ADR-011`, `ADR-022`). En la suscripción del
proyecto, el entorno quedó de tipo **Express** (sección 8).

**Secretos y variables.** Primero los secretos, cada uno leído sin mostrarlo:

```bash
az containerapp secret set --resource-group $GRUPO --name $APP --secrets "bd-contrasena=$CLAVE_BD" --output none; unset CLAVE_BD
```
```bash
read -rs -p "API key de Brevo: " LLAVE; echo; read -rs -p "Token permanente de WhatsApp: " TOKEN; echo
```
```bash
az containerapp secret set --resource-group $GRUPO --name $APP --secrets "brevo-api-key=$LLAVE" "whatsapp-token=$TOKEN" --output none; unset LLAVE TOKEN
```

Después las variables, con los valores de la sección 3:

```bash
FQDN=$(az postgres flexible-server show --resource-group $GRUPO --name $SERVIDOR --query fullyQualifiedDomainName -o tsv)
```
```bash
az containerapp update --resource-group $GRUPO --name $APP --set-env-vars "BD_URL=jdbc:postgresql://$FQDN:5432/ucokudos" "BD_USUARIO=ucokudos" "BD_CONTRASENA=secretref:bd-contrasena" "IDP_EMISOR=https://<dominio-de-auth0>/" "IDP_AUDIENCIA=https://api.ucokudos" "CORS_ORIGENES=<url-del-front>" "BREVO_API_KEY=secretref:brevo-api-key" "CORREO_REMITENTE=<correo-verificado-en-brevo>" "URL_DEL_FRONT=<url-del-front>" "WHATSAPP_TOKEN=secretref:whatsapp-token" "WHATSAPP_ID_TELEFONO=<phone-number-id>" --output none
```

### 4.4 · Observabilidad (Application Insights)

```bash
az config set extension.use_dynamic_install=yes_without_prompt
```
```bash
WS=$(az monitor log-analytics workspace list --resource-group $GRUPO --query "[0].id" -o tsv)
```
```bash
az monitor app-insights component create --app ucokudos-insights --location $REGION --resource-group $GRUPO --workspace "$WS" --application-type web --output none
```
```bash
az containerapp secret set --resource-group $GRUPO --name $APP --secrets "appinsights-conexion=$(az monitor app-insights component show --app ucokudos-insights --resource-group $GRUPO --query connectionString -o tsv)" --output none
```
```bash
az containerapp update --resource-group $GRUPO --name $APP --set-env-vars "APPLICATIONINSIGHTS_CONNECTION_STRING=secretref:appinsights-conexion" --output none
```

La imagen carga el agente de Application Insights solo cuando existe esa variable (`ADR-024`).

### 4.5 · El front en Static Web Apps

```bash
az staticwebapp create --name $WEB --resource-group $GRUPO --location westus2 --sku Free
```

El token de despliegue va como secreto `AZURE_STATIC_WEB_APPS_API_TOKEN` del repositorio del front.
Se copia en GitHub, en *Settings → Secrets and variables → Actions*, sin pegarlo en otro lado:

```bash
az staticwebapp secrets list --name $WEB --query properties.apiKey -o tsv
```

### 4.6 · Identidad del CD (OIDC, sin contraseñas)

El CD del back entra a Azure con una credencial federada, sin guardar contraseñas (`ADR-023`).

```bash
APP_ID=$(az ad app create --display-name github-uco-kudos-backend --query appId -o tsv); az ad sp create --id $APP_ID --output none
```
```bash
az role assignment create --assignee $APP_ID --role Contributor --scope $(az group show --name $GRUPO --query id -o tsv)
```
```bash
az ad app federated-credential create --id $APP_ID --parameters '{"name":"github-main","issuer":"https://token.actions.githubusercontent.com","subject":"repo:jeromontoya19/uco-kudos-backend:ref:refs/heads/main","audiences":["api://AzureADTokenExchange"]}'
```

`SPK-04` encontró que GitHub puede presentar el sujeto con los identificadores fijos del dueño y del
repositorio. Si `azure/login` falla con un error de sujeto, se crea una segunda credencial,
`github-main-ids`, con el sujeto exacto que aparece en el error.

En el repositorio del back van tres secretos: `AZURE_CLIENT_ID` (el `APP_ID`), `AZURE_TENANT_ID` y
`AZURE_SUBSCRIPTION_ID`, que se sacan con `az account show`. Además, `SONAR_TOKEN` va en los dos
repositorios, para SonarQube Cloud.

---

## 5 · Servicios externos

### 5.1 · Auth0 (identidad, `ADR-018`)

1. Crear un *tenant*. Su dominio, por ejemplo `dev-xxxx.us.auth0.com`, es el `IDP_EMISOR`.
2. *Applications → APIs → Create API*:
   - Identifier: `https://api.ucokudos`.
   - Firma: RS256.
   - *Maximum Access Token Lifetime*: 900 s.
   - Activar *Allow Offline Access*.
3. *Applications → Create Application*, de tipo **Single Page Application**:
   - URL de retorno, de salida y orígenes web: la URL del front y `http://localhost:5173`.
   - Activar la rotación de *refresh tokens*, con 30 días de vida sin uso (2592000 s).
   - En la API, pestaña *Application Access*, autorizar esta aplicación.
4. *Authentication → Database → Username-Password-Authentication*: activar **Disable Sign Ups**.
5. *User Management → Users*: crear las cuentas y copiar el `user_id` de cada una.
6. Vincular cada cuenta con su persona en la base:
   ```bash
   psql "host=$FQDN port=5432 dbname=ucokudos user=ucokudos sslmode=require" -c "UPDATE identidad.persona SET sujeto_idp = '<user_id de Auth0>' WHERE id = '<id de la persona>';"
   ```
   Las personas de prueba tienen los identificadores `00000000-0000-0000-0000-00000000000N`: `…6` es
   la docente y `…7` el estudiante (migración `V2`).

### 5.2 · Brevo (correo, `ADR-019`)

1. *Senders, domains & dedicated IPs → Senders*: agregar y verificar la dirección que envía. Esa es
   `CORREO_REMITENTE`.
2. *SMTP & API → API keys*: crear una llave con permiso **Todos** solo en correos transaccionales, y
   guardarla como el secreto `brevo-api-key` (sección 4.3).

### 5.3 · WhatsApp Cloud API de Meta (`ADR-020`, `SPK-01`)

1. En Meta for Developers, crear una app con el caso de uso **Conectarte con los clientes a través de
   WhatsApp**, en un portafolio comercial.
2. *Casos de uso → Personalizar → Paso 1. Pruébalo*:
   - Meta asigna un número de prueba. Su *Phone number ID* es `WHATSAPP_ID_TELEFONO`.
   - En **Para**, agregar y verificar los celulares que van a recibir. Son hasta 5.
3. En el Administrador de WhatsApp, crear la plantilla `kudo_recibido`:
   - Categoría Utilidad, idioma Español (Colombia), es decir, `es_CO`.
   - Tres variables: `{{1}}` el destinatario, `{{2}}` el autor y `{{3}}` la categoría.
   - Esperar la aprobación.
4. **Token permanente:** en business.facebook.com, entrar a *Configuración → Usuarios → Usuarios del
   sistema → Agregar*, como Administrador.
   - Asignarle la app y la cuenta de WhatsApp con control total.
   - Generar un token **sin vencimiento**, con `whatsapp_business_messaging` y
     `whatsapp_business_management`.
   - Guardarlo como el secreto `whatsapp-token`. El token temporal de la página de prueba vence a las
     24 h.
5. El celular de cada persona en la base va con indicativo, por ejemplo `+57300…`. El adaptador le
   quita todo lo que no sea dígito.

### 5.4 · UptimeRobot (disponibilidad, `ADR-014`)

Crear un monitor HTTP(s) sobre `https://<api>/actuator/health`, cada 5 min y con aviso por correo, y
una página pública de estado. La del proyecto es
[stats.uptimerobot.com/Kg3YSa2rj9](https://stats.uptimerobot.com/Kg3YSa2rj9).

---

## 6 · Desplegar y volver atrás

| Qué | Cómo |
|---|---|
| **Desplegar el back** | Un PR de `codigo-testing` a `main`. Al fusionarlo, el workflow `CD` construye la imagen, la publica en GHCR con el SHA, la despliega en Container Apps por OIDC y espera a que `/actuator/info` responda con esa versión |
| **Desplegar el front** | Igual, en `uco-kudos-frontend`: el `CD` compila la PWA, la sube a Static Web Apps y comprueba que el sitio sirva la versión nueva |
| **Volver atrás el back** | *Actions → Vuelta atrás → Run workflow*, desde `main`, con el SHA completo del commit al que se vuelve. Redespliega esa imagen sin reconstruirla (47 s en la prueba, `ESC-15`) |
| **Volver atrás el front** | *Actions → Vuelta atrás* en el front, con el SHA de un commit de `main`. Lo recompila con su `package-lock` y lo publica (38 s en la prueba) |
| **Cambiar un secreto** | `az containerapp secret set`, y después un `az containerapp update` que cambie una variable para que la API lo lea (sección 8) |

El flujo de ramas es `codigo-desarrollo` → `codigo-testing` → `main`, siempre con «Create a merge
commit» y sin borrar ramas (`ADR-013`). La documentación va por `documentacion-desarrollo` → `main`, y
el CD ignora los cambios en `docs/` y en los `.md`.

---

## 7 · Comprobar que funciona

1. **La salud:**
   ```bash
   curl -s https://<api>/actuator/health; echo
   ```
   Debe responder `{"status":"UP"}`. Esa salud incluye la conexión con la base.
2. **La versión:**
   ```bash
   curl -s https://<api>/actuator/info; echo
   ```
   Debe responder el SHA del último commit de `main`.
3. **La seguridad:** `curl -s -o /dev/null -w '%{http_code}' https://<api>/api/kudos` debe responder
   `401`.
4. **El flujo completo:**
   - Entra al front como estudiante y deja la app abierta.
   - En otro navegador, entra como docente y publica un Kudo para el estudiante.
   - Al estudiante le aparece el aviso flotante, y le llegan el correo y el WhatsApp.
5. **La auditoría:**
   ```bash
   psql "host=$FQDN port=5432 dbname=ucokudos user=ucokudos sslmode=require" -c "SELECT accion, resultado, ocurrido_en FROM auditoria.registro ORDER BY ocurrido_en DESC LIMIT 4;"
   ```
   Deben aparecer `KUDO_PUBLICADO`, `AVISO_CORREO`, `AVISO_WHATSAPP` y `AVISO_PUSH` en `EXITO`. El
   push sale `OMITIDO` si el estudiante no tenía la app abierta.
6. **La observabilidad:** en el portal, *ucokudos-insights → Asignación de aplicaciones* (el mapa) y
   *Rendimiento → POST /api/kudos → Muestras* (la transacción completa).

---

## 8 · Problemas conocidos

| Síntoma | Causa | Qué hacer |
|---|---|---|
| `az containerapp revision restart` falla con «Express environments do not support revision restart» | El entorno de Container Apps es de tipo Express | Hacer un `az containerapp update --set-env-vars` que cambie una variable. Así se despliega de nuevo y lee los secretos nuevos |
| `psql: could not translate host name "port=5432"` | La variable `FQDN` quedó vacía porque Cloud Shell se reinició | Volver a definir `FQDN` (sección 4.3) |
| `az postgres flexible-server db create` pide `--name` | Las opciones cortas cambiaron entre versiones de la CLI | Usar las opciones largas, como en este manual |
| Al publicar, la API responde 403 | La cuenta de Auth0 no está vinculada con una persona activa | Hacer el paso 6 de la sección 5.1 |
| El front no inicia sesión | La aplicación del front no está autorizada en la API de Auth0 | Activarla en la pestaña *Application Access* de la API (sección 5.1) |
| El WhatsApp no llega y el aviso queda en FALLO | El token venció, o el celular no está en la lista **Para** del número de prueba | Usar el token permanente (sección 5.3) y verificar el número |
| El correo cae en spam | El remitente es de un dominio gratuito | Verificar un dominio propio en Brevo (`ADR-019`) |
| El celular sigue mostrando la versión vieja de la PWA | El *service worker* anterior sigue en caché | La PWA se actualiza sola desde `7b08168`: basta con cerrarla y volver a abrirla |
