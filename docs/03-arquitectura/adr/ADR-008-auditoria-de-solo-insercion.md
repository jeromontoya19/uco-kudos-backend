# `ADR-008` · Auditoría de solo inserción, sin datos personales

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-08`, `D-10`, `D-16`, `D-22`; `DRV-006`, `DRV-009`; `CN-04`

## Contexto

El enunciado exige que todo el proceso de publicar quede auditado (paso 5), y Cumplimiento es el
atributo de calidad número 4. La Ley 1581 permite pedir que se borren los datos personales (`CN-04`),
y eso choca con una auditoría que no se borra (`T-4`).

## Decisión

- Se registra **toda acción que cambie algo**: las publicaciones, cada intento de aviso con su
  resultado, los vetos, las aprobaciones y los cambios de categorías. De cada una queda quién, qué,
  cuándo y el resultado.
- Los registros **solo se agregan**: nunca se editan ni se borran.
- La auditoría guarda **solo identificadores**, nunca el nombre, el correo ni el celular. Si alguien
  pide borrar sus datos, se eliminan de su cuenta, y la auditoría queda intacta pero sin apuntar a una
  persona identificable (`D-22`).
- Ninguna respuesta de la API incluye el celular de otra persona (`D-16`).
- Un **veto oculta el contenido del muro y lo conserva**, con quién lo vetó, cuándo y por qué
  (`D-10`).

## Alternativas

- **Auditar solo el flujo de publicar:** es el mínimo del enunciado, pero deja sin evidencia los vetos
  y las aprobaciones.
- **Una auditoría que un administrador puede corregir:** más flexible, pero pierde valor como
  evidencia.
- **Conservar los datos personales en la auditoría:** choca con la Ley 1581.

## Consecuencias

**Se gana:** evidencia completa de lo que pasó (`ESC-09`), y cumplir la ley sin romper la auditoría
(`ESC-10`).

**Cuesta:** la auditoría crece sin límite. `[!]` No hay una política de retención: queda pendiente
para cuando haya un volumen real.

## Escenarios

`ESC-09`, `ESC-10`, `ESC-11`

## Observaciones

Cierra `T-4`.
