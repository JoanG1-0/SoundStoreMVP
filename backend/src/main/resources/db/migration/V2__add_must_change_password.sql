-- ============================================================
-- V2: Añadir columna must_change_password a users
-- Requerido por RF-AU-05: usuarios creados por Admin deben
-- cambiar su contraseña en el primer acceso.
-- ============================================================

ALTER TABLE users
    ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT false;
