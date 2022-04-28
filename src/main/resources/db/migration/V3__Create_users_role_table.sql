CREATE TABLE USERS_ROLE
(
    id         SERIAL PRIMARY KEY,
    user_id    INTEGER     NOT NULL,
    role_id    INTEGER     NOT NULL,
    created_on TIMESTAMP   NOT NULL DEFAULT now(),
    updated_on TIMESTAMP   NOT NULL DEFAULT now(),
    status     VARCHAR(10) NOT NULL DEFAULT 'OK'
);
