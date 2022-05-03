CREATE TABLE COURSE_VIDEO
(
    id         SERIAL PRIMARY KEY,
    course_id  INTEGER     NOT NULL,
    video_id   INTEGER     NOT NULL,
    created_on TIMESTAMP   NOT NULL DEFAULT now(),
    updated_on TIMESTAMP   NOT NULL DEFAULT now(),
    status     VARCHAR(10) NOT NULL DEFAULT 'OK'
);
