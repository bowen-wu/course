CREATE TABLE PERMISSION
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(50) UNIQUE NOT NULL,
    role_id    INTEGER            NOT NULL,
    created_on TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on TIMESTAMP          NOT NULL DEFAULT now(),
    status     VARCHAR(10)        NOT NULL DEFAULT 'OK'
);
