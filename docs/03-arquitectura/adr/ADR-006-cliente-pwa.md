# `ADR-006` · El cliente es una PWA, con el celular como prioridad

**Estado:** Aceptada · **Fecha:** 12-nov-2026
**Deriva de:** `D-17`, `D-21`, `D-29`; `DRV-001`; `CN-13`, `CN-17`, `CN-20`, `CN-21`

## Contexto

Cerca del 90 % de los usuarios entra desde el celular (`DRV-001`), el docente tiene que poder usar el
sistema desde una URL pública (`CN-13`) y el diseño es *mobile first* (`CN-17`).

## Decisión

- El cliente es una **aplicación web instalable (PWA)**: una sola URL pública, que se puede instalar en
  la pantalla de inicio, sin tiendas de aplicaciones.
- Funciona en **cualquier navegador moderno, con el celular como prioridad**. El diseño y las pruebas
  principales se hacen en celular; el escritorio se verifica con la prueba de punta a punta en
  Chromium, y Safari se prueba a mano.
- El muro muestra **miniaturas** de las imágenes; el original se descarga al abrir el Kudo (`D-29`).
- Publicar un Kudo sin adjuntos cabe en **una sola pantalla** (`ESC-16`).

## Alternativas

- **Web responsive sin instalación:** lo más rápido de hacer, pero sin ícono ni experiencia de
  aplicación.
- **Aplicación nativa Android:** experiencia nativa, pero hay que compilar y repartir el APK, y no se
  cumple «entrar desde una URL pública».

## Consecuencias

**Se gana:** un solo cliente para celular y escritorio, sin tiendas ni compilaciones por plataforma.

**Cuesta:** en iPhone, instalar una PWA es más limitado; por eso Safari se prueba a mano.

## Escenarios

`ESC-13`, `ESC-16`

## Observaciones

El framework del front se propone en `ADR-016`.
