CREATE TABLE VIDEO
(
    id          SERIAL PRIMARY KEY,
    name        varchar(50) UNIQUE NOT NULL,
    description varchar(100)       NOT NULL,
    url         TEXT               NOT NULL,
    created_on  TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on  TIMESTAMP          NOT NULL DEFAULT now(),
    status      VARCHAR(10)        NOT NULL DEFAULT 'OK'
);
