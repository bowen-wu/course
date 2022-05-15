# 基础镜像
FROM openjdk:11.0.12-jdk

RUN mkdir /app
WORKDIR /app

COPY target/course-0.0.1-SNAPSHOT.jar  /app

# 暴露端口
EXPOSE 8080

CMD ["java", "-jar", "course-0.0.1-SNAPSHOT.jar"]
