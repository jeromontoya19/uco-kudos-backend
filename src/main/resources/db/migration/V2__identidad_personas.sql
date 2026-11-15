-- Personas de la comunidad (módulo de identidad). Cada persona tiene un solo rol activo
-- (exclusividad del enunciado), y la universidad tiene un solo Rector y un solo Coordinador
-- Académico activos (unicidad). Los coordinadores de facultad y de programa necesitan las tablas de
-- facultades y programas, que llegan con su propia migración.
CREATE TABLE identidad.persona (
    id        UUID PRIMARY KEY,
    nombre    TEXT    NOT NULL CHECK (btrim(nombre) <> ''),
    correo    TEXT    NOT NULL UNIQUE,
    celular   TEXT    NOT NULL UNIQUE,
    rol       TEXT    NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'RECTOR', 'COORDINADOR_ACADEMICO',
                                              'COORDINADOR_FACULTAD', 'COORDINADOR_PROGRAMA',
                                              'DOCENTE', 'ESTUDIANTE')),
    activa    BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX persona_un_solo_rector_y_coordinador_academico_activos
    ON identidad.persona (rol)
    WHERE activa AND rol IN ('RECTOR', 'COORDINADOR_ACADEMICO');

-- Una persona de prueba por rol, para la demostración. Los correos y celulares son ficticios: los
-- datos reales del destinatario de la demo se cambian en la base desplegada, nunca en el repositorio.
INSERT INTO identidad.persona (id, nombre, correo, celular, rol) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Administrador de prueba',          'administrador@example.com',        '+570000000001', 'ADMINISTRADOR'),
    ('00000000-0000-0000-0000-000000000002', 'Rector de prueba',                 'rector@example.com',               '+570000000002', 'RECTOR'),
    ('00000000-0000-0000-0000-000000000003', 'Coordinador académico de prueba',  'coordinador.academico@example.com', '+570000000003', 'COORDINADOR_ACADEMICO'),
    ('00000000-0000-0000-0000-000000000004', 'Coordinador de facultad de prueba', 'coordinador.facultad@example.com', '+570000000004', 'COORDINADOR_FACULTAD'),
    ('00000000-0000-0000-0000-000000000005', 'Coordinador de programa de prueba', 'coordinador.programa@example.com', '+570000000005', 'COORDINADOR_PROGRAMA'),
    ('00000000-0000-0000-0000-000000000006', 'Docente de prueba',                'docente@example.com',              '+570000000006', 'DOCENTE'),
    ('00000000-0000-0000-0000-000000000007', 'Estudiante de prueba',             'estudiante@example.com',           '+570000000007', 'ESTUDIANTE');
