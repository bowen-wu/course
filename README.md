## 技术栈

- Database => Postgres SQl
- Login Status => Cookie => 保存至DB => SESSION Table

## Table

### USER Table

```postgresql
CREATE TABLE uses
(
    id                 serial PRIMARY KEY,
    username           VARCHAR(50) UNIQUE NOT NULL,
    encrypted_password VARCHAR(50)        NOT NULL,
    created_on         TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on         TIMESTAMP          NOT NULL DEFAULT now(),
    status             VARCHAR(10)        NOT NULL DEFAULT 'OK'
);

-- status: OK DELETED
```

### ROLE Table

```postgresql
CREATE TABLE ROLE
(
    id         serial PRIMARY KEY,
    name       VARCHAR(50) UNIQUE NOT NULL,
    created_on TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on TIMESTAMP          NOT NULL DEFAULT now(),
    status     VARCHAR(10)        NOT NULL DEFAULT 'OK'
);
```

### USERS_ROLE Table

```postgresql
CREATE TABLE USERS_ROLE
(
    id         serial PRIMARY KEY,
    user_id    INTEGER     NOT NULL,
    role_id    INTEGER     NOT NULL,
    created_on TIMESTAMP   NOT NULL DEFAULT now(),
    updated_on TIMESTAMP   NOT NULL DEFAULT now(),
    status     VARCHAR(10) NOT NULL DEFAULT 'OK'
);
```

### PERMISSION Table

```postgresql
CREATE TABLE PERMISSION
(
    id         serial PRIMARY KEY,
    name       VARCHAR(50) UNIQUE NOT NULL,
    role_id    INTEGER            NOT NULL,
    created_on TIMESTAMP          NOT NULL DEFAULT now(),
    updated_on TIMESTAMP          NOT NULL DEFAULT now(),
    status     VARCHAR(10)        NOT NULL DEFAULT 'OK'
);
```

### SESSION Table

```postgresql
CREATE TABLE SESSION
(
    id      serial PRIMARY KEY,
    cookie  VARCHAR(50) UNIQUE NOT NULL,
    user_id INTEGER            NOT NULL
)
```

## Api Doc

```
apidoc -i src/main/java/com/personal/course/controller -o apidoc
```

## Flyway

```
mvn flyway:migrate
```
