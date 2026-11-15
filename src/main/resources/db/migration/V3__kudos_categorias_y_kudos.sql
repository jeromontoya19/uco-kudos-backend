-- Categorías y Kudos (módulo de Kudos). El autor y el destinatario son personas del módulo de
-- identidad: no llevan llave foránea hacia ese esquema, porque cada módulo es dueño de sus datos y
-- solo se habla con los otros por sus puertos (ADR-002, D-55).
CREATE TABLE kudos.categoria (
    id      UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre  TEXT    NOT NULL UNIQUE CHECK (btrim(nombre) <> ''),
    activa  BOOLEAN NOT NULL DEFAULT TRUE
);

-- Las 15 categorías mínimas del enunciado.
INSERT INTO kudos.categoria (nombre) VALUES
    ('Rendimiento Académico'),
    ('Mejor Investigador'),
    ('Empatía'),
    ('Comunicación Asertiva'),
    ('Orientación al Logro'),
    ('Trabajo en Equipo'),
    ('Trabajo Bajo Presión'),
    ('Visión Estratégica'),
    ('Innovación'),
    ('Liderazgo'),
    ('Transformación Digital'),
    ('Mentor Inspirador'),
    ('Espíritu UCO'),
    ('Servicio a la Comunidad'),
    ('Compromiso Institucional');

-- El contenido no tiene límite de longitud (enunciado), pero no puede quedar vacío. Nadie se da un
-- Kudo a sí mismo (D-12).
CREATE TABLE kudos.kudo (
    id            UUID        PRIMARY KEY,
    autor         UUID        NOT NULL,
    destinatario  UUID        NOT NULL,
    categoria     UUID        NOT NULL REFERENCES kudos.categoria (id),
    contenido     TEXT        NOT NULL CHECK (btrim(contenido) <> ''),
    publicado_en  TIMESTAMPTZ NOT NULL,
    CHECK (autor <> destinatario)
);

-- El muro se lee del más reciente al más antiguo.
CREATE INDEX kudo_por_fecha_de_publicacion ON kudos.kudo (publicado_en DESC);
