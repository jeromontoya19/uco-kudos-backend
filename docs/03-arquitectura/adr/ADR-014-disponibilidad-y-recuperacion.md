# `ADR-014` · Disponibilidad y recuperación con presupuesto limitado

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-07`, `D-31`, `D-40`, `D-43`, `D-53`; `DRV-004`; `CN-03`, `CN-19`; `T-9`

## Contexto

Disponibilidad es el atributo de calidad número 3, y Confiabilidad el número 2. Con el crédito de
Azure for Students (`D-53`), la API ya no tiene por qué dormirse, pero el crédito es finito (`T-9`) y
una copia guardada en el mismo proveedor no protege de un desastre de ese proveedor. Además,
`DRV-004` pide una estrategia de recuperación de los adjuntos.

## Decisión

- La API corre en una **instancia siempre encendida** (`D-53`), sin arranque en frío.
- **Un ping de salud periódico**, desde un monitor gratuito **fuera de Azure**, mide la disponibilidad
  de la API y avisa si se cae. La meta es **99 % en la semana de evaluación**: como máximo unas 1,7
  horas fuera de servicio. Si se pasa al plan B con capas gratuitas, el mismo ping la mantiene además
  despierta (`D-31`).
- **Una copia diaria automática** de la base y de los adjuntos, **guardada fuera del proveedor
  principal**. Se pueden perder como máximo **24 horas** de información, y recuperar todo tarda **4
  horas** como máximo.
- La restauración **se prueba al menos una vez** antes de la entrega.

## Alternativas

- **Seguir en la capa gratuita, con el ping manteniéndola despierta:** era esta decisión antes de
  `D-53`, y ahora es el plan B.
- **Aceptar el arranque en frío** y despertar la API a mano antes de la demo.
- **No perder nada y recuperar en 1 hora:** exige recuperar a cualquier punto en el tiempo también
  fuera del proveedor. Azure lo da para la base dentro del propio proveedor (`ADR-025`), pero eso no
  protege de un desastre del proveedor.
- **No garantizar la recuperación:** no cumple `DRV-004`.

## Consecuencias

**Se gana:** sin arranque en frío, disponibilidad medible desde fuera del proveedor y una recuperación
probada, dentro de la meta de gasto.

**Cuesta:** una parte del crédito cada mes, y si el crédito se agota, la URL deja de responder
(`T-9`). Entre dos copias se pueden perder hasta 24 horas de información.

## Escenarios

`ESC-06`, `ESC-07`

## Observaciones

El monitor se propone en `ADR-022` y el mecanismo de copias en `ADR-025`.

`[:W:]` 12-nov-2026: antes de `D-53` el título era «Disponibilidad y recuperación a costo cero», y el
ping mantenía despierta la API en la capa gratuita. Con el crédito, la instancia queda siempre
encendida y el ping solo mide. La meta del 99 % y la estrategia de copias no cambian.
