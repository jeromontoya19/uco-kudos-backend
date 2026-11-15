# `ADR-023` · CI/CD, análisis estático y registro de imágenes

**Estado:** Aceptada el 14-nov-2026 (`D-56`) · **Fecha:** 12-nov-2026
**Deriva de:** `D-30`, `D-38`, `D-54`, `D-56`; `ADR-013`, `ADR-022`; `CN-11`, `CN-16`, `CN-25`; `DRV-007`
**La confirmaron:** el primer pipeline en verde y GitHub Actions entrando a Azure por OIDC (`SPK-04`,
pasos 10 y 11)

## Contexto

El enunciado exige CI, CD, Sonar y Trivy, con evidencia (`CN-11`). Las ramas y sus compuertas ya
están decididas (`ADR-013`), al igual que la cobertura mínima: 70 % en el dominio y los casos de uso
(`D-30`). Los repositorios están en GitHub y son públicos.

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

**CI/CD:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| GitHub Actions | Bajo | Bajo | Alto | Bajo | Alto | Alto |
| GitLab CI | Bajo | Medio | Alto | Medio | Alto | Alto |
| CircleCI | Bajo | Medio | Alto | Medio | Alto | Medio |

**Análisis estático:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| SonarQube Cloud | Bajo | Bajo | Alto | Bajo | Alto | Alto |
| SonarQube Community dentro de la CI | Bajo | Medio | Medio | Medio | Medio | Medio |

**Registro de imágenes:**

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| GitHub Container Registry | Bajo | Bajo | Alto | Bajo | Alto | Alto |
| Docker Hub | Bajo | Bajo | Alto | Medio | Alto | Alto |
| Azure Container Registry | Medio | Bajo | Alto | Bajo | Alto | Alto |

Azure Container Registry cobra el registro por día y duplica lo que GitHub Container Registry da sin
costo para repositorios públicos, junto al código. Container Apps descarga la imagen de cualquiera de
los dos.

Trivy no se compara: el enunciado lo exige (`CN-11`).

## Decisión

**GitHub Actions, SonarQube Cloud, GitHub Container Registry y Trivy.** Todo vive donde ya están los
repositorios, y para repositorios públicos no tiene costo.

- **CI rápida** en `codigo-desarrollo`: compilar, pruebas unitarias y de arquitectura.
- **CI completa** en los PR a `codigo-testing`: pruebas de integración, cobertura ≥ 70 % en el
  dominio y los casos de uso, el *quality gate* de Sonar, y Trivy sobre las dependencias y sobre la
  imagen.
- **CD** al hacer merge en `main`: construir la imagen, publicarla en el registro con la etiqueta del
  commit y desplegarla en Container Apps (`ADR-022`). El front se despliega con la acción de Azure
  Static Web Apps.
- **Cómo entra GitHub Actions a Azure:** con OIDC, por una credencial federada registrada en Azure
  para el repositorio y la rama `main`, con permiso solo sobre el grupo de recursos. No se guarda
  ninguna contraseña de Azure en GitHub. La suscripción quedó en el directorio de la cuenta personal
  del autor, donde sí se pueden registrar aplicaciones (`SPK-04`).
- **Volver atrás** (`ESC-15`): un workflow que se lanza a mano y redespliega una etiqueta anterior, o
  activa la revisión anterior de Container Apps, sin reconstruir la imagen.
- Las acciones de terceros se fijan por su SHA de commit, no por etiqueta (cadena de suministro).

## Consecuencias

**Se gana:** evidencia automática de CI, CD, Sonar y Trivy en cada PR, sin costo, y un CD sin
contraseñas de larga vida.

**Cuesta:** atar el pipeline a GitHub y registrar en Azure la identidad del pipeline. Trivy frena un
cambio con cualquier vulnerabilidad CRÍTICA o ALTA que ya tenga corrección (ver Observaciones).

## Escenarios

`ESC-15`

## Observaciones

**Lo que mostró el pipeline montado (13 y 14-nov-2026):**
- **CI rápida:** corre en cada push a `codigo-desarrollo`, sin base ni Docker.
- **CI completa:** en cada PR a `codigo-testing` y a `main`. Corre las pruebas de integración contra
  PostgreSQL en Testcontainers, la cobertura, el *quality gate* de Sonar y Trivy.
  - El primer PR (#1) no pasó el *quality gate* por la cobertura del código nuevo. Se excluyó de la
    cobertura la clase de arranque, que solo llama a Spring, y pasó.
  - Evidencia: `docs/evidencias/ci/ci-pr1-sonar-quality-gate.png`.
- **Umbral de Trivy:** frena el cambio si hay una vulnerabilidad CRÍTICA o ALTA que ya tenga
  corrección, en las dependencias o en la imagen.
- **CD por OIDC:** funciona sin contraseñas de Azure (`SPK-04`, paso 10).
  - GitHub presenta el sujeto del token con los ID fijos del dueño y del repositorio
    (`repo:jeromontoya19@177062128/uco-kudos-backend@1454753573:ref:refs/heads/main`). Por eso la
    identidad tiene una segunda credencial federada, `github-main-ids`, con ese sujeto.
  - La imagen se publica en GitHub Container Registry, que es público, así que Container Apps la
    descarga sin credenciales.
- **Vuelta atrás:** el workflow manual, lanzado desde `main`, tardó 47 s, y el regreso, 44 s
  (`SPK-04`, paso 11; `ESC-15`).
- **Vuelta atrás del front:** Static Web Apps no guarda versiones y el front no tiene imagen que
  redesplegar, así que su workflow manual compila el commit anterior con su propio `package-lock` y lo
  publica. Solo acepta commits de la historia de `main`. Tardó 38 s, y el regreso, 44 s, y la
  compilación dio el mismo JavaScript que se había publicado (`SPK-04`, paso 11; `ESC-15`).

Con esto se cumplió lo que confirmaba este ADR, que el autor aceptó el 14-nov-2026 (`D-56`). Ese
mismo día quedó comprobado también el front: su CI completa pasó, y su CD lo desplegó con la acción de
Static Web Apps en 57 s (`SPK-04`, paso 10).

`[:W:]` 14-nov-2026: en los PR #1 y #2 del front, la CI completa corrió sin Sonar, porque faltaba el
secreto `SONAR_TOKEN`. Desde el PR #4, Sonar corre en la CI y el análisis automático de SonarQube Cloud
quedó apagado. El *quality gate* pasó, con 100 % de cobertura, después de corregir tres reglas en los
workflows y en el script de verificación: `npm ci --ignore-scripts`, `curl` solo por HTTPS y `[[` en
las condiciones. Evidencia: `docs/evidencias/ci/ci-pr4-front-sonar-quality-gate.png`.
