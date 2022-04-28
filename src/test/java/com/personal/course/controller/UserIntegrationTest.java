package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Response;
import com.personal.course.entity.Role;
import com.personal.course.entity.Status;
import com.personal.course.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void adminCanUpdateUserRole() throws IOException, InterruptedException {
        // Login
        String usernameAndPassword = "username=administrator&password=administrator";
        HttpResponse<String> loginResponse = login(usernameAndPassword);
        String cookie = getCookieFromResponse(loginResponse);

        // get user => 200
        HttpResponse<String> response = get("/user/1", HttpHeaders.COOKIE, cookie);

        assertEquals(200, response.statusCode());

        Response<User> getUserResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        assertEquals(1, getUserResponse.getData().getId());
        assertEquals("student", getUserResponse.getData().getUsername());
        assertEquals(Status.OK, getUserResponse.getData().getStatus());
        assertEquals(1, getUserResponse.getData().getRoles().size());

        // update role => 200 + User + correct role
        User updateUser = getUserResponse.getData();
        Role updateRole = new Role();
        updateRole.setName("admin");
        List<Role> roles = updateUser.getRoles();
        roles.add(updateRole);
        updateUser.setRoles(roles);

        response = patch("/user", objectMapper.writeValueAsString(updateUser), HttpHeaders.COOKIE, cookie, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Response<User> updatedUserResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });

        assertEquals(200, response.statusCode());
        assertEquals(1, updatedUserResponse.getData().getId());
        assertEquals(2, updatedUserResponse.getData().getRoles().size());
        assertEquals(Arrays.asList("student", "admin"), updatedUserResponse.getData().getRoles().stream().map(Role::getName).collect(toList()));

        // get user => 200 + correct role
        response = get("/user/1", HttpHeaders.COOKIE, cookie);
        Response<User> getUserResponseAfterUpdate = objectMapper.readValue(response.body(), new TypeReference<>() {
        });

        assertEquals(200, response.statusCode());
        assertEquals(1, getUserResponseAfterUpdate.getData().getId());
        assertEquals(2, getUserResponseAfterUpdate.getData().getRoles().size());
        assertEquals(Arrays.asList("student", "admin"), getUserResponseAfterUpdate.getData().getRoles().stream().map(Role::getName).collect(toList()));
    }

    @Test
    public void adminCanGetUserList() {

    }

    @Test
    public void return403WhenUpdateUserRole() {

    }

    @Test
    public void return404WhenUpdateUserRole() {

    }

    @Test
    public void return403WhenGetUserById() {

    }

    @Test
    public void return404WhenGetUserById() {

    }

    @Test
    public void return403WhenGetUserList() {

    }
}
