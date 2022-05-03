CREATE TABLE COURSE
(
    id                  SERIAL PRIMARY KEY,
    name                varchar(50) UNIQUE NOT NULL,
    teacher_name        varchar(50)        NOT NULL,
    teacher_description varchar(100),
    description         varchar(100)       NOT NULL,
    price               INTEGER            NOT NULL, -- 分
    created_on          TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on          TIMESTAMP          NOT NULL DEFAULT now(),
    status              VARCHAR(10)        NOT NULL DEFAULT 'OK'
);
