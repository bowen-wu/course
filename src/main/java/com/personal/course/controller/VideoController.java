package com.personal.course.controller;

import com.personal.course.annotation.ManagementCourse;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Response;
import com.personal.course.entity.Video;
import com.personal.course.service.OSClientService;
import com.personal.course.service.VideoService;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class VideoController {

    private final OSClientService osClientService;
    private final VideoService videoService;

    @Inject
    public VideoController(OSClientService osClientService, VideoService videoService) {
        this.osClientService = osClientService;
        this.videoService = videoService;
    }

    @PostMapping("/test")
    public Response<String> test(@RequestParam("file") MultipartFile file) throws IOException {
        return Response.success(osClientService.upload(file.getInputStream(), file.getOriginalFilename()));
    }

    @GetMapping("/test")
    public String test(@RequestParam("source") String source) {
        return osClientService.generateSignUrl(source);
    }

    /**
     * @api {post} /api/v1/video 创建视频
     * @apiName createVideo
     * @apiGroup Video Management
     * @apiDescription 需要"课程管理"权限
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} name 视频名称
     * @apiParam {String} url 视频地址
     * @apiParam {String} [description] 视频描述
     * @apiParamExample Request-Example:
     *          POST /api/v1/video
     *          {
     *             "name": "第一课",
     *             "description": "这是第一课的视频",
     *             "url": "https://oos.aliyun.com/xxx"
     *          }
     *
     * @apiSuccess (Success 201) {Video} data 创建的视频
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 Created
     *     {
     *         "id": 12345,
     *         "name": "第一课",
     *         "description": "这是第一课的视频",
     *         "url": "https://oos.aliyun.com/xxx"
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     *
     * @apiError 401 Unauthorized 若未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "用户未登录"
     *     }
     *
     * @apiError 403 Forbidden 若没有权限
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     *
     */
    /**
     *
     */
    @PostMapping("/video")
    @ManagementCourse
    public Response<Video> createVideo(@RequestBody Video video, HttpServletResponse response) {
        cleanUp(video);
        response.setStatus(HttpStatus.CREATED.value());
        return Response.success(videoService.createVideo(new Video(video)));
    }

    private void cleanUp(Video video) {
        if (video.getName() == null) {
            throw HttpException.badRequest("视频名称不能为空");
        }
        if (video.getUrl() == null) {
            throw HttpException.badRequest("视频地址不能为空");
        }
    }

    /**
     * @api {delete} /api/v1/video/{id} 删除视频
     * @apiName deleteVideo
     * @apiGroup Video Management
     * @apiDescription 需要"课程管理"权限
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *          DELETE /api/v1/video/1
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 204 No Content
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     *
     * @apiError 401 Unauthorized 若未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "用户未登录"
     *     }
     *
     * @apiError 403 Forbidden 若没有权限
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     * @apiError 404 Not Found 若视频未找到
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 404 Not Found
     *     {
     *       "message": "没有该视频"
     *     }
     */
    /**
     *
     */
    @DeleteMapping("/video/{id}")
    @ManagementCourse
    public void deleteVideo(@PathVariable("id") Integer videoId, HttpServletResponse response) {
        response.setStatus(HttpStatus.NO_CONTENT.value());
        videoService.deleteVideo(videoId);
    }

    /**
     * @api {patch} /api/v1/video/{id} 修改视频
     * @apiName updateVideo
     * @apiGroup Video Management
     * @apiDescription 需要"课程管理"权限
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} name 视频名称
     * @apiParam {String} url 视频地址
     * @apiParam {String} [description] 视频描述
     * @apiParamExample Request-Example:
     *          PATCH /api/v1/video/12345
     *          {
     *             "name": "第一课",
     *             "description": "更新简介",
     *             "url": "https://oos.aliyun.com/xxx"
     *          }
     *
     * @apiSuccess (Success 200) {Video} data 更新后的视频信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *         "id": 12345,
     *         "name": "第一课",
     *         "description": "更新简介",
     *         "url": "https://oos.aliyun.com/xxx"
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     *
     * @apiError 401 Unauthorized 若未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "用户未登录"
     *     }
     *
     * @apiError 403 Forbidden 若没有权限
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     *
     * @apiError 404 Not Found 若视频未找到
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 404 Not Found
     *     {
     *       "message": "没有该视频"
     *     }
     */
    /**
     *
     */
    @PatchMapping("/video/{id}")
    @ManagementCourse
    public Response<Video> updateVideo(@PathVariable("id") Integer videoId, @RequestBody Video video) {
        cleanUp(video);
        return Response.success(videoService.updateVideo(videoId, video));
    }

    /**
     * @api {get} /api/v1/video/{id} 获取视频
     * @apiName getVideoById
     * @apiGroup Video Management
     * @apiDescription 需要"课程管理"权限
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *          GET /api/v1/video/12345
     *
     * @apiSuccess (Success 200) {Video} data 视频信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *         "id": 12345,
     *         "name": "第一课",
     *         "description": "这是第一课的视频",
     *         "url": "https://oos.aliyun.com/xxx"
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     *
     * @apiError 401 Unauthorized 若未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "用户未登录"
     *     }
     *
     * @apiError 403 Forbidden 若没有权限
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     * @apiError 404 Not Found 若视频未找到
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 404 Not Found
     *     {
     *       "message": "没有该视频"
     *     }
     *
     */
    /**
     *
     */
    @GetMapping("/video/{id}")
    @ManagementCourse
    public Response<Video> getVideoById(@PathVariable("id") Integer videoId) {
        return Response.success(videoService.getVideoById(videoId));
    }


    /**
     * @api {get} /api/v1/video/token 获取上传视频所需token
     * @apiName 获取在指定课程下上传视频所需token等验证信息
     * @apiGroup Video Management
     * @apiDescription
     *  验证信息不止包括token。详见 https://help.aliyun.com/document_detail/31927.html。需要"课程管理"权限
     *
     *  当客户端上传成功时，应调用createVideo接口发起一个新的POST请求将视频URL发给应用。
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParamExample Request-Example:
     *   GET /api/v1/video/token
     * @apiSuccess {String} accessid
     * @apiSuccess {String} host
     * @apiSuccess {String} policy
     * @apiSuccess {String} signature
     * @apiSuccess {Number} expire
     * @apiSuccess {String} dir
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "accessid":"6MKO******4AUk44",
     *       "host":"http://post-test.oss-cn-hangzhou.aliyuncs.com",
     *       "policy":"MCwxMDQ4NTc2MDAwXSxbInN0YXJ0cy13aXRoIiwiJGtleSIsInVzZXItZGlyXC8iXV19",
     *       "signature":"VsxOcOudx******z93CLaXPz+4s=",
     *       "expire":1446727949,
     *       "dir":"user-dirs/"
     *     }
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 403 Forbidden 若无权限
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @return
     */
    @GetMapping("/video/token")
    @ManagementCourse
    public Object getToken() {
        return null;
    }


    /**
     * @api {get} /api/v1/video 获取视频列表
     * @apiName getVideoList
     * @apiGroup Video Management
     * @apiDescription
     *  获取分页的视频列表。需要"课程管理"权限
     *
     *  如果是老师，则获取所有自己上传的视频列表
     *
     *  如果是管理员，获取所有的视频列表
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParam {String} [search] 搜索关键字
     * @apiParam {Number} [pageSize] 每页包含多少个视频
     * @apiParam {Number} [pageNum] 页码，从1开始
     * @apiParam {String} [orderBy] 排序字段，如id/name
     * @apiParam {String} [orderType] 排序方法，ASC/DESC
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/video?pageSize=10&pageNum=1&orderBy=id&orderType=Desc&search=21天
     * @apiSuccess {Number} totalPage 总页数
     * @apiSuccess {Number} pageNum 当前页码，从1开始
     * @apiSuccess {Number} pageSize 每页包含多少个视频
     * @apiSuccess {Video[]} data 视频列表
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "totalPage": 100,
     *       "pageSize": 10,
     *       "pageNum": 1,
     *       "data": [
     *          {
     *             "name": "第一课",
     *             "description": "这是第一课的视频",
     *             "url": "https://oos.aliyun.com/xxx"
     *          }
     *       ]
     *     }
     * @apiError 400 Bad request 若请求中包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param pageSize  每页包含多少个视频
     * @param pageNum   页码
     * @param orderType 排序方法
     * @param orderBy   排序字段
     * @param search    搜索值
     * @return 视频列表
     */
    @GetMapping("/video")
    @ManagementCourse
    public PageResponse<Video> getVideoList(@RequestParam(required = false, name = "pageSize") Integer pageSize, @RequestParam(required = false, name = "pageNum") Integer pageNum, @RequestParam(required = false, name = "orderBy") Direction orderBy, @RequestParam(required = false, name = "orderType") String orderType, @RequestParam(required = false, name = "search") String search) {
        return null;
    }
}