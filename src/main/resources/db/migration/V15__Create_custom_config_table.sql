CREATE TABLE CUSTOM_CONFIG
(
    id         SERIAL PRIMARY KEY,
    name       varchar(64) UNIQUE NOT NULL,
    value      varchar(64)        NOT NULL,
    created_on TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on TIMESTAMP          NOT NULL DEFAULT now(),
    status     VARCHAR(10)        NOT NULL DEFAULT 'OK' -- status: OK DELETED
);

INSERT INTO CUSTOM_CONFIG (name, value)
VALUES ('cookieMaxAge', '1800'); -- 单位：s
