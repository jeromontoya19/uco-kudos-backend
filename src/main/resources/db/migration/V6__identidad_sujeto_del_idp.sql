-- Une a cada persona con su cuenta en el IdP: el «sub» del token OIDC (ADR-007). El rol lo sigue
-- decidiendo la API, no el IdP. Se llena al crear la cuenta en el IdP; mientras esté vacío, la persona
-- no puede iniciar sesión.
ALTER TABLE identidad.persona ADD COLUMN sujeto_idp TEXT UNIQUE;
