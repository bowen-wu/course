package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Course;
import com.personal.course.entity.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void testOrderProcess() throws IOException, InterruptedException {
        // 创建一个课程用于下订单
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();

        HttpResponse<String> res = post("/course", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<Course> createdCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        Integer createdCourseId = createdCourseResponse.getData().getId();

        String studentCookie = getUserCookie("username=student&password=student");

        // 下订单 => 201 + orderInfo
        HttpResponse<String> post = post("/order/" + createdCourseId, "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);

        // 获取订单信息 => 200 + orderInfo

        // 删除订单 => 204

        // 获取订单信息 => 404
    }

    @Test
    public void testGetOrderList() {

    }

    @Test
    public void return404WhenPlaceOrder() {

    }

    @Test
    public void return404WhenDeleteOrder() {

    }

    @Test
    public void return404WhenGetOrder() {

    }
}
