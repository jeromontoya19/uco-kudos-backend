# Documentación de UCO Kudos

El trabajo en curso se hace en la rama `documentacion-desarrollo`. Cada documento entra a `main` por
pull request cuando queda completo.

## En qué orden se lee

| # | Documento | Qué responde |
|---|---|---|
| 0 | [Decisiones del autor](02-drivers-arquitectonicos/DECISIONES.md) | Lo que el enunciado no dice y se decidió en rondas de preguntas |
| 1 | [Drivers arquitectónicos](02-drivers-arquitectonicos/DRIVERS_ARQUITECTONICOS.md) | Qué se construye, bajo qué restricciones y qué atributos de calidad mandan |
| 2 | [Alternativa de solución](03-arquitectura/ALTERNATIVA_DE_SOLUCION.md) | Qué estilo se eligió, con qué bloques (ABB) y con qué tecnología (SBB) |
| 3 | [C4](03-arquitectura/C4.md) | Contexto, contenedores y despliegue |
| 4 | [Spikes](03-arquitectura/spikes/) | Las pruebas cortas que respaldan las decisiones riesgosas |
| 5 | [ADR](03-arquitectura/adr/README.md) | Cada decisión, con su contexto, alternativas y consecuencias |
| 6 | Diseño detallado · Prácticas · Manual · Evidencias | Se agregan al construir |

## Convenciones

| Prefijo | Qué identifica | Dónde vive |
|---|---|---|
| `D-nn` | Decisión del autor | `DECISIONES.md`, una sección por ronda |
| `T-nn` | Tensión entre dos decisiones, con su estado | `DECISIONES.md`, sección *Tensiones* |
| `RF-nnn` | Funcionalidad significativa | Drivers §2 |
| `CN-nn` | Restricción. Una sola serie: las de negocio desde `CN-01` y las técnicas desde `CN-10` | Drivers §3 y §4 |
| `DRV-nnn` | Driver: los 7 del enunciado (obligatorios) y los que identifica el autor | Drivers §5 |
| `P-nn` | Pregunta de caracterización | Drivers §7 |
| `ESC-nn` | Escenario de calidad | Drivers §8 |
| `ALT-n` | Alternativa de solución | Alternativa §2 |
| `ABB-nn` | Bloque de construcción de arquitectura | Alternativa §3 |
| `SPK-nn` | Spike | `03-arquitectura/spikes/` |
| `ADR-nnn` | Decisión de arquitectura | `03-arquitectura/adr/` |

- Los identificadores **nunca se renumeran** y **no se borra nada**: lo que cambia se marca y se explica.
- Todo archivo generado con IA lleva el prefijo `IA_` y la cabecera «REGISTRO DE IA · NO ES FUENTE ·
  SIN REVISAR» hasta que se revisa. Lo que no lleva prefijo lo escribió y lo revisó el autor, y es lo que manda.
