-- Auditoría (ADR-008): quién, qué, cuándo y con qué resultado. Solo guarda identificadores, nunca
-- nombres, correos ni celulares, y solo admite inserciones: la base rechaza cualquier cambio o borrado.
CREATE TABLE auditoria.registro (
    id            UUID        PRIMARY KEY,
    ocurrido_en   TIMESTAMPTZ NOT NULL,
    accion        TEXT        NOT NULL CHECK (btrim(accion) <> ''),
    actor         UUID        NOT NULL,
    tipo_recurso  TEXT        NOT NULL CHECK (btrim(tipo_recurso) <> ''),
    id_recurso    UUID        NOT NULL,
    resultado     TEXT        NOT NULL CHECK (resultado IN ('EXITO', 'FALLO', 'OMITIDO'))
);

CREATE INDEX registro_por_recurso ON auditoria.registro (id_recurso, ocurrido_en);

CREATE FUNCTION auditoria.rechazar_cambios() RETURNS TRIGGER
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'La auditoría es de solo inserción (ADR-008)';
END;
$$;

CREATE TRIGGER registro_sin_cambios
    BEFORE UPDATE OR DELETE ON auditoria.registro
    FOR EACH ROW EXECUTE FUNCTION auditoria.rechazar_cambios();

CREATE TRIGGER registro_sin_vaciado
    BEFORE TRUNCATE ON auditoria.registro
    FOR EACH STATEMENT EXECUTE FUNCTION auditoria.rechazar_cambios();
