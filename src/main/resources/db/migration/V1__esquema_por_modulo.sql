-- Un esquema por módulo (ADR-002): cada módulo guarda sus tablas en el suyo, y la frontera entre
-- módulos del código también se ve en la base. Las tablas llegan en las migraciones siguientes.
CREATE SCHEMA kudos;
CREATE SCHEMA avisos;
CREATE SCHEMA identidad;
CREATE SCHEMA auditoria;
CREATE SCHEMA moderacion;
