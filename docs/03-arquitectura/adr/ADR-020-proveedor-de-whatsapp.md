# `ADR-020` · Proveedor de WhatsApp

**Estado:** Propuesta · **Fecha:** 12-nov-2026
**Deriva de:** `D-38`; `ADR-003`; `CN-14`, `CN-18`; `DRV-003`
**La confirma:** `SPK-01`

## Contexto

WhatsApp tiene que funcionar de verdad y con evidencia (`CN-14`). Es el canal más riesgoso: solo deja
iniciar una conversación con plantillas aprobadas, y un número de prueba solo envía a unos pocos
números verificados (`CN-18`).

## Alternativas

Escala **Bajo / Medio / Alto**: en costo, complejidad y riesgo, Bajo es mejor; en escalabilidad,
mantenibilidad y evolución, Alto es mejor.

| Candidato | Costo | Complejidad | Escalabilidad | Riesgo | Mantenibilidad | Evolución |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| WhatsApp Cloud API de Meta (número de prueba) | Bajo | Medio | Alto | Medio | Alto | Alto |
| Twilio WhatsApp Sandbox | Bajo | Bajo | Bajo | Medio | Alto | Medio |

**De dónde sale el riesgo:** con Meta, la plantilla tiene que estar aprobada y los destinatarios
verificados. Con Twilio Sandbox, cada destinatario tiene que unirse con un código, y esa unión caduca.

## Propuesta

**La WhatsApp Cloud API de Meta con un número de prueba**, porque es el proveedor oficial y escala sin
cambiar de API. **Plan B:** Twilio WhatsApp Sandbox, detrás del mismo adaptador (`D-38`). **En local**,
un doble que registra los mensajes en vez de enviarlos.

El primer paso no espera al spike: **solicitar ya la plantilla del aviso de Kudo**, porque su
aprobación puede tardar.

## Consecuencias

**Se gana:** el canal oficial, y un plan B que no exige tocar el caso de uso de publicar.

**Cuesta:** en la demo solo reciben los números verificados, y fuera de la ventana de 24 horas solo
se pueden enviar plantillas aprobadas.

## Escenarios

`ESC-05`, `ESC-08`

## Observaciones

Antes de cada demo, el destinatario le escribe primero al número de prueba para abrir la ventana de
24 horas.
