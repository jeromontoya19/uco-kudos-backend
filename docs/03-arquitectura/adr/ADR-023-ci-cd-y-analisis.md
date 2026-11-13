# `ADR-023` · CI/CD, análisis estático y registro de imágenes

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-30`, `D-38`, `D-54`; `ADR-013`, `ADR-022`; `CN-11`, `CN-16`, `CN-25`; `DRV-007`
**La confirma:** el primer pipeline en verde; que GitHub Actions entra a Azure, `SPK-04`

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

## Propuesta

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

**Cuesta:** atar el pipeline a GitHub y registrar en Azure la identidad del pipeline. El umbral exacto
con el que Trivy frena un cambio se fija al montar el pipeline.

## Escenarios

`ESC-15`

## Observaciones

—
