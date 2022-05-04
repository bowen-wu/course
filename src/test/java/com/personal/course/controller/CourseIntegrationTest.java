package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Course;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CourseIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void testCourseProcess() throws IOException, InterruptedException {
        // 增删改查 => 增删改 课程管理权限(teacher & administrator)
        // 新建课程 => 201 + get courseId
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();

        HttpResponse<String> res = post("/course", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<Course> createdCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        Integer createdCourseId = createdCourseResponse.getData().getId();
        assertNotNull(createdCourseId);
        assertCoursePropertyEquals(pendingCreateCourse, createdCourseResponse.getData());
        assertEquals(Status.OK, createdCourseResponse.getData().getStatus());

        // 获取课程 => 200 + Course
        res = get("/course/" + createdCourseId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<Course> getCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertCoursePropertyEquals(createdCourseResponse.getData(), getCourseResponse.getData());
        assertEquals(Status.OK, getCourseResponse.getData().getStatus());

        // 修改课程 => 200 + 修改后的 Course
        Course pendModifyCourse = getCourseResponse.getData();
        pendModifyCourse.setPrice(39900);
        pendModifyCourse.setDescription("新的课程简介");
        res = patch("/course/" + createdCourseId, objectMapper.writeValueAsString(pendModifyCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<Course> modifiedCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertCoursePropertyEquals(pendModifyCourse, modifiedCourseResponse.getData());
        assertEquals(Status.OK, modifiedCourseResponse.getData().getStatus());
        assertEquals(getCourseResponse.getData().getCreatedOn(), modifiedCourseResponse.getData().getCreatedOn());
        assertNotEquals(getCourseResponse.getData().getUpdatedOn(), modifiedCourseResponse.getData().getUpdatedOn());

        // 获取课程 => 200 + 修改后的 Course
        res = get("/course/" + modifiedCourseResponse.getData().getId(), HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<Course> getModifiedCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
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

    private Course createCourse() {
        Course pendingCreateCourse = new Course();
        pendingCreateCourse.setName("新增测试课程");
        pendingCreateCourse.setDescription("测试课程简介");
        pendingCreateCourse.setTeacherName("Jack");
        pendingCreateCourse.setTeacherDescription("Jack is a good teacher!");
        pendingCreateCourse.setPrice(29900);
        return pendingCreateCourse;
    }

    private void assertCoursePropertyEquals(Course pendingCreateCourse, Course createdCourse) {
        assertEquals(pendingCreateCourse.getName(), createdCourse.getName());
        assertEquals(pendingCreateCourse.getDescription(), createdCourse.getDescription());
        assertEquals(pendingCreateCourse.getTeacherName(), createdCourse.getTeacherName());
        assertEquals(pendingCreateCourse.getTeacherDescription(), createdCourse.getTeacherDescription());
        assertEquals(pendingCreateCourse.getPrice(), createdCourse.getPrice());
    }

    @Test
    public void canGetCourseList() {

    }

    @Test
    public void return403WhenAddModifyDeleteAndNotAuthorized() throws IOException, InterruptedException {
        // student
        String studentCookie = getUserCookie("username=student&password=student");
        Course pendingCreateCourse = createCourse();

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
        String adminCookie = getAdminCookie();
        Course pendingCreateCourse = createCourse();

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
    public void return400WhenNoModify() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = patch("/course/1", objectMapper.writeValueAsString(new Course()), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
    }

    @Test
    public void return400WhenCreateCourse() throws IOException, InterruptedException {
        Course pendingCreateCourse = createCourse();

        pendingCreateCourse.setName(null);
        exception400(pendingCreateCourse, "课程名称不能为空");

        pendingCreateCourse.setName("课程名称");
        pendingCreateCourse.setDescription(null);
        exception400(pendingCreateCourse, "课程简介不能为空");

        pendingCreateCourse.setDescription("课程简介");
        pendingCreateCourse.setTeacherName(null);
        exception400(pendingCreateCourse, "老师姓名不能为空");

        pendingCreateCourse.setTeacherName("Jack");
        pendingCreateCourse.setPrice(null);
        exception400(pendingCreateCourse, "课程价格不能为空");
    }

    private void exception400(Course pendingCreateCourse, String errorMessage) throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = post("/course", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        Response response = objectMapper.readValue(res.body(), Response.class);
        assertEquals(errorMessage, response.getMessage());
    }
}
