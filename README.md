## 技术栈

- Java11
- Spring Boot
- Postgresql
- JPA
- 腾讯云 COS
- 支付宝支付

## Deploy

### jar包

1. ` mvn clean verify `
2. ` scp target/course-0.0.1-SNAPSHOT.jar <name>@<host>:/you/path `
3. 服务器启动数据库
   => ` docker run -p 5432:5432 -e POSTGRES_PASSWORD=root -e POSTGRES_USER=root -e POSTGRES_DB=course -v /custom/mount:/var/lib/postgresql/data -d postgres `
4. 初始化数据库
    1. download code
    2. Flyway => ` mvn flyway:migrate `
5. 配置 application.yml，将 application.yml 中的秘钥配置好，application.yml 位置放在和 jar 包同一目录下
6. 启动服务 => ` nohup java -jar course-0.0.1-SNAPSHOT.jar & `

### [Docker 部署](https://www.jianshu.com/p/ff6eac8ea250)

## 接口文档

clone 项目之后，使用浏览器打开 /apidoc/index.html

### 生成接口文档

1. install apidoc => ` npm install -g apidoc `
2. 生成 => ` apidoc -i src/main/java/com/personal/course/controller -o apidoc `

## Command

### Api Doc

```
apidoc -i src/main/java/com/personal/course/controller -o apidoc
```

### Flyway

```
mvn flyway:migrate
```

### Postgresql

```
docker run -p 5432:5432 -e POSTGRES_PASSWORD=root -e POSTGRES_USER=root -e POSTGRES_DB=course -v /custom/mount:/var/lib/postgresql/data -d postgres
```

### Dockerfile

```
docker build [--platform linux/amd64] [-f Dockerfile] . 
```
