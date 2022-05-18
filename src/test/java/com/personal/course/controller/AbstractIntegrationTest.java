package com.personal.course.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.course.CourseApplication;
import com.personal.course.entity.Query.CourseQuery;
import com.personal.course.entity.Query.VideoQuery;
import com.personal.course.entity.Response;
import com.personal.course.entity.Status;
import com.personal.course.entity.VO.CourseVO;
import com.personal.course.entity.VO.VideoVO;
import com.personal.course.service.OSClientService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CourseApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public abstract class AbstractIntegrationTest {
    @LocalServerPort
    private int port;

    @Value("${spring.datasource.url}")
    private String flywayUrl;
    @Value("${spring.datasource.username}")
    private String flywayUser;
    @Value("${spring.datasource.password}")
    private String flywayPassword;

    @MockBean
    OSClientService osClientService;

    private final String test1Key = "test1.mp4";
    private final String test1Url = "http://course-api-doc.oss-cn-hangzhou.aliyuncs.com/" + test1Key + "?Expires=1652001437&OSSAccessKeyId=TMP.3KhT7CnCWSDUdBwvfMkRs4KWoFEEskoAUdD3dn2iabHf4uDQNSnKz1bgJrdNeqZXb9xULBBkcqUkGkHYUVDSQhQ5ERexvt&Signature=WtAw6K4Ome2v0w3HIxdCP0QnE1o%3D";
    private final String test2Key = "test2.mp4";
    private final String test2Url = "http://course-api-doc.oss-cn-hangzhou.aliyuncs.com/" + test2Key + "?Expires=1652001437&OSSAccessKeyId=TMP.3KhT7CnCWSDUdBwvfMkRs4KWoFEEskoAUdD3dn2iabHf4uDQNSnKz1bgJrdNeqZXb9xULBBkcqUkGkHYUVDSQhQ5ERexvt&Signature=WtAw6K4Ome2v0w3HIxdCP0QnE1o%3D";


    public Integer created1VideoId;
    public Integer created2VideoId;
    public final ObjectMapper objectMapper = new ObjectMapper();
    public final HttpClient client = HttpClient.newBuilder().build();

    @BeforeEach
    public void setUp() throws IOException, InterruptedException, URISyntaxException {
        objectMapper.findAndRegisterModules();
        Flyway flyway = Flyway.configure().dataSource(flywayUrl, flywayUser, flywayPassword).load();
        flyway.clean();
        flyway.migrate();

        when(osClientService.generateSignUrl(test1Key)).thenReturn(test1Url);
        when(osClientService.generateSignUrl(test2Key)).thenReturn(test2Url);
        String adminCookie = getAdminCookie();
        this.created1VideoId = createVideoInDb(adminCookie, createVideo("测试视频1", test1Url)).getData().getId();
        this.created2VideoId = createVideoInDb(adminCookie, createVideo("测试视频2", test2Url)).getData().getId();
    }

    public Response<CourseVO> createCourseInDb() throws IOException, InterruptedException {
        CourseQuery pendingCreateCourse = createCourse();

        String adminCookie = getAdminCookie();
        HttpResponse<String> res = post("/course", objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<CourseVO> createdCourseResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(pendingCreateCourse.getName(), createdCourseResponse.getData().getName());
        assertEquals(pendingCreateCourse.getDescription(), createdCourseResponse.getData().getDescription());
        assertEquals(pendingCreateCourse.getTeacherName(), createdCourseResponse.getData().getTeacherName());
        assertEquals(pendingCreateCourse.getTeacherDescription(), createdCourseResponse.getData().getTeacherDescription());
        assertEquals(pendingCreateCourse.getPrice(), createdCourseResponse.getData().getPrice());
        assertEquals(pendingCreateCourse.getVideoIdList(), createdCourseResponse.getData().getVideoList().stream().map(VideoVO::getId).collect(toList()));
        assertEquals(Status.OK, createdCourseResponse.getData().getStatus());
        assertNotNull(createdCourseResponse.getData().getId());

        return createdCourseResponse;
    }

    public CourseQuery createCourse() {
        CourseQuery pendingCreateCourse = new CourseQuery();
        pendingCreateCourse.setName("新增测试课程");
        pendingCreateCourse.setDescription("测试课程简介");
        pendingCreateCourse.setTeacherName("Jack");
        pendingCreateCourse.setTeacherDescription("Jack is a good teacher!");
        pendingCreateCourse.setPrice(29900);
        pendingCreateCourse.setVideoIdList(Arrays.asList(created1VideoId, created2VideoId));
        return pendingCreateCourse;
    }

    public VideoQuery createVideo(String url) {
        return createVideo("新增视频名称", url);
    }

    public VideoQuery createVideo(String name, String url) {
        VideoQuery pendingCreateVideo = new VideoQuery();
        pendingCreateVideo.setName(name);
        pendingCreateVideo.setDescription("测试视频简介");
        pendingCreateVideo.setUrl(url);
        return pendingCreateVideo;
    }

    public Response<VideoVO> createVideoInDb(String adminCookie, VideoQuery pendingCreateVideo) throws IOException, InterruptedException {
        HttpResponse<String> res = post("/video", objectMapper.writeValueAsString(pendingCreateVideo), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(201, res.statusCode());
        Response<VideoVO> createdVideoResponse = objectMapper.readValue(res.body(), new TypeReference<>() {
        });
        assertEquals(pendingCreateVideo.getName(), createdVideoResponse.getData().getName());
        assertEquals(pendingCreateVideo.getDescription(), createdVideoResponse.getData().getDescription());
        assertEquals(pendingCreateVideo.getUrl(), createdVideoResponse.getData().getUrl());
        assertEquals(Status.OK, createdVideoResponse.getData().getStatus());
        assertNotNull(createdVideoResponse.getData().getId());
        return createdVideoResponse;
    }

    public <T> void exception400(String uri, T pendingCreateCourse, String errorMessage) throws IOException, InterruptedException {
        String adminCookie = getAdminCookie();
        HttpResponse<String> res = post(uri, objectMapper.writeValueAsString(pendingCreateCourse), HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE, HttpHeaders.COOKIE, adminCookie);
        assertEquals(400, res.statusCode());
        Response response = objectMapper.readValue(res.body(), Response.class);
        assertEquals(errorMessage, response.getMessage());
    }

    public String getUserCookie(String usernameAndPassword) throws IOException, InterruptedException {
        return getCookieFromResponse(login(usernameAndPassword));
    }

    public String getAdminCookie() throws IOException, InterruptedException {
        return getUserCookie("username=administrator&password=administrator");
    }

    public String getCookieFromResponse(HttpResponse<String> response) {
        return response.headers().allValues(HttpHeaders.SET_COOKIE).get(0);
    }

    public HttpResponse<String> login(String usernameAndPassword) throws IOException, InterruptedException {
        return post("/session", usernameAndPassword, HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    public HttpResponse<String> patch(String uri, String body, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "PATCH", BodyPublishers.ofString(body), headers);
    }

    public HttpResponse<String> post(String uri, String body, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "POST", BodyPublishers.ofString(body), headers);
    }

    public HttpResponse<String> post(String uri, BodyPublisher body, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "POST", body, headers);
    }

    public HttpResponse<String> delete(String uri, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "DELETE", BodyPublishers.noBody(), headers);
    }

    public HttpResponse<String> get(String uri) throws IOException, InterruptedException {
        return getHttpRequest(uri, "GET", BodyPublishers.noBody());
    }

    public HttpResponse<String> get(String uri, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, "GET", BodyPublishers.noBody(), headers);
    }

    public HttpResponse<String> getHttpRequest(String uri, String method, BodyPublisher bodyPublisher, String... headers) throws IOException, InterruptedException {
        return getHttpRequest(uri, method, bodyPublisher, BodyHandlers.ofString(), headers);
    }

    public <T> HttpResponse<T> getHttpRequest(String uri, String method, BodyPublisher bodyPublisher, HttpResponse.BodyHandler<T> responseBodyHandler, String... headers) throws IOException, InterruptedException {
        HttpRequest request;
        Builder builder = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/api/v1" + uri)).method(method, bodyPublisher);
        if (headers.length > 0) {
            request = builder.headers(headers).build();
        }
        request = builder.build();

        return client.send(request, responseBodyHandler);
    }
}
