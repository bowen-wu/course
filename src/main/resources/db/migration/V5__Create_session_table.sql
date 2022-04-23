CREATE TABLE session
(
    id      SERIAL PRIMARY KEY,
    cookie  VARCHAR(50) UNIQUE NOT NULL, -- 不包含 name，值为 UUID
    user_id INTEGER            NOT NULL
)
