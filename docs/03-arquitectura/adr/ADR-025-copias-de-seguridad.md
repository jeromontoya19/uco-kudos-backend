# `ADR-025` · Mecanismo de las copias de seguridad

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-40`, `D-54`; `ADR-014`, `ADR-017`; `DRV-004`
**La confirma:** `SPK-06` (restauración probada)

## Contexto

`ADR-014` decidió una copia diaria de la base y de los adjuntos, fuera del proveedor principal, con
≤ 24 h de pérdida y ≤ 4 h de recuperación (`ESC-06`). Falta con qué se hace.

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Tarea programada en GitHub Actions | Bajo | Medio | Medio | Medio | Alto | Medio |
| Las copias del propio proveedor | Bajo | Bajo | Medio | Alto | Alto | Medio |
| Copia manual | Bajo | Bajo | Bajo | Alto | Bajo | Bajo |

**De dónde sale el riesgo:** las copias del propio proveedor viven en el mismo proveedor, así que no
protegen de un desastre suyo, que es justo el caso de `ESC-06`. Una copia manual depende de que alguien
se acuerde de hacerla.

## Propuesta

**Una tarea programada diaria en GitHub Actions** que vuelca la base y copia los adjuntos a un segundo
almacén de **otro proveedor** (por ejemplo, Cloudflare R2 o Backblaze B2), cifrados. Como la base solo
acepta conexiones de Azure y de las IP autorizadas (`ADR-017`), la tarea abre una regla de firewall
temporal para la IP del runner y la cierra al terminar. `SPK-06` prueba la restauración completa y
mide cuánto tarda; se vuelve a probar antes de la entrega.

**Complemento, no reemplazo:** el servidor flexible de Azure hace copias automáticas de la base con
restauración a un punto en el tiempo (7 días por defecto). Sirven ante un error humano, como borrar
datos por equivocación, pero no ante un desastre del proveedor.

## Consecuencias

**Se gana:** la copia queda fuera del proveedor principal, sin costo y con evidencia en cada ejecución;
y, dentro de Azure, la base se puede devolver a cualquier momento de la última semana.

**Cuesta:** guardar en la CI los secretos de los dos almacenes y el acceso a Azure, y mantener el
script de restauración.

## Escenarios

`ESC-06`

## Observaciones

—
