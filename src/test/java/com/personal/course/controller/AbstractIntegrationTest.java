package com.personal.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.course.CourseApplication;
import com.personal.course.entity.Course;
import com.personal.course.entity.Response;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CourseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public abstract class AbstractIntegrationTest {
    @LocalServerPort
    private int port;

    @Value("${spring.datasource.url}")
    private String flywayUrl;
    @Value("${spring.datasource.username}")
    private String flywayUser;
    @Value("${spring.datasource.password}")
    private String flywayPassword;

    public final ObjectMapper objectMapper = new ObjectMapper();
    public final HttpClient client = HttpClient.newBuilder().build();

    @BeforeEach
    public void setUp() {
        objectMapper.findAndRegisterModules();
        Flyway flyway = Flyway.configure().dataSource(flywayUrl, flywayUser, flywayPassword).load();
        flyway.clean();
        flyway.migrate();
    }

    public <T> void exception400(String uri, T pendingCreateCourse, String errorMessage) throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = post(uri, objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        Response response = objectMapper.readValue(res.body(), Response.class);
        assertEquals(errorMessage, response.getMessage());
    }

    public String getUserCookie(String usernameAndPassword) throws IOException, InterruptedException {
        return getCookieFromResponse(login(usernameAndPassword));
    }

    public String getAdminCookie() throws IOException, InterruptedException {
        return getUserCookie("username=administrator&password=administrator");
    }

    public String getCookieFromResponse(HttpResponse<String> response) {
        return response.headers().allValues(HttpHeaders.SET_COOKIE).get(0);
    }

    public HttpResponse<String> login(String usernameAndPassword) throws IOException, InterruptedException {
        return post("/session", usernameAndPassword, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    public HttpResponse<String> patch(String uri, String body, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "PATCH", BodyPublishers.ofString(body), headers);
    }

    public HttpResponse<String> post(String uri, String body, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "POST", BodyPublishers.ofString(body), headers);
    }

    public HttpResponse<String> delete(String uri, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "DELETE", BodyPublishers.noBody(), headers);
    }

    public HttpResponse<String> get(String uri) throws IOException, InterruptedException {
        return getHttpRequest(uri, "GET", BodyPublishers.noBody());
    }

    public HttpResponse<String> get(String uri, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "GET", BodyPublishers.noBody(), headers);
    }

    public HttpResponse<String> getHttpRequest(String uri, String method, BodyPublisher bodyPublisher, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, method, bodyPublisher, BodyHandlers.ofString(), headers);
    }

    public <T> HttpResponse<T> getHttpRequest(String uri, String method, BodyPublisher bodyPublisher, HttpResponse.BodyHandler<T> responseBodyHandler, String... headers) throws IOException, InterruptedException {
        HttpRequest request;
        Builder builder = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/api/v1" + uri)).method(method, bodyPublisher);
        if (headers.length > 0) {
            request = builder.headers(headers).build();
        }
        request = builder.build();

        return client.send(request, responseBodyHandler);
    }
}
