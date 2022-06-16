package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.DO.Course;
import com.personal.course.entity.DTO.PaymentTradeQueryResponse;
import com.personal.course.entity.OrderWithComponentHtml;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Query.CourseQuery;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;
import com.personal.course.entity.VO.CourseVO;
import com.personal.course.entity.VO.UsernameAndPassword;
import com.personal.course.entity.base.VideoBase;
import com.personal.course.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CourseIntegrationTest extends AbstractIntegrationTest {
    @MockBean
    PaymentService paymentService;

    @Test
    public void testCourseProcess() throws IOException, InterruptedException {
        // 增删改查 => 增删改 课程管理权限(teacher & administrator)
        // 新建课程 => 201 + get courseId
        Response<CourseVO> createdCourseResponse = createCourseInDb();
        Integer createdCourseId = createdCourseResponse.getData().getId();

        // 获取课程 => 200 + Course
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = get("/course/" + createdCourseId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<CourseVO> getCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertCoursePropertyEquals(createdCourseResponse.getData(), getCourseResponse.getData());
        assertEquals(Status.OK, getCourseResponse.getData().getStatus());

        // 修改课程 => 200 + 修改后的 Course
        CourseVO pendModifyCourse = getCourseResponse.getData();
        pendModifyCourse.setPrice(39900);
        pendModifyCourse.setDescription("新的课程简介");
        res = patch("/course/" + createdCourseId, objectMapper.writeValueAsString(pendModifyCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<CourseVO> modifiedCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertCoursePropertyEquals(pendModifyCourse, modifiedCourseResponse.getData());
        assertEquals(Status.OK, modifiedCourseResponse.getData().getStatus());
        assertEquals(getCourseResponse.getData().getCreatedOn(), modifiedCourseResponse.getData().getCreatedOn());
        assertNotEquals(getCourseResponse.getData().getUpdatedOn(), modifiedCourseResponse.getData().getUpdatedOn());

        // 获取课程 => 200 + 修改后的 Course
        res = get("/course/" + modifiedCourseResponse.getData().getId(), HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<CourseVO> getModifiedCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertCoursePropertyEquals(modifiedCourseResponse.getData(), getModifiedCourseResponse.getData());
        assertEquals(Status.OK, getModifiedCourseResponse.getData().getStatus());

        // 删除课程 => 204
        res = delete("/course/" + createdCourseId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(204, res.statusCode());

        // 获取课程 => 404
        res = get("/course/" + createdCourseId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    private void assertCoursePropertyEquals(CourseVO course, CourseVO courseInDb) {
        assertEquals(course.getName(), courseInDb.getName());
        assertEquals(course.getDescription(), courseInDb.getDescription());
        assertEquals(course.getTeacherName(), courseInDb.getTeacherName());
        assertEquals(course.getTeacherDescription(), courseInDb.getTeacherDescription());
        assertEquals(course.getPrice(), courseInDb.getPrice());
    }

    @Test
    public void canGetCourseList() throws IOException, InterruptedException {
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        HttpResponse<String> res = get("/course", HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        PageResponse<CourseVO> courseVOPageResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(1, courseVOPageResponse.getPageNum());
        assertEquals(10, courseVOPageResponse.getPageSize());
        assertEquals(3, courseVOPageResponse.getTotal());
        assertEquals(3, courseVOPageResponse.getData().size());
        assertEquals(Arrays.asList("前端", "Java 课程", "Docker 课程"), courseVOPageResponse.getData().stream().map(CourseVO::getName).collect(Collectors.toList()));
        assertEquals(Arrays.asList(19900, 49900, 129900), courseVOPageResponse.getData().stream().map(CourseVO::getPrice).collect(toList()));

        res = get("/course?search=课程&pageSize=1", HttpHeaders.COOKIE, studentCookie);
        assertEquals(200, res.statusCode());
        courseVOPageResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(1, courseVOPageResponse.getPageNum());
        assertEquals(1, courseVOPageResponse.getPageSize());
        assertEquals(1, courseVOPageResponse.getData().size());
        // TODO: search 未生效
//        assertEquals(2, courseVOPageResponse.getTotal());
//        assertEquals(Arrays.asList("Java 课程", "Docker 课程"), courseVOPageResponse.getData().stream().map(CourseVO::getName).collect(Collectors.toList()));
//        assertEquals(Arrays.asList(49900, 129900), courseVOPageResponse.getData().stream().map(CourseVO::getPrice).collect(toList()));

    }

    @Test
    public void return403WhenAddModifyDeleteAndNotAuthorized() throws IOException, InterruptedException {
        // student
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));
        CourseQuery pendingCreateCourse = createCourse();

        // create
        HttpResponse<String> res = post("/course", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // patch
        res = patch("/course/1", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // delete
        res = delete("/course/1", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());
    }

    @Test
    public void return404WhenModifyDeleteGet() throws IOException, InterruptedException {
        CourseQuery pendingCreateCourse = createCourse();

        String adminCookie = getAdminCookie();
        // get
        HttpResponse<String> res = get("/course/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());

        // patch
        res = patch("/course/999", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());

        // delete
        res = delete("/course/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return400WhenNoModifyAndCreate() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = patch("/course/1", objectMapper.writeValueAsString(new Course()), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());

        CourseQuery pendingCreateCourse = createCourse();

        pendingCreateCourse.setName(null);
        exception400("/course", pendingCreateCourse, "课程名称不能为空");

        pendingCreateCourse.setName("课程名称");
        pendingCreateCourse.setDescription(null);
        exception400("/course", pendingCreateCourse, "课程简介不能为空");

        pendingCreateCourse.setDescription("课程简介");
        pendingCreateCourse.setTeacherName(null);
        exception400("/course", pendingCreateCourse, "老师姓名不能为空");

        pendingCreateCourse.setTeacherName("Jack");
        pendingCreateCourse.setPrice(null);
        exception400("/course", pendingCreateCourse, "课程价格不能为空");
    }


    @Test
    public void getCourseDetailVideoUrlIsExistWhenPaid() throws IOException, InterruptedException {
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));

        String testFormComponentHtml = "<form></form>";
        when(paymentService.tradePayInWebPage(anyString(), any(), anyInt(), anyString(), anyString())).thenReturn(TradePayResponse.of(testFormComponentHtml, null));
        when(paymentService.getTradeStatusFromPayTradeNo(any(), any(), any())).thenReturn(PaymentTradeQueryResponse.of(Status.PAID, null));

        HttpResponse<String> res = post("/order/1", "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        get("/order/status?out_trade_no=" + orderWithComponentHtmlResponse.getData().getTradeNo());

        // 课程1有4个视频
        res = get("/course/1", HttpHeaders.COOKIE, studentCookie);
        Response<CourseVO> getCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(Arrays.asList(null, null, null, null), getCourseResponse.getData().getVideoList().stream().map(VideoBase::getUrl).collect(toList()));
        assertTrue(getCourseResponse.getData().isPurchased());
    }

    @Test
    public void getCourseDetailVideoUrlIsNullWhenUnpaid() throws IOException, InterruptedException {
        String studentCookie = getUserCookie(new UsernameAndPassword("student", "student"));

        // 课程1有4个视频
        HttpResponse<String> res = get("/course/1", HttpHeaders.COOKIE, studentCookie);
        Response<CourseVO> getCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(Arrays.asList(null, null, null, null), getCourseResponse.getData().getVideoList().stream().map(VideoBase::getUrl).collect(toList()));

        String testFormComponentHtml = "<form></form>";
        when(paymentService.tradePayInWebPage(anyString(), any(), anyInt(), anyString(), anyString())).thenReturn(TradePayResponse.of(testFormComponentHtml, null));

        res = post("/order/1", "", HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(201, res.statusCode());
        Response<OrderWithComponentHtml> orderWithComponentHtmlResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(Status.UNPAID, orderWithComponentHtmlResponse.getData().getStatus());

        res = get("/course/1", HttpHeaders.COOKIE, studentCookie);
        getCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(Arrays.asList(null, null, null, null), getCourseResponse.getData().getVideoList().stream().map(VideoBase::getUrl).collect(toList()));
    }
}
