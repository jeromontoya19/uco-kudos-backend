-- Outbox de avisos (ADR-003): al publicar se guarda un aviso pendiente por canal, en la misma
-- transacción del Kudo, y un despachador en segundo plano los envía. Guarda el nombre de la categoría
-- tal como estaba al publicar, para escribir el mensaje sin consultar otro módulo.
CREATE TABLE avisos.aviso (
    id                  UUID        PRIMARY KEY,
    kudo                UUID        NOT NULL,
    autor               UUID        NOT NULL,
    destinatario        UUID        NOT NULL,
    categoria           TEXT        NOT NULL,
    canal               TEXT        NOT NULL CHECK (canal IN ('CORREO', 'WHATSAPP', 'PUSH')),
    estado              TEXT        NOT NULL CHECK (estado IN ('PENDIENTE', 'ENTREGADO', 'FALLIDO', 'OMITIDO')),
    intentos            INTEGER     NOT NULL DEFAULT 0 CHECK (intentos >= 0),
    proximo_intento_en  TIMESTAMPTZ NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL,
    UNIQUE (kudo, canal)
);

-- El despachador solo busca los pendientes cuyo próximo intento ya llegó.
CREATE INDEX aviso_pendiente_por_proximo_intento
    ON avisos.aviso (proximo_intento_en)
    WHERE estado = 'PENDIENTE';
