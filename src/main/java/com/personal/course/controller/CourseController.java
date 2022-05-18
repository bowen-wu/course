package com.personal.course.controller;

import com.personal.course.annotation.ManagementCourse;
import com.personal.course.configuration.UserContext;
import com.personal.course.entity.DO.Course;
import com.personal.course.entity.Query.CourseQuery;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Response;
import com.personal.course.entity.VO.CourseVO;
import com.personal.course.service.CourseService;
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

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import static com.personal.course.entity.PageResponse.DEFAULT_ORDER_BY;
import static com.personal.course.entity.PageResponse.DEFAULT_ORDER_TYPE;
import static com.personal.course.entity.PageResponse.DEFAULT_PAGE_NUM;
import static com.personal.course.entity.PageResponse.DEFAULT_PAGE_SIZE;

/**
 * 课程管理相关
 */
@RestController
@RequestMapping("/api/v1")
public class CourseController {
    private final CourseService courseService;

    @Inject
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * @api {get} /api/v1/course 获取课程列表
     * @apiName 获取课程列表
     * @apiGroup Course Management
     * @apiDescription
     *  获取分页的课程信息。课程信息里包含视频信息和教师信息。
     *  若视频的URL为空，证明未购买该课程。
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParam {String} [search] 搜索关键字，课程名称
     * @apiParam {Number} [pageSize] 每页包含多少个课程
     * @apiParam {Number} [pageNum] 页码，从1开始
     * @apiParam {String} [orderBy] 排序字段，如price/createdAt
     * @apiParam {String} [orderType] 排序方法，ASC/DESC
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/course?pageSize=10&pageNum=1&orderBy=price&orderType=Desc&search=21天&userId=1
     * @apiSuccess {Number} totalPage 总页数
     * @apiSuccess {Number} pageNum 当前页码，从1开始
     * @apiSuccess {Number} pageSize 每页包含多少个课程
     * @apiSuccess {Course[]} data 课程列表
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "totalPage": 100,
     *       "pageSize": 10,
     *       "pageNum": 1,
     *       "data": [
     *          {
     *             "id": 12345,
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             videos: [
     *                {
     *                    "id": 456,
     *                    "name": "第一课",
     *                    "description": "",
     *                    "url": "https://oss.aliyun.com/xxx"
     *                }
     *             ]
     *             price: 9900,
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
     * @param pageSize  每页包含多少个课程
     * @param pageNum   页码
     * @param orderType 排序方法
     * @param orderBy   排序字段
     * @param search    搜索值
     * @return 课程列表
     */
    @GetMapping("/course")
    public PageResponse<CourseVO> getCourses(
            @RequestParam(required = false, value = "pageSize") Integer pageSize,
            @RequestParam(required = false, value = "pageNum") Integer pageNum,
            @RequestParam(required = false, value = "orderBy") Direction orderBy,
            @RequestParam(required = false, value = "orderType") String orderType,
            @RequestParam(required = false, value = "search") String search) {
        if (pageNum == null) pageNum = DEFAULT_PAGE_NUM;
        if (pageSize == null) pageSize = DEFAULT_PAGE_SIZE;
        if (orderBy == null) orderBy = DEFAULT_ORDER_BY;
        if (orderType == null) orderType = DEFAULT_ORDER_TYPE;
        return courseService.getCourseList(pageNum, pageSize, orderType, orderBy, search);
    }

    /**
     * @api {get} /api/v1/course/{id} 获取课程
     * @apiName 获取课程
     * @apiGroup Course Management
     * @apiDescription
     *  获取指定id的课程信息。课程信息里包含视频信息和教师信息。
     *  若视频的URL为空，证明未购买该课程。
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/course/123
     *
     * @apiSuccess {Course} course 课程信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *          {
     *             "id": 12345,
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             "videos": [
     *                {
     *                    "id": 456,
     *                    "name": "第一课",
     *                    "description": "",
     *                    "url": "https://oss.aliyun.com/xxx"
     *                }
     *             ]
     *             "price": 9900,
     *          }
     * @apiError 400 Bad request 若请求中包含错误
     * @apiError 404 Not Found 没有该课程
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param courseId 课程ID
     * @return 课程信息
     */
    @GetMapping("/course/{id}")
    public Response<CourseVO> getCourse(@PathVariable("id") Integer courseId) {
        return Response.success(courseService.getCourse(courseId, UserContext.getUser().getId()));
    }

    /**
     * @api {post} /api/v1/course 创建课程
     * @apiName 创建课程
     * @apiGroup Course Management
     * @apiDescription 填写必要信息，创建一门课程。需要"课程管理"权限。
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} name 课程名称
     * @apiParam {String} description 课程简介
     * @apiParam {String} teacherName 老师姓名
     * @apiParam {String} price 价格 分
     * @apiParam {String} [teacherDescription] 老师简介
     * @apiParam {String[]} [videos] 视频Id数组
     * @apiParamExample Request-Example:
     *          POST /api/v1/course
     *          {
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             "price": 9900
     *          }
     *
     * @apiSuccess (Success 201) {Course} course 新创建的课程信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 OK
     *          {
     *             "id": 12345,
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             "price": 9900
     *          }
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
     * @param courseQuery 课程
     * @param response    response
     * @return 创建的课程信息
     */
    @PostMapping("/course")
    @ManagementCourse
    public Response<CourseVO> createCourse(@RequestBody CourseQuery courseQuery, HttpServletResponse response) {
        cleanUp(courseQuery);
        response.setStatus(HttpStatus.CREATED.value());
        return Response.success(courseService.createCourse(courseQuery));
    }

    private void cleanUp(CourseQuery course) {
        if (course.getName() == null) {
            throw HttpException.badRequest("课程名称不能为空");
        }
        if (course.getDescription() == null) {
            throw HttpException.badRequest("课程简介不能为空");
        }
        if (course.getTeacherName() == null) {
            throw HttpException.badRequest("老师姓名不能为空");
        }
        if (course.getPrice() == null) {
            throw HttpException.badRequest("课程价格不能为空");
        }
    }

    /**
     * @api {delete} /api/v1/course/{id} 删除课程
     * @apiName 删除课程
     * @apiGroup Course Management
     * @apiDescription
     *  需要"课程管理"权限。
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *            DELETE /api/v1/course/123
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 204 No Content
     *
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 403 Forbidden 若无权限
     * @apiError 404 Not Found 没有该课程
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param courseId 课程ID
     * @param response response
     */
    @DeleteMapping("/course/{id}")
    @ManagementCourse
    public void deleteCourse(@PathVariable("id") Integer courseId, HttpServletResponse response) {
        if (courseId == null) {
            throw HttpException.badRequest("id不合法");
        }
        response.setStatus(HttpStatus.NO_CONTENT.value());
        courseService.deleteCourseById(courseId);
    }

    /**
     * @api {patch} /api/v1/course/{id} 修改课程
     * @apiName 修改课程
     * @apiGroup Course Management
     * @apiDescription
     *  需要"课程管理"权限。
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} [name] 课程名称
     * @apiParam {String} [description] 课程简介
     * @apiParam {String} [teacherName] 老师姓名
     * @apiParam {String} [teacherDescription] 老师简介
     * @apiParam {String} [price] 价格 分
     * @apiParam {String[]} [videos] 视频Id数组
     *
     * @apiParamExample Request-Example:
     * PATCH /api/v1/course
     *          {
     *             "id": 12345,
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             "price": 9900
     *          }
     *
     * @apiSuccess {Course} course 修改后的课程信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *          {
     *             "id": 12345,
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             "price": 9900
     *          }
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 403 Forbidden 若无权限
     * @apiError 404 Not Found 没有该课程
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param courseId 课程ID
     * @param course   修改的课程信息
     * @return 修改后的课程信息
     */
    @PatchMapping("/course/{id}")
    @ManagementCourse
    public Response<CourseVO> updateCourse(@PathVariable("id") Integer courseId, @RequestBody CourseQuery course) {
        cleanUp(course);
        return Response.success(courseService.updateCourse(courseId, course, UserContext.getUser().getId()));
    }
}
