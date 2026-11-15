# `ADR-009` · Adjuntos: tipo real y tamaño; antivirus solo en local

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-03`, `D-11`, `D-29`, `D-41`; `DRV-004`; `CN-03`; `T-7`

## Contexto

`DRV-004` pide estrategias de almacenamiento, seguridad, recuperación y trazabilidad para los
adjuntos. Un archivo malicioso es la vía de ataque más directa contra un sistema que acepta archivos,
y un antivirus necesita unos 2 GB de RAM, más de lo que tiene la instancia de producción, que además
la comparte con la API (`CN-03`).

## Decisión

- Se aceptan **imágenes y PDF**, de **hasta 5 MB** cada uno y **como máximo 3 por Kudo**.
- Antes de guardar se revisa **el tipo real del archivo** —su contenido, no la extensión— y su tamaño.
- Los archivos se guardan **fuera de la base**, en un almacén de archivos (`ADR-017`). Se entregan
  **solo con sesión iniciada** y con encabezados que impiden que el navegador los ejecute.
- Al subir una imagen se genera su **miniatura** para el muro (`D-29`).
- Cada subida queda en la auditoría con quién, cuándo y la huella del archivo (trazabilidad).
- **En local**, además, cada archivo pasa por un **antivirus (ClamAV)** con cuarentena, que se
  demuestra en la sustentación con el archivo de prueba EICAR. **En producción no hay antivirus**
  hasta que haya presupuesto para unos 2 GB de RAM.

## Alternativas

- **Antivirus también en producción:** no cabe junto a la API en la instancia de producción, y una
  segunda instancia solo para el antivirus duplicaría el gasto mensual (`D-53`).
- **Revisar solo la extensión:** cualquiera la cambia.

## Consecuencias

**Se gana:** un archivo peligroso o de un tipo no permitido nunca se guarda (`ESC-03`), y el muro
carga liviano en el celular.

**Cuesta:** local y producción revisan los archivos de forma distinta (`T-7`, aceptada). La
recuperación de los adjuntos depende de las copias de seguridad de `ADR-014`.

## Escenarios

`ESC-03`, `ESC-06`

## Observaciones

**Disparador para el antivirus en producción:** presupuesto para unos 2 GB de RAM.
