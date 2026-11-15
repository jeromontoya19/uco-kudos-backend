# UCO Kudos — Diseño detallado del back

> **Borrador:** preparado con IA a partir del código del back. Revísalo y borra esta nota cuando el
> documento sea tuyo.

**v0.1 · 15-nov-2026 · Jerónimo Montoya Londoño**

> Cómo está organizado por dentro el código de la API. Complementa el nivel 3 del
> [modelo C4](C4.md), que muestra los componentes.
>
> **Los diagramas están en draw.io:** [`imagenes/UCO-Kudos-diseno.drawio`](imagenes/UCO-Kudos-diseno.drawio).
> Los PNG de este documento se exportan de ese archivo.

## Paquetes del back

![Paquetes del back de UCO Kudos](imagenes/paquetes-del-back.png)

El código vive en `co.edu.uco.ucokudos` y se divide en cinco módulos, uno por cada hexágono de
`ADR-002`: `kudos`, `avisos`, `identidad`, `auditoria` y `moderacion`. Hoy `moderacion` solo está
diseñado, y sus paquetes están vacíos.

**Dentro de cada módulo** hay cuatro partes:

| Paquete | Qué guarda | De qué depende |
|---|---|---|
| `aplicacion.puerto.entrada` | Los contratos que usan los controladores y los otros módulos | Del dominio, si hace falta |
| `aplicacion` | Los casos de uso y los puertos de salida (por ejemplo, los repositorios) | Del dominio |
| `infraestructura` | Los adaptadores: web, persistencia, configuración y, según el módulo, seguridad, canales, canal en vivo o tareas programadas | De la aplicación y de sus puertos |
| `dominio` | Las reglas del negocio | De nada: ni de Spring ni de la base |

**Entre módulos** solo se usa el `aplicacion.puerto.entrada` del otro:

| Módulo | Usa de otro módulo |
|---|---|
| `kudos` | `ProgramarAvisos` y `KudoPorAvisar` de `avisos`; `ConsultarPersonas` de `identidad`; `RegistrarEnAuditoria` de `auditoria` |
| `avisos` | `ConsultarContactos` y `Contacto` de `identidad`; `RegistrarEnAuditoria` de `auditoria` |
| `identidad` | Nada |
| `auditoria` | Nada |

Así no hay ciclos entre módulos, y cada uno se puede probar con dobles de los puertos de los demás.

**Cómo se vigila:** `ReglasDeArquitecturaTest` (ArchUnit) corre en cada build de la CI y la detiene si
se rompe una de estas reglas:
1. El dominio no depende de frameworks (Spring, JPA, Servlet, Jackson).
2. El dominio no depende de `aplicacion` ni de `infraestructura`.
3. La `aplicacion` no depende de la `infraestructura`.
4. Los módulos no tienen ciclos entre sí.
5. Un módulo solo usa el `aplicacion.puerto.entrada` de otro.

## Diagramas de secuencia

Se agregan cuando el despacho inmediato de los avisos esté en `main`, porque cambia el flujo
principal: hoy los avisos esperan la vuelta de 5 s del despachador, y con ese cambio salen apenas se
confirma la publicación.
