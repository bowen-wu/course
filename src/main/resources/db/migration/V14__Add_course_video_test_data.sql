INSERT INTO COURSE (name, teacher_name, teacher_description, description, price)
VALUES ('前端', '老师1', '老师1简介', 'HTML CSS JS', '19900');

INSERT INTO COURSE (name, teacher_name, teacher_description, description, price)
VALUES ('Java 课程', '老师1', '老师1简介', 'Java 编程语言是一种简单、面向对象、分布式、解释型、健壮安全、与系统无关、可移植、高性能、多线程和动态的语言。', '49900');

INSERT INTO COURSE (name, teacher_name, teacher_description, description, price)
VALUES ('Docker 课程', '老师2', '老师2简介', 'Docker 改变了软件世界', '129900');

INSERT INTO VIDEO (name, description, key)
VALUES ('JavaScript 入门', 'JavaScript 入门视频', 'javascript.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('CSS 入门', 'CSS 入门视频', 'css.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('HTML 入门', 'HTML 入门视频', 'html.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('Java 入门', 'Java 入门视频', 'java_init.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('Maven 入门', 'Maven 入门视频', 'maven_init.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('Java Collection 入门', 'Java Collection 入门视频', 'java_collection.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('Docker 入门', 'Docker 入门视频', 'docker_init.mp4');

INSERT INTO VIDEO (name, description, key)
VALUES ('位运算入门', '位运算入门视频', 'bit_operation.mp4');

INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (1, 1);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (1, 2);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (1, 3);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (1, 8);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (2, 4);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (2, 5);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (2, 6);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (2, 8);
INSERT INTO COURSE_VIDEO (course_id, video_id)
VALUES (3, 7);
