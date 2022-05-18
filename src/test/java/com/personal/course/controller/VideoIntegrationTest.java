package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Query.VideoQuery;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.VO.VideoVO;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class VideoIntegrationTest extends AbstractIntegrationTest {
    private final String testKey = "test.mp4";
    private final String testUrl = "http://course-api-doc.oss-cn-hangzhou.aliyuncs.com/" + testKey + "?Expires=1652001437&OSSAccessKeyId=TMP.3KhT7CnCWSDUdBwvfMkRs4KWoFEEskoAUdD3dn2iabHf4uDQNSnKz1bgJrdNeqZXb9xULBBkcqUkGkHYUVDSQhQ5ERexvt&Signature=WtAw6K4Ome2v0w3HIxdCP0QnE1o%3D";
    private final String updatedTestKey = "MTB.mp4";
    private final String updatedTestUrl = "http://course-api-doc.oss-cn-hangzhou.aliyuncs.com/" + updatedTestKey + "?Expires=1652001437&OSSAccessKeyId=TMP.3KhT7CnCWSDUdBwvfMkRs4KWoFEEskoAUdD3dn2iabHf4uDQNSnKz1bgJrdNeqZXb9xULBBkcqUkGkHYUVDSQhQ5ERexvt&Signature=WtAw6K4Ome2v0w3HIxdCP0QnE1o%3D";

    @Test
    public void testVideoProcess() throws IOException, InterruptedException, URISyntaxException {
        String adminCookie = getAdminCookie();

        when(osClientService.upload(any(), any())).thenReturn(testUrl);
        when(osClientService.generateSignUrl(testKey)).thenReturn(testUrl);
        when(osClientService.generateSignUrl(updatedTestKey)).thenReturn(updatedTestUrl);

        HttpResponse<String> uploadFileHttpResponse = sendUploadFileRequest(adminCookie);
        Response<String> uploadFileResponse = objectMapper.readValue(uploadFileHttpResponse.body(), new TypeReference<>() {
        });
        assertEquals(testUrl, uploadFileResponse.getData());

        // 增删改查 => 增删改查 课程管理权限(teacher & administrator)
        // 增加
        VideoQuery pendingCreateVideo = createVideo(testUrl);

        Response<VideoVO> createdVideoResponse = createVideoInDb(adminCookie, pendingCreateVideo);
        Integer createdVideoId = createdVideoResponse.getData().getId();

        // 获取视频 => 200 + VideoVO
        HttpResponse<String> res = get("/video/" + createdVideoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<VideoVO> getVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(createdVideoResponse.getData().getName(), getVideoResponse.getData().getName());
        assertEquals(createdVideoResponse.getData().getDescription(), getVideoResponse.getData().getDescription());
        assertEquals(createdVideoResponse.getData().getUrl(), getVideoResponse.getData().getUrl());
        assertEquals(Status.OK, getVideoResponse.getData().getStatus());

        // 修改视频信息 => 200 + 修改后的 VideoVO
        VideoVO pendModifyVideo = getVideoResponse.getData();
        pendModifyVideo.setUrl(updatedTestUrl);
        pendModifyVideo.setDescription("新的视频简介");
        res = patch("/video/" + createdVideoId, objectMapper.writeValueAsString(pendModifyVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<VideoVO> modifiedVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(pendModifyVideo.getName(), modifiedVideoResponse.getData().getName());
        assertEquals(pendModifyVideo.getDescription(), modifiedVideoResponse.getData().getDescription());
        assertEquals(pendModifyVideo.getUrl(), modifiedVideoResponse.getData().getUrl());
        assertEquals(Status.OK, modifiedVideoResponse.getData().getStatus());
        assertEquals(getVideoResponse.getData().getCreatedOn(), modifiedVideoResponse.getData().getCreatedOn());
        assertNotEquals(getVideoResponse.getData().getUpdatedOn(), modifiedVideoResponse.getData().getUpdatedOn());

        // 获取视频 => 200 + 修改后的 VideoVO
        res = get("/video/" + modifiedVideoResponse.getData().getId(), HttpHeaders.COOKIE, adminCookie);
        assertEquals(200, res.statusCode());
        Response<VideoVO> getModifiedVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(modifiedVideoResponse.getData().getName(), getModifiedVideoResponse.getData().getName());
        assertEquals(modifiedVideoResponse.getData().getDescription(), getModifiedVideoResponse.getData().getDescription());
        assertEquals(modifiedVideoResponse.getData().getUrl(), getModifiedVideoResponse.getData().getUrl());
        assertEquals(Status.OK, getModifiedVideoResponse.getData().getStatus());

        // 删除课程 => 204
        res = delete("/video/" + createdVideoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(204, res.statusCode());

        // 获取课程 => 404
        res = get("/video/" + createdVideoId, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    @Test
    public void canGetVideoList() {
        // TODO
    }

    @Test
    public void return403WhenForbidden() throws IOException, InterruptedException, URISyntaxException {
        // student
        String studentCookie = getUserCookie("username=student&password=student");
        VideoQuery pendingCreateVideo = createVideo(testUrl);

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

        // upload file
        HttpResponse<String> response = sendUploadFileRequest(studentCookie);
        assertEquals(403, response.statusCode());

        // get video list
        res = get("/video", HttpHeaders.COOKIE, studentCookie);
        assertEquals(403, res.statusCode());
    }

    @Test
    public void return400WhenCreateOrUpdateVideo() throws IOException, InterruptedException {
        VideoQuery pendingCreateVideo = createVideo(testUrl);
        String adminCookie = getAdminCookie();
        when(osClientService.generateSignUrl(testKey)).thenReturn(testUrl);

        Response<VideoVO> createdVideoResponse = createVideoInDb(adminCookie, pendingCreateVideo);
        VideoVO createdVideo = createdVideoResponse.getData();
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

        pendingCreateVideo.setName(null);
        exception400("/video", pendingCreateVideo, "视频名称不能为空");

        pendingCreateVideo.setName("视频名称");
        pendingCreateVideo.setUrl(null);
        exception400("/video", pendingCreateVideo, "视频地址不能为空");
    }

    @Test
    public void return404WhenDeleteOrUpdateOrGetVideo() throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = delete("/video/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());

        VideoQuery pendingCreateVideo = createVideo(testUrl);
        res = patch("/video/999", objectMapper.writeValueAsString(pendingCreateVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());

        res = get("/video/999", HttpHeaders.COOKIE, adminCookie);
        assertEquals(404, res.statusCode());
    }

    private HttpResponse<String> sendUploadFileRequest(String adminCookie) throws URISyntaxException, IOException, InterruptedException {
        URL url = getClass().getClassLoader().getResource("static/test/200.jpeg");
        HttpEntity httpEntity = MultipartEntityBuilder.create().addBinaryBody("file", Paths.get(url.toURI()).toFile(), ContentType.IMAGE_JPEG, testKey).build();
        Pipe pipe = Pipe.open();
        new Thread(() -> {
            try (OutputStream outputStream = Channels.newOutputStream(pipe.sink())) {
                // Write the encoded data to the pipeline.
                httpEntity.writeTo(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        BodyPublisher bodyPublisher = BodyPublishers.ofInputStream(() -> Channels.newInputStream(pipe.source()));

        return post("/video/upload", bodyPublisher, HttpHeaders.CONTENT_TYPE, httpEntity.getContentType().getValue(), HttpHeaders.COOKIE, adminCookie);
    }
}
