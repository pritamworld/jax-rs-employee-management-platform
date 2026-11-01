-- Run as an Oracle DBA/admin; choose strong credentials and store them securely.
CREATE USER employee_app IDENTIFIED BY "CHANGE_ME_STRONG_PASSWORD";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER TO employee_app;
ALTER USER employee_app QUOTA UNLIMITED ON USERS;

-- Connect as employee_app and run src/main/resources/db/migration/V1__initial_schema.sql.
-- For production, use a dedicated migration identity and least-privilege grants.
