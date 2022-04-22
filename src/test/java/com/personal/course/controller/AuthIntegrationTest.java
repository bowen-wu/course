package com.personal.course.controller;

import com.personal.course.CourseApplication;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CourseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.properties"})
class AuthIntegrationTest {
    @LocalServerPort
    private int port;

    @Value("${spring.datasource.url}")
    private String flywayUrl;
    @Value("${spring.datasource.username}")
    private String flywayUser;
    @Value("${spring.datasource.password}")
    private String flywayPassword;

    @BeforeEach
    public void setUp() {
        Flyway flyway = Flyway.configure().dataSource(flywayUrl, flywayUser, flywayPassword).load();
        flyway.clean();
        flyway.migrate();
    }

    @Test
    public void testLoginProcess() throws IOException, InterruptedException {
        // 检查登录状态 GET /session => 401
        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/session"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        System.out.println(response);
        assertEquals(401, response.statusCode());


        // 注册 POST /user => 201

        // 登录 POST /session => 200 + User

        // 检查登录状态 GET /session => 200 + User

        // 登出 DELETE /session => 204

        // 检查登录状态 GET /session => 401
    }

    @Test
    public void return404WhenUnregisteredUserLogin() {

    }
}
