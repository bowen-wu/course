package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Course;
import com.personal.course.entity.Order;
import com.personal.course.entity.OrderWithComponentHtml;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;
import com.personal.course.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class OrderIntegrationTest extends AbstractIntegrationTest {
    @MockBean
    PaymentService paymentService;

    @Test
    public void testOrderProcess() throws IOException, InterruptedException {
        // 创建一个课程用于下订单
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();
        Integer createdCourseId = createCourseInDb(adminCookie, pendingCreateCourse);
        String testFormComponentHtml = mockData(Status.PAID);

        // student userId = 1
        String studentCookie = getUserCookie("username=student&password=student");

        // 下订单 => 201 + orderInfo
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(pendingCreateCourse, createdCourseId, testFormComponentHtml, studentCookie);

        String createdTradeNo = orderWithComponentHtmlResponse.getData().getTradeNo();
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        // 获取订单信息 orderId => 200 + orderInfo
        HttpResponse<String> res = get("/order/" + createdOrderId, HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        Response<Order> orderResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(pendingCreateCourse.getPrice(), orderResponse.getData().getPrice());
        assertEquals(createdCourseId, orderResponse.getData().getCourse().getId());
        assertEquals(1, orderResponse.getData().getUserId());
        assertEquals(orderWithComponentHtmlResponse.getData().getCreatedOn(), orderResponse.getData().getCreatedOn());
        assertEquals(createdTradeNo, orderResponse.getData().getTradeNo());
        assertNotEquals(orderWithComponentHtmlResponse.getData().getUpdatedOn(), orderResponse.getData().getUpdatedOn());
        assertEquals(Status.PAID, orderResponse.getData().getStatus());

        // 删除订单 => 204
        res = delete("/order/" + createdOrderId, HttpHeaders.COOKIE, studentCookie);
        assertEquals(204, res.statusCode());

        // 获取订单信息 => 404
        res = get("/order/" + createdOrderId, HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());

    }

    @Test
    public void testCancelOrder() throws IOException, InterruptedException {
        // 创建一个课程用于下订单
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();
        Integer createdCourseId = createCourseInDb(adminCookie, pendingCreateCourse);
        String testFormComponentHtml = mockData(Status.UNPAID, Status.CLOSED);

        String studentCookie = getUserCookie("username=student&password=student");
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(pendingCreateCourse, createdCourseId, testFormComponentHtml, studentCookie);
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        // 取消订单(只有 UNPAID 的可以) => 200 + orderInfo status cancel
        HttpResponse<String> res = patch("/order/" + createdOrderId, "", HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        Response<Order> closedOrderResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });

        assertEquals(createdOrderId, closedOrderResponse.getData().getId());
        assertEquals(pendingCreateCourse.getPrice(), closedOrderResponse.getData().getPrice());
        assertEquals(createdCourseId, closedOrderResponse.getData().getCourse().getId());
        assertEquals(1, closedOrderResponse.getData().getUserId());
        assertEquals(orderWithComponentHtmlResponse.getData().getTradeNo(), closedOrderResponse.getData().getTradeNo());
        assertEquals(orderWithComponentHtmlResponse.getData().getCreatedOn(), closedOrderResponse.getData().getCreatedOn());
        assertNotEquals(orderWithComponentHtmlResponse.getData().getUpdatedOn(), closedOrderResponse.getData().getUpdatedOn());
        assertEquals(Status.CLOSED, closedOrderResponse.getData().getStatus());

        // 获取订单信息 orderId => 200 + orderInfo
        res = get("/order/" + createdOrderId, HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        Response<Order> orderResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(createdOrderId, orderResponse.getData().getId());
        assertEquals(Status.CLOSED, orderResponse.getData().getStatus());
    }

    @Test
    public void return404WhenPlaceOrGetOrDeleteOrder() throws IOException, InterruptedException {
        String studentCookie = getUserCookie("username=student&password=student");
        HttpResponse<String> res = post("/order/99", "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());

        res = delete("/order/99", HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());

        res = get("/order/99", HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return403WhenGetOrDeleteOrCancelOrder() throws IOException, InterruptedException {
        // 创建一个课程用于下订单
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();
        Integer createdCourseId = createCourseInDb(adminCookie, pendingCreateCourse);
        String testFormComponentHtml = mockData(Status.PAID);

        // student userId = 1
        String studentCookie = getUserCookie("username=student&password=student");

        // 下订单 => 201 + orderInfo
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(pendingCreateCourse, createdCourseId, testFormComponentHtml, studentCookie);

        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        String teacherCookie = getUserCookie("username=teacher&password=teacher");

        HttpResponse<String> res = get("/order/" + createdOrderId, HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, res.statusCode());

        res = delete("/order/" + createdOrderId, HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, res.statusCode());

        res = patch("/order/" + createdOrderId, "", HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, res.statusCode());
    }

    @Test
    public void cannotCancelOrderWhenStatusNoUnpaid() throws IOException, InterruptedException {
        // 创建一个课程用于下订单
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();
        Integer createdCourseId = createCourseInDb(adminCookie, pendingCreateCourse);
        String testFormComponentHtml = mockData(Status.CLOSED);

        String studentCookie = getUserCookie("username=student&password=student");
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(pendingCreateCourse, createdCourseId, testFormComponentHtml, studentCookie);
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        HttpResponse<String> res = patch("/order/" + createdOrderId, "", HttpHeaders.COOKIE, studentCookie);
        assertEquals(410, res.statusCode());
    }

    @Test
    public void testGetOrderList() {

    }

    private Response<OrderWithComponentHtml> placeOrder(Course pendingCreateCourse, Integer createdCourseId, String testFormComponentHtml, String studentCookie) throws IOException, InterruptedException {
        HttpResponse<String> res = post("/order/" + createdCourseId, "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(201, res.statusCode());
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(testFormComponentHtml, orderWithComponentHtmlResponse.getData().getFormComponentHtml());
        assertEquals(Status.UNPAID, orderWithComponentHtmlResponse.getData().getStatus());
        assertEquals(pendingCreateCourse.getPrice(), orderWithComponentHtmlResponse.getData().getPrice());
        assertEquals(createdCourseId, orderWithComponentHtmlResponse.getData().getCourse().getId());
        assertEquals(1, orderWithComponentHtmlResponse.getData().getUserId());
        return orderWithComponentHtmlResponse;
    }

    private String mockData(Status firstStatus, Status... nextStatus) {
        String testFormComponentHtml = "<form></form>";
        String testPayTradeNo = UUID.randomUUID().toString();
        TradePayResponse testTradePayResponse = TradePayResponse.of(testFormComponentHtml, testPayTradeNo);
        when(paymentService.tradePayInWebPage(anyString(), anyInt(), anyString(), anyString())).thenReturn(testTradePayResponse);
        when(paymentService.getTradeStatusFromPayTradeNo(testPayTradeNo)).thenReturn(firstStatus, nextStatus);
        return testFormComponentHtml;
    }

    private Integer createCourseInDb(String adminCookie, Course pendingCreateCourse) throws IOException, InterruptedException {
        HttpResponse<String> res = post("/course", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<Course> createdCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        return createdCourseResponse.getData().getId();
    }

}
