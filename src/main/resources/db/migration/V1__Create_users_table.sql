CREATE TABLE USERS
(
    id                 SERIAL PRIMARY KEY,
    username           VARCHAR(50) UNIQUE NOT NULL,
    encrypted_password VARCHAR(50)        NOT NULL,
    created_on         TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on         TIMESTAMP          NOT NULL DEFAULT now(),
    status             VARCHAR(10)        NOT NULL DEFAULT 'OK'
);

-- status: OK DELETED
