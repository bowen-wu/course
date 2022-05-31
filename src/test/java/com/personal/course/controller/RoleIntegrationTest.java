package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.Response;
import com.personal.course.entity.VO.UsernameAndPassword;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoleIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void testGetRoleEnum() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        String teacherCookie = getUserCookie(new UsernameAndPassword("teacher", "teacher"));

        HttpResponse<String> response = get("/roleEnum", HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, response.statusCode());
        Response<List<Role>> rolesResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        assertEquals(3, rolesResponse.getData().size());
        assertEquals(Arrays.asList("student", "teacher", "admin"), rolesResponse.getData().stream().map(Role::getName).collect(toList()));

        response = get("/roleEnum", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, response.statusCode());

        response = get("/roleEnum", HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, response.statusCode());
    }
}
