package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.VideoVo;
import com.personal.course.service.OSClientService;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

class VideoVoIntegrationTest extends AbstractIntegrationTest {
    private final String testKey = "test.mp4";
    private final String testUrl = "http://course-api-doc.oss-cn-hangzhou.aliyuncs.com/" + testKey + "?Expires=1652001437&OSSAccessKeyId=TMP.3KhT7CnCWSDUdBwvfMkRs4KWoFEEskoAUdD3dn2iabHf4uDQNSnKz1bgJrdNeqZXb9xULBBkcqUkGkHYUVDSQhQ5ERexvt&Signature=WtAw6K4Ome2v0w3HIxdCP0QnE1o%3D";
    private final String updatedTestKey = "MTB.mp4";
    private final String updatedTestUrl = "http://course-api-doc.oss-cn-hangzhou.aliyuncs.com/" + updatedTestKey + "?Expires=1652001437&OSSAccessKeyId=TMP.3KhT7CnCWSDUdBwvfMkRs4KWoFEEskoAUdD3dn2iabHf4uDQNSnKz1bgJrdNeqZXb9xULBBkcqUkGkHYUVDSQhQ5ERexvt&Signature=WtAw6K4Ome2v0w3HIxdCP0QnE1o%3D";

    @MockBean
    OSClientService osClientService;

    @Test
    public void testVideoVoProcess() throws IOException, InterruptedException, URISyntaxException {
        String adminCookie = getAdminCookie();

        Mockito.when(osClientService.upload(any(), any())).thenReturn(testUrl);
        Mockito.when(osClientService.generateSignUrl(testKey)).thenReturn(testUrl);
        Mockito.when(osClientService.generateSignUrl(updatedTestKey)).thenReturn(updatedTestUrl);

        HttpResponse<String> uploadFileHttpResponse = sendUploadFileRequest(adminCookie);
        Response<String> uploadFileResponse = objectMapper.readValue(uploadFileHttpResponse.body(), new TypeReference<>() {
        });
        assertEquals(testUrl, uploadFileResponse.getData());

        // 增删改查 => 增删改查 课程管理权限(teacher & administrator)
        // 增加
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
        pendModifyVideoVo.setUrl(updatedTestUrl);
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
    public void canGetVideoVoList() {
        // TODO
    }

    @Test
    public void return403WhenForbidden() throws IOException, InterruptedException, URISyntaxException {
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

        // upload file
        HttpResponse<String> response = sendUploadFileRequest(studentCookie);
        assertEquals(403, response.statusCode());

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
        pendingCreateVideoVo.setUrl(testUrl);
        return pendingCreateVideoVo;
    }

    private void assertVideoVoPropertyEquals(VideoVo pendingCreateVideoVo, VideoVo createdVideoVo) {
        assertEquals(pendingCreateVideoVo.getName(), createdVideoVo.getName());
        assertEquals(pendingCreateVideoVo.getDescription(), createdVideoVo.getDescription());
        assertEquals(pendingCreateVideoVo.getUrl(), createdVideoVo.getUrl());
    }

    private BodyPublisher oMultipartData(Map<Object, Object> data, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName() + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return BodyPublishers.ofByteArrays(byteArrays);
    }

    private HttpResponse<String> sendUploadFileRequest(String adminCookie) throws URISyntaxException, IOException, InterruptedException {
        URL url = getClass().getClassLoader().getResource("static/200.jpeg");
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
