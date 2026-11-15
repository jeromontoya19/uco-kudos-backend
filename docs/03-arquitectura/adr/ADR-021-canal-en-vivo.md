# `ADR-021` · Mecanismo del canal en vivo

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-04`, `D-15`; `ADR-003`, `ADR-011`; `DRV-003`
**La confirma:** `SPK-03`

## Contexto

El push solo se envía si el destinatario tiene la aplicación abierta (`D-04`), y tiene que llegar en
≤ 2 s (`ESC-14`). Si no está conectado, el push se omite y se audita, así que hace falta saber quién
está conectado. Hay una sola instancia (`ADR-011`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Server-Sent Events (SSE) | Bajo | Bajo | Medio | Bajo | Alto | Medio |
| WebSocket | Bajo | Medio | Medio | Medio | Medio | Alto |
| Consulta periódica desde el cliente | Bajo | Bajo | Bajo | Bajo | Alto | Bajo |

La consulta periódica no cumple los 2 s sin generar mucho tráfico, y tampoco dice quién está
conectado.

## Propuesta

**Server-Sent Events.** El canal solo necesita enviar del servidor al cliente; SSE es HTTP normal, el
navegador se reconecta solo, y saber si alguien está conectado es saber si tiene su flujo abierto.
WebSocket agrega un canal de ida y vuelta que nadie usa.

`[!]` Lo que el spike tiene que resolver: el navegador no deja agregar cabeceras a una conexión SSE,
así que el token debe viajar en una cookie o como un token corto en la URL. **Plan B:** WebSocket.

## Consecuencias

**Se gana:** la opción más simple que cumple `ESC-14`, sin piezas nuevas.

**Cuesta:** cada usuario conectado mantiene una conexión abierta contra la única instancia. Con varias
instancias, el push necesita un relevo entre ellas (`T-3`, evolución de `ADR-011`).

## Escenarios

`ESC-14`

## Observaciones

`[!]` En Container Apps (`ADR-022`), como detrás de cualquier balanceador, una conexión que pasa unos
minutos sin tráfico se corta. El canal envía un comentario de latido cada 30 s, y si aun así se
corta, el navegador se reconecta solo. `SPK-03` lo comprueba contra la URL pública. El ingreso de
Container Apps también admite WebSocket, por si se pasa al plan B.
