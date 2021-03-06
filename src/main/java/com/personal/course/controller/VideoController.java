package com.personal.course.controller;

import com.personal.course.annotation.ManagementCourse;
import com.personal.course.configuration.UserContext;
import com.personal.course.entity.DO.Video;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Query.VideoQuery;
import com.personal.course.entity.Response;
import com.personal.course.entity.VO.VideoVO;
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

import static com.personal.course.entity.PageResponse.DEFAULT_ORDER_BY;
import static com.personal.course.entity.PageResponse.DEFAULT_ORDER_TYPE;
import static com.personal.course.entity.PageResponse.DEFAULT_PAGE_NUM;
import static com.personal.course.entity.PageResponse.DEFAULT_PAGE_SIZE;

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
    public Response<VideoVO> createVideo(@RequestBody com.personal.course.entity.Query.VideoQuery videoQuery, HttpServletResponse response) {
        cleanUp(videoQuery);
        response.setStatus(HttpStatus.CREATED.value());
        return Response.success(videoService.createVideo(videoQuery));
    }

    private void cleanUp(VideoQuery videoQuery) {
        if (videoQuery.getName() == null) {
            throw HttpException.badRequest("视频名称不能为空");
        }
        if (videoQuery.getUrl() == null) {
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
    public Response<VideoVO> updateVideo(@PathVariable("id") Integer videoId, @RequestBody VideoQuery videoQuery) {
        cleanUp(videoQuery);
        return Response.success(videoService.updateVideo(videoId, videoQuery));
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
    public Response<VideoVO> getVideoById(@PathVariable("id") Integer videoId) {
        return Response.success(videoService.getVideoVoById(videoId));
    }

    /**
     * @api {get} /api/v1/video/{courseId}/{videoId} 用于学生获取视频信息
     * @apiName getVideoByIdAndCourseId
     * @apiGroup Video Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *          GET /api/v1/video/1/2
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
     * @param videoId  视频ID
     * @param courseId 课程ID
     * @return 视频信息
     */
    @GetMapping("/video/{courseId}/{videoId}")
    public Response<VideoVO> getVideoByIdAndCourseId(@PathVariable("videoId") Integer videoId, @PathVariable("courseId") Integer courseId) {
        return Response.success(videoService.getVideoVoByVideoIdAndCourseId(videoId, courseId, UserContext.getUser().getId()));
    }

    /**
     * @api {get} /api/v1/video/upload 上传视频
     * @apiName uploadVideo
     * @apiGroup Video Management
     * @apiDescription 需要"课程管理"权限
     *
     * @apiHeader {String} Accept multipart/form-data
     *
     * @apiParamExample Request-Example:
     *   POST /api/v1/video/upload
     *   Form Data
     *       file: (binary)
     *
     * @apiSuccess (Success 200) {String} data 视频地址
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "data":"http://post-test.oss-cn-hangzhou.aliyuncs.com",
     *     }
     *
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
     *
     */
    @PostMapping(value = "/video/upload")
    @ManagementCourse
    public Response<String> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        return Response.success(osClientService.upload(file.getInputStream(), file.getName()));
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
     * @apiParam {String} [search] 搜索关键字 - 视频名称
     * @apiParam {Number} [pageSize] 每页包含多少个视频
     * @apiParam {Number} [pageNum] 页码，从1开始
     * @apiParam {String} [orderBy] 排序字段，如id/name
     * @apiParam {String} [orderType] 排序方法，ASC/DESC
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/video?pageSize=10&pageNum=1&orderBy=id&orderType=Desc&search=21天
     * @apiSuccess {Number} total 总数
     * @apiSuccess {Number} pageNum 当前页码，从1开始
     * @apiSuccess {Number} pageSize 每页包含多少个视频
     * @apiSuccess {Video[]} data 视频列表
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "total": 100,
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
    public PageResponse<Video> getVideoList(
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "orderBy", required = false) Direction orderBy,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "search", required = false) String search) {
        if (pageNum == null) pageNum = DEFAULT_PAGE_NUM;
        if (pageSize == null) pageSize = DEFAULT_PAGE_SIZE;
        if (orderBy == null) orderBy = DEFAULT_ORDER_BY;
        if (orderType == null) orderType = DEFAULT_ORDER_TYPE;
        return videoService.getVideoList(pageNum, pageSize, orderType, orderBy, search);
    }
}
