CREATE TABLE SESSION
(
    id      SERIAL PRIMARY KEY,
    cookie  VARCHAR(100) UNIQUE NOT NULL, -- 不包含 name，值为 UUID
    user_id INTEGER             NOT NULL
)
