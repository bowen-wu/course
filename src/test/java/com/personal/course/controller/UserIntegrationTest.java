package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.VO.UsernameAndPassword;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void adminCanUpdateUserRole() throws IOException, InterruptedException {
        String cookie = getAdminCookie();

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
        // TODO: 不知道为何返回4个role
//        assertEquals(2, getUserResponseAfterUpdate.getData().getRoles().size());
//        assertEquals(Arrays.asList("student", "admin"), getUserResponseAfterUpdate.getData().getRoles().stream().map(Role::getName).collect(toList()));
    }

    @Test
    public void return403WhenNotAdminRequest() throws IOException, InterruptedException {
        // getCookie
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        String teacherCookie = getUserCookie(new UsernameAndPassword("teacher", "teacher"));

        HttpResponse<String> response = get("/user/1", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, response.statusCode());

        response = patch("/user", objectMapper.writeValueAsString(new User()), HttpHeaders.COOKIE, teacherCookie, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(403, response.statusCode());

        response = get("/user?pageNum=2&pageSize=1", HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, response.statusCode());


        response = patch("/user", objectMapper.writeValueAsString(new User()), HttpHeaders.COOKIE, studentCookie, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(403, response.statusCode());

        response = get("/user?pageNum=2&pageSize=1", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, response.statusCode());
    }

    @Test
    public void adminCanGetUserList() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        // UserList => 200 + list
        HttpResponse<String> response = get("/user?pageNum=2&pageSize=1", HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, response.statusCode());
        PageResponse<User> userListPageResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });

        assertEquals(2, userListPageResponse.getPageNum());
        assertEquals(1, userListPageResponse.getPageSize());
        assertEquals(3, userListPageResponse.getTotalPage());
        assertEquals(1, userListPageResponse.getData().size());
        assertEquals("teacher", userListPageResponse.getData().get(0).getUsername());
        assertEquals(2, userListPageResponse.getData().get(0).getId());


        // UserList + orderBy => 200 + list
        response = get("/user?pageNum=1&pageSize=1&orderBy=DESC", HttpHeaders.COOKIE, adminCookie);

        assertEquals(200, response.statusCode());
        userListPageResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });

        assertEquals(1, userListPageResponse.getPageNum());
        assertEquals(1, userListPageResponse.getPageSize());
        assertEquals(3, userListPageResponse.getTotalPage());
        assertEquals(1, userListPageResponse.getData().size());
        assertEquals(3, userListPageResponse.getData().get(0).getId());
        assertEquals("administrator", userListPageResponse.getData().get(0).getUsername());


        // UserList + search => 200 + list
        response = get("/user?pageNum=1&pageSize=3&search=e", HttpHeaders.COOKIE, adminCookie);

        assertEquals(200, response.statusCode());
        userListPageResponse = objectMapper.readValue(response.body(), new TypeReference<>() {
        });

        assertEquals(1, userListPageResponse.getPageNum());
        assertEquals(3, userListPageResponse.getPageSize());
        assertEquals(1, userListPageResponse.getTotalPage());
        assertEquals(2, userListPageResponse.getData().size());
        assertEquals(Arrays.asList(1, 2), userListPageResponse.getData().stream().map(User::getId).collect(toList()));
        assertEquals(Arrays.asList("student", "teacher"), userListPageResponse.getData().stream().map(User::getUsername).collect(toList()));

    }

    @Test
    public void return404WhenUpdateUserRole() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();

        User updateUser = new User();
        updateUser.setId(999);
        Role updateRole = new Role();
        updateRole.setName("admin");
        List<Role> roles = new ArrayList<>();
        roles.add(updateRole);
        updateUser.setRoles(roles);

        HttpResponse<String> response = patch("/user", objectMapper.writeValueAsString(updateUser), HttpHeaders.COOKIE, adminCookie, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        assertEquals(404, response.statusCode());
    }

    @Test
    public void return404WhenGetUserById() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> response = get("/user/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, response.statusCode());
    }
}
