package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.Video;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VideoIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void testVideoProcess() throws IOException, InterruptedException {
        // 增删改查 => 增删改查 课程管理权限(teacher & administrator)
        // 增加
        String adminCookie = getAdminCookie();
        Video pendingCreateVideo = createVideo();

        Response<Video> createdVideoResponse = createVideo(adminCookie, pendingCreateVideo);
        Integer createdVideoId = createdVideoResponse.getData().getId();

        // 获取视频 => 200 + Video
        HttpResponse<String> res = get("/video/" + createdVideoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<Video> getVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoPropertyEquals(createdVideoResponse.getData(), getVideoResponse.getData());
        assertEquals(Status.OK, getVideoResponse.getData().getStatus());

        // 修改视频信息 => 200 + 修改后的 Video
        Video pendModifyVideo = getVideoResponse.getData();
        pendModifyVideo.setUrl("http://oss/aliyun.com/yyy");
        pendModifyVideo.setDescription("新的视频简介");
        res = patch("/video/" + createdVideoId, objectMapper.writeValueAsString(pendModifyVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<Video> modifiedVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoPropertyEquals(pendModifyVideo, modifiedVideoResponse.getData());
        assertEquals(Status.OK, modifiedVideoResponse.getData().getStatus());
        assertEquals(getVideoResponse.getData().getCreatedOn(), modifiedVideoResponse.getData().getCreatedOn());
        assertNotEquals(getVideoResponse.getData().getUpdatedOn(), modifiedVideoResponse.getData().getUpdatedOn());

        // 获取视频 => 200 + 修改后的 Video
        res = get("/video/" + modifiedVideoResponse.getData().getId(), HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<Video> getModifiedVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoPropertyEquals(modifiedVideoResponse.getData(), getModifiedVideoResponse.getData());
        assertEquals(Status.OK, getModifiedVideoResponse.getData().getStatus());

        // 删除课程 => 204
        res = delete("/video/" + createdVideoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(204, res.statusCode());

        // 获取课程 => 404
        res = get("/video/" + createdVideoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());

    }

    private Response<Video> createVideo(String adminCookie, Video pendingCreateVideo) throws IOException, InterruptedException {
        HttpResponse<String> res = post("/video", objectMapper.writeValueAsString(pendingCreateVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<Video> createdVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoPropertyEquals(pendingCreateVideo, createdVideoResponse.getData());
        assertEquals(Status.OK, createdVideoResponse.getData().getStatus());
        assertNotNull(createdVideoResponse.getData().getId());
        return createdVideoResponse;
    }

    @Test
    public void canGetUploadVideoToken() {

    }

    @Test
    public void canGetVideoList() {

    }

    @Test
    public void return403WhenForbidden() throws IOException, InterruptedException {
        // student
        String studentCookie = getUserCookie("username=student&password=student");
        Video pendingCreateVideo = createVideo();

        // create
        HttpResponse<String> res = post("/video", objectMapper.writeValueAsString(pendingCreateVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // patch
        res = patch("/video/1", objectMapper.writeValueAsString(pendingCreateVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // delete
        res = delete("/video/1", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // get
        res = get("/video/1", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // get upload video token
        res = get("/video/token", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // get video list
        res = get("/video", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());
    }

    @Test
    public void return400WhenCreateVideo() throws IOException, InterruptedException {
        Video pendingCreateVideo = createVideo();

        pendingCreateVideo.setName(null);
        exception400("/video", pendingCreateVideo, "视频名称不能为空");

        pendingCreateVideo.setName("视频名称");
        pendingCreateVideo.setUrl(null);
        exception400("/video", pendingCreateVideo, "视频地址不能为空");
    }

    @Test
    public void return404WhenDeleteVideo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = delete("/video/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return400WhenUpdateVideo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        Video pendingCreateVideo = createVideo();

        Response<Video> createdVideoResponse = createVideo(adminCookie, pendingCreateVideo);
        Video createdVideo = createdVideoResponse.getData();
        Integer createdVideoId = createdVideoResponse.getData().getId();

        createdVideo.setName(null);
        HttpResponse<String> res = patch("/video/" + createdVideoId, objectMapper.writeValueAsString(createdVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        Response response = objectMapper.readValue(res.body(), Response.class);
        assertEquals("视频名称不能为空", response.getMessage());

        createdVideo.setName("视频名称");
        createdVideo.setUrl(null);
        res = patch("/video/" + createdVideoId, objectMapper.writeValueAsString(createdVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        response = objectMapper.readValue(res.body(), Response.class);
        assertEquals("视频地址不能为空", response.getMessage());
    }

    @Test
    public void return404WhenUpdateVideo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        Video pendingCreateVideo = createVideo();
        HttpResponse<String> res = patch("/video/999", objectMapper.writeValueAsString(pendingCreateVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return404WhenGetVideoById() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = get("/video/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    private Video createVideo() {
        Video pendingCreateVideo = new Video();
        pendingCreateVideo.setName("新增测试视频");
        pendingCreateVideo.setDescription("测试视频简介");
        pendingCreateVideo.setUrl("https://oss.aliyun.com/xxx");
        return pendingCreateVideo;
    }

    private void assertVideoPropertyEquals(Video pendingCreateVideo, Video createdVideo) {
        assertEquals(pendingCreateVideo.getName(), createdVideo.getName());
        assertEquals(pendingCreateVideo.getDescription(), createdVideo.getDescription());
        assertEquals(pendingCreateVideo.getUrl(), createdVideo.getUrl());
    }
}
