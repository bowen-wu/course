package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.DO.CustomConfig;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.DTO.PaymentTradeQueryResponse;
import com.personal.course.entity.OrderWithComponentHtml;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;
import com.personal.course.entity.VO.CourseVO;
import com.personal.course.entity.VO.UsernameAndPassword;
import com.personal.course.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderIntegrationTest extends AbstractIntegrationTest {
    @MockBean
    PaymentService paymentService;

    private int coursePrice;
    private Integer createdCourseId;

    @BeforeEach
    public void setUp() throws IOException, InterruptedException, URISyntaxException {
        super.setUp();
        Response<CourseVO> createdCourseResponse = createCourseInDb();
        this.createdCourseId = createdCourseResponse.getData().getId();
        this.coursePrice = createdCourseResponse.getData().getPrice();
    }

    @Test
    public void testOrderProcess() throws IOException, InterruptedException {
        // 创建一个课程用于下订单
        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.PAID, null));

        // student userId = 1
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));

        // 下订单 => 201 + orderInfo
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);

        String createdTradeNo = orderWithComponentHtmlResponse.getData().getTradeNo();
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        // 获取订单信息 orderId => 200 + orderInfo
        HttpResponse<String> res = get("/order/" + createdOrderId, HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        Response<Order> orderResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(coursePrice, orderResponse.getData().getPrice());
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
        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.UNPAID, null), PaymentTradeQueryResponse.of(Status.CLOSED, null));

        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        // 取消订单(只有 UNPAID 的可以) => 200 + orderInfo status cancel
        HttpResponse<String> res = patch("/order/" + createdOrderId, "", HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        Response<Order> closedOrderResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });

        assertEquals(createdOrderId, closedOrderResponse.getData().getId());
        assertEquals(coursePrice, closedOrderResponse.getData().getPrice());
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
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        HttpResponse<String> res = post("/order/99", "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());

        res = delete("/order/99", HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());

        res = get("/order/99", HttpHeaders.COOKIE, studentCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return403WhenGetOrDeleteOrCancelOrder() throws IOException, InterruptedException {
        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.PAID, null));

        // student userId = 1
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));

        // 下订单 => 201 + orderInfo
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);

        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        String teacherCookie = getUserCookie(new UsernameAndPassword("teacher", "teacher"));

        HttpResponse<String> res = get("/order/" + createdOrderId, HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, res.statusCode());

        res = delete("/order/" + createdOrderId, HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, res.statusCode());

        res = patch("/order/" + createdOrderId, "", HttpHeaders.COOKIE, teacherCookie);
        assertEquals(403, res.statusCode());
    }

    @Test
    public void cannotCancelOrderWhenStatusNoUnpaid() throws IOException, InterruptedException {
        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.CLOSED, null));

        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        HttpResponse<String> res = patch("/order/" + createdOrderId, "", HttpHeaders.COOKIE, studentCookie);
        assertEquals(410, res.statusCode());
    }

    @Test
    public void testOrderNotCreatedWhenOrderAlreadyExist() throws IOException, InterruptedException {
        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.PAID, null));

        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));

        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);
        Response<OrderWithComponentHtml> twiceOrderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);
        assertEquals(orderWithComponentHtmlResponse.getData().getId(), twiceOrderWithComponentHtmlResponse.getData().getId());
        assertEquals(orderWithComponentHtmlResponse.getData().getTradeNo(), twiceOrderWithComponentHtmlResponse.getData().getTradeNo());

        get("/order/status?out_trade_no=" + orderWithComponentHtmlResponse.getData().getTradeNo());
        HttpResponse<String> res = post("/order/" + createdCourseId, "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(409, res.statusCode());
    }

    @Test
    public void testGetOrderList() {

    }

    private Response<OrderWithComponentHtml> placeOrder(Integer createdCourseId, String testFormComponentHtml, String studentCookie, int coursePrice) throws IOException, InterruptedException {
        HttpResponse<String> res = post("/order/" + createdCourseId, "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(201, res.statusCode());
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(testFormComponentHtml, orderWithComponentHtmlResponse.getData().getFormComponentHtml());
        assertEquals(Status.UNPAID, orderWithComponentHtmlResponse.getData().getStatus());
        assertEquals(coursePrice, orderWithComponentHtmlResponse.getData().getPrice());
        assertEquals(createdCourseId, orderWithComponentHtmlResponse.getData().getCourse().getId());
        assertEquals(1, orderWithComponentHtmlResponse.getData().getUserId());
        return orderWithComponentHtmlResponse;
    }

    private String mockData(PaymentTradeQueryResponse firstPaymentTradeQueryResponse, PaymentTradeQueryResponse... restPaymentTradeQueryResponse) {
        String testFormComponentHtml = "<form></form>";
        when(paymentService.tradePayInWebPage(anyString(), any(), anyInt(), anyString(), anyString(), anyString(), anyString())).thenReturn(TradePayResponse.of(testFormComponentHtml, null));
        when(paymentService.getTradeStatusFromPayTradeNo(any(), any(), any())).thenReturn(firstPaymentTradeQueryResponse, restPaymentTradeQueryResponse);
        return testFormComponentHtml;
    }

    @Test
    public void testTimingGetOrderStatus() throws IOException, InterruptedException {
        // 下单之后 预期在 delay 时间后 paymentService.getTradeStatusFromPayTradeNo 方法被调用
        String delay = "2";
        String adminCookie = getAdminCookie();
        patch("/customConfig", objectMapper.writeValueAsString(new CustomConfig("paymentTimeExpire", delay)), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);

        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.PAID, null));

        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);

        Thread.sleep((Long.parseLong(delay) * 60 + 15) * 1000);
        verify(paymentService, times(1)).getTradeStatusFromPayTradeNo(orderWithComponentHtmlResponse.getData().getPayTradeNo(), orderWithComponentHtmlResponse.getData().getTradeNo(), orderWithComponentHtmlResponse.getData().getStatus());
    }

    @Test
    public void generatorNewTradeNoWhenOrderStatusIsDeleted() throws IOException, InterruptedException {
        String testFormComponentHtml = mockData(PaymentTradeQueryResponse.of(Status.PAID, null));

        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);

        String createdTradeNo = orderWithComponentHtmlResponse.getData().getTradeNo();
        Integer createdOrderId = orderWithComponentHtmlResponse.getData().getId();

        Response<OrderWithComponentHtml> secondOrderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);
        assertEquals(createdOrderId, secondOrderWithComponentHtmlResponse.getData().getId());
        assertEquals(createdTradeNo, secondOrderWithComponentHtmlResponse.getData().getTradeNo());

        HttpResponse<String> res = delete("/order/" + createdOrderId, HttpHeaders.COOKIE, studentCookie);
        assertEquals(204, res.statusCode());

        Thread.sleep(1000);

        Response<OrderWithComponentHtml> thirdOrderWithComponentHtmlResponse = placeOrder(createdCourseId, testFormComponentHtml, studentCookie, coursePrice);
        assertNotEquals(createdOrderId, thirdOrderWithComponentHtmlResponse.getData().getId());
        assertNotEquals(createdTradeNo, thirdOrderWithComponentHtmlResponse.getData().getTradeNo());
    }
}
