package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.VO.UsernameAndPassword;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void testLoginProcess() throws IOException, InterruptedException {
        // 检查登录状态 GET /session => 401
        HttpResponse<String> response = get("/session");
        assertEquals(401, response.statusCode());

        // 注册 POST /user => 201
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("jackson", "jackson");
        response = post("/user", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(201, response.statusCode());
        Response<User> registerResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        assertEquals("jackson", registerResponse.getData().getUsername());
        assertEquals(Status.OK, registerResponse.getData().getStatus());

        // 登录 POST /session => 200 + User
        response = post("/session", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        assertEquals(200, response.statusCode());
        Response<User> loginResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        assertEquals("jackson", loginResponse.getData().getUsername());
        assertEquals(Status.OK, loginResponse.getData().getStatus());
        assertEquals(1, response.headers().allValues(HttpHeaders.SET_COOKIE).size());

        String cookie = response.headers().allValues(HttpHeaders.SET_COOKIE).get(0);

        // 检查登录状态 GET /session => 200 + User
        response = get("/session", HttpHeaders.COOKIE, cookie);
        assertEquals(200, response.statusCode());
        Response<User> loginStatusResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        assertEquals("jackson", loginStatusResponse.getData().getUsername());
        assertEquals(loginResponse.getData().getId(), loginStatusResponse.getData().getId());
        assertEquals(loginResponse.getData().getStatus(), loginStatusResponse.getData().getStatus());

        // 登出 DELETE /session => 204
        response = delete("/session", HttpHeaders.COOKIE, cookie);
        assertEquals(204, response.statusCode());

        // 检查登录状态 GET /session => 401
        response = get("/session", HttpHeaders.COOKIE, cookie);
        assertEquals(401, response.statusCode());
    }

    @Test
    public void testDefaultRoleWhenRegisterUser() throws IOException, InterruptedException {
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("jackson", "jackson");
        HttpResponse<String> response = post("/user", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(201, response.statusCode());
        Response<User> registerResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        assertEquals(1, registerResponse.getData().getRoles().size());
        assertEquals("student", registerResponse.getData().getRoles().get(0).getName());
    }

    @Test
    public void testCookieSetMaxAge() throws IOException, InterruptedException {
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("student", "student");
        HttpResponse<String> response = post("/session", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpCookie httpCookie = HttpCookie.parse(response.headers().allValues(HttpHeaders.SET_COOKIE).get(0)).get(0);
        assertEquals(1800, httpCookie.getMaxAge());
    }

    @Test
    public void testCookieUpdateWhenRequest() throws IOException, InterruptedException, ParseException {
        // Tue, 24-May-2022 07:25:36 GMT
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss zzz");
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("student", "student");
        HttpResponse<String> response = post("/session", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String expiresValue = Arrays.stream(response.headers().allValues(HttpHeaders.SET_COOKIE).get(0).split("; ")).filter(attr -> attr.startsWith("Expires")).collect(Collectors.toList()).get(0).split("=")[1];
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(expiresValue, formatter);
        String cookie = response.headers().allValues(HttpHeaders.SET_COOKIE).get(0);

        sleep(5 * 1000);
        response = get("/session", HttpHeaders.COOKIE, cookie);
        String updateExpiresValue = Arrays.stream(response.headers().allValues(HttpHeaders.SET_COOKIE).get(0).split("; ")).filter(attr -> attr.startsWith("Expires")).collect(Collectors.toList()).get(0).split("=")[1];
        ZonedDateTime updateZonedDateTime = ZonedDateTime.parse(updateExpiresValue, formatter);

        assertTrue(Duration.between(zonedDateTime, updateZonedDateTime).getSeconds() >= 5);
    }

    @Test
    public void return404WhenUnregisteredUserLogin() throws IOException, InterruptedException {
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("jackson", "jackson");
        HttpResponse<String> response = post("/session", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(404, response.statusCode());
    }

    @Test
    public void return409WhenDuplicateUsername() throws IOException, InterruptedException {
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("jackson", "jackson");
        HttpResponse<String> response = post("/user", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(201, response.statusCode());

        response = post("/user", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(409, response.statusCode());
    }

    @Test
    public void return400WhenRegisterOrLoginUsernameOrPasswordInvalid() throws IOException, InterruptedException {
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("jack", "jackson");
        HttpResponse<String> response = post("/user", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(400, response.statusCode());

        usernameAndPassword.setUsername("jackson");
        usernameAndPassword.setUsername("jacks");
        response = post("/session", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(400, response.statusCode());
    }

    @Test
    public void loginWithCookie() throws IOException, InterruptedException {
        UsernameAndPassword usernameAndPassword = new UsernameAndPassword("student", "student");
        String studentCookie = getUserCookie(usernameAndPassword);
        HttpResponse<String> response = post("/session", objectMapper.writeValueAsString(usernameAndPassword), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, response.statusCode());
        String cookie = response.headers().allValues(HttpHeaders.SET_COOKIE).get(0);
        assertNotEquals(studentCookie.split(";")[0].split("=")[1], cookie.split(";")[0].split("=")[1]);
    }
}
