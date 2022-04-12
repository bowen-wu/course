CREATE TABLE session
(
    id      SERIAL PRIMARY KEY,
    cookie  VARCHAR(50) UNIQUE NOT NULL,
    user_id INTEGER            NOT NULL
)
