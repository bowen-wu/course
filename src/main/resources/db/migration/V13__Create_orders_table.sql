CREATE TABLE ORDERS
(
    id           SERIAL PRIMARY KEY,
    user_id      INTEGER            NOT NULL,
    course_id    INTEGER            NOT NULL,
    price        INTEGER            NOT NULL,                 -- 分
    trade_no     varchar(64) UNIQUE NOT NULL,
    pay_trade_no varchar(64)        NOT NULL,
    created_on   TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on   TIMESTAMP          NOT NULL DEFAULT now(),
    status       VARCHAR(10)        NOT NULL DEFAULT 'UNPAID' -- status: UNPAID PAID DELETED CLOSED
);
