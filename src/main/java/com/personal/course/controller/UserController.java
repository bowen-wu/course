package com.personal.course.controller;

import com.personal.course.entity.Response;
import com.personal.course.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    /**
     * @api {patch} /api/v1/user 更新用户
     * @apiName updateUseRole
     * @apiDescription 管理员才能访问此接口
     * @apiGroup User Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     * {
     *     "id": 12345,
     *     "roles": [
     *          {
     *              "name": "管理员"
     *          }
     *     ]
     * }
     *
     * @apiSuccess (Success 200) {User} data 更新后的用户
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *         "id": 12345,
     *         "username": "Alice",
     *         "roles": [
     *              {
     *                  "name": "管理员"
     *              }
     *         ]
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
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     *
     * @apiError 404 Not Found 若用户未找到
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 404 Not Found
     *     {
     *       "message": "没有该用户"
     *     }
     */
    /**
     *
     */
    @PatchMapping("/user")
    @ResponseBody
    public Response<User> updateUseRole() {
        return null;
    }

    /**
     * @api {get} /api/v1/user/{id} 获取指定id的用户
     * @apiName getUserById
     * @apiDescription 管理员才能访问此接口
     * @apiGroup User Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *      GET /api/v1/user/1
     *
     * @apiSuccess (Success 200) {User} data 用户
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *         "id": 12345,
     *         "username": "Alice",
     *         "roles": [
     *              {
     *                  "name": "管理员"
     *              }
     *         ]
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
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     *
     * @apiError 404 Not Found 若用户未找到
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 404 Not Found
     *     {
     *       "message": "没有该用户"
     *     }
     */
    /**
     * @param id 用户ID
     */
    @GetMapping("/user/{id}")
    @ResponseBody
    public Response<User> getUserById(@PathVariable("id") String id) {
        return null;
    }

    /**
     * @api {get} /api/v1/user 获取用户列表
     * @apiName getUserList
     * @apiDescription 管理员才能访问，获取分页的用户信息，支持搜索
     * @apiGroup User Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {Number} [pageSize] 每页包含多少个用户
     * @apiParam {Number} [pageNum] 页码，从1开始
     * @apiParam {String} [search] 搜索值
     * @apiParam {String} [orderBy] 排序字段，如 username | createdAt
     * @apiParam {String} [orderType] 排序方法，Asc | Desc
     *
     * @apiParamExample Request-Example:
     *      GET /api/v1/user?pageSize=10&pageNum=2&orderType=Desc&search=zhang
     *
     * @apiSuccess (Success 200) {User} data 用户
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *      {
     *        "totalPage": 100,
     *        "pageSize": 10,
     *        "pageNum": 1,
     *        "data": [
     *           {
     *               "id": 12345
     *               "username": "zhangsan"
     *               "roles": [
     *                   {
     *                       "name": "管理员"
     *                   }
     *               ]
     *           }
     *        ]
     *      }
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
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     *
     */
    /**
     * @param pageSize  每页包含多少个用户
     * @param pageNum   页码
     * @param orderType 排序方法
     * @param orderBy   排序字段
     * @param search    搜索值
     * @return 用户列表
     */
    @GetMapping("/user")
    @ResponseBody
    public Response<User> getUserList(@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNum") Integer pageNum, @RequestParam("orderType") String orderType, @RequestParam("orderBy") String orderBy, @RequestParam("search") String search) {
        return null;
    }
}
