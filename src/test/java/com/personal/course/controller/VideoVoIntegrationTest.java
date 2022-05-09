package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.VideoVo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VideoVoIntegrationTest extends AbstractIntegrationTest {
    @Test
    public void testVideoVoProcess() throws IOException, InterruptedException {
        // TODO: 上传一张图片，获取 url
        // TODO: mock OSClientService
        // 增删改查 => 增删改查 课程管理权限(teacher & administrator)
        // 增加
        String adminCookie = getAdminCookie();
        VideoVo pendingCreateVideoVo = createVideoVo();

        Response<VideoVo> createdVideoVoResponse = createVideoVo(adminCookie, pendingCreateVideoVo);
        Integer createdVideoVoId = createdVideoVoResponse.getData().getId();

        // 获取视频 => 200 + VideoVo
        HttpResponse<String> res = get("/video/" + createdVideoVoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<VideoVo> getVideoVoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoVoPropertyEquals(createdVideoVoResponse.getData(), getVideoVoResponse.getData());
        assertEquals(Status.OK, getVideoVoResponse.getData().getStatus());

        // 修改视频信息 => 200 + 修改后的 VideoVo
        VideoVo pendModifyVideoVo = getVideoVoResponse.getData();
        pendModifyVideoVo.setUrl("https://course-video-1308805652.cos.ap-shanghai.myqcloud.com/sd1620039785_2.MP4/?q-sign-algorithm=sha1&q-ak=AKIDlVPJBKA7l9FMcMwkFlwCt_XHCYsKYZjn092JMTps3QYVcXpQcD-0wdNspjBSUlhi&q-sign-time=1652090052%3B1652091852&q-key-time=1652090052%3B1652091852&q-header-list=host&q-url-param-list=&q-signature=f81b9ac7d8d300b92b322309cd02bebea4abc282&x-cos-security-token=JkPBext6u4uGeR40NagS1CbCiGiKR06ae11878bca9813b2a7076ca1a1d40f138w7NZMkD_1ZPGvpgV2AcAjo4WWqqcSaleY5GJMKsrUZL6oGGbxBWdPtWKWkthhNw3n8ovCHuEA-UY_ZalMKVxr_KKcPmizyf2AWo-AFNrwWrJmr4HKC2pY1CMNY7-mzL1IqtL1FC9PaIiCA5jT5AH9p6pkOqUXiNkmocuAlV39VkwfZeqcNwF1Ud6zwfrUJKNZDdMGmcn-bqob2xmMJgqSG2RYF4ldmD0iThA1P4Mct4Z6Fv4RTHflmArqbALUT_79LfxV24Vd7b_dWNk5UWIW5PsjgAHQi4elCvFP8boBcJnjZEkdi7hjp7hRPqBXvRxDCJOnbYFLWVGkRIcn3ANjcUzLqZ1GG3KITs2dvZ2LK4");
        pendModifyVideoVo.setDescription("新的视频简介");
        res = patch("/video/" + createdVideoVoId, objectMapper.writeValueAsString(pendModifyVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<VideoVo> modifiedVideoVoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoVoPropertyEquals(pendModifyVideoVo, modifiedVideoVoResponse.getData());
        assertEquals(Status.OK, modifiedVideoVoResponse.getData().getStatus());
        assertEquals(getVideoVoResponse.getData().getCreatedOn(), modifiedVideoVoResponse.getData().getCreatedOn());
        assertNotEquals(getVideoVoResponse.getData().getUpdatedOn(), modifiedVideoVoResponse.getData().getUpdatedOn());

        // 获取视频 => 200 + 修改后的 VideoVo
        res = get("/video/" + modifiedVideoVoResponse.getData().getId(), HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<VideoVo> getModifiedVideoVoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoVoPropertyEquals(modifiedVideoVoResponse.getData(), getModifiedVideoVoResponse.getData());
        assertEquals(Status.OK, getModifiedVideoVoResponse.getData().getStatus());

        // 删除课程 => 204
        res = delete("/video/" + createdVideoVoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(204, res.statusCode());

        // 获取课程 => 404
        res = get("/video/" + createdVideoVoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());

    }

    private Response<VideoVo> createVideoVo(String adminCookie, VideoVo pendingCreateVideoVo) throws IOException, InterruptedException {
        HttpResponse<String> res = post("/video", objectMapper.writeValueAsString(pendingCreateVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<VideoVo> createdVideoVoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertVideoVoPropertyEquals(pendingCreateVideoVo, createdVideoVoResponse.getData());
        assertEquals(Status.OK, createdVideoVoResponse.getData().getStatus());
        assertNotNull(createdVideoVoResponse.getData().getId());
        return createdVideoVoResponse;
    }

    @Test
    public void canGetUploadVideoVoToken() {

    }

    @Test
    public void canGetVideoVoList() {

    }

    @Test
    public void return403WhenForbidden() throws IOException, InterruptedException {
        // student
        String studentCookie = getUserCookie("username=student&password=student");
        VideoVo pendingCreateVideoVo = createVideoVo();

        // create
        HttpResponse<String> res = post("/video", objectMapper.writeValueAsString(pendingCreateVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());

        // patch
        res = patch("/video/1", objectMapper.writeValueAsString(pendingCreateVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, studentCookie);
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
    public void return400WhenCreateVideoVo() throws IOException, InterruptedException {
        VideoVo pendingCreateVideoVo = createVideoVo();

        pendingCreateVideoVo.setName(null);
        exception400("/video", pendingCreateVideoVo, "视频名称不能为空");

        pendingCreateVideoVo.setName("视频名称");
        pendingCreateVideoVo.setUrl(null);
        exception400("/video", pendingCreateVideoVo, "视频地址不能为空");
    }

    @Test
    public void return404WhenDeleteVideoVo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = delete("/video/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return400WhenUpdateVideoVo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        VideoVo pendingCreateVideoVo = createVideoVo();

        Response<VideoVo> createdVideoVoResponse = createVideoVo(adminCookie, pendingCreateVideoVo);
        VideoVo createdVideoVo = createdVideoVoResponse.getData();
        Integer createdVideoVoId = createdVideoVoResponse.getData().getId();

        createdVideoVo.setName(null);
        HttpResponse<String> res = patch("/video/" + createdVideoVoId, objectMapper.writeValueAsString(createdVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        Response response = objectMapper.readValue(res.body(), Response.class);
        assertEquals("视频名称不能为空", response.getMessage());

        createdVideoVo.setName("视频名称");
        createdVideoVo.setUrl(null);
        res = patch("/video/" + createdVideoVoId, objectMapper.writeValueAsString(createdVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        response = objectMapper.readValue(res.body(), Response.class);
        assertEquals("视频地址不能为空", response.getMessage());
    }

    @Test
    public void return404WhenUpdateVideoVo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        VideoVo pendingCreateVideoVo = createVideoVo();
        HttpResponse<String> res = patch("/video/999", objectMapper.writeValueAsString(pendingCreateVideoVo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void return404WhenGetVideoVoById() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = get("/video/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    private VideoVo createVideoVo() {
        VideoVo pendingCreateVideoVo = new VideoVo();
        pendingCreateVideoVo.setName("新增测试视频");
        pendingCreateVideoVo.setDescription("测试视频简介");
        pendingCreateVideoVo.setUrl("https://oss.aliyun.com/xxx");
        return pendingCreateVideoVo;
    }

    private void assertVideoVoPropertyEquals(VideoVo pendingCreateVideoVo, VideoVo createdVideoVo) {
        assertEquals(pendingCreateVideoVo.getName(), createdVideoVo.getName());
        assertEquals(pendingCreateVideoVo.getDescription(), createdVideoVo.getDescription());
        assertEquals(pendingCreateVideoVo.getUrl(), createdVideoVo.getUrl());
    }
}