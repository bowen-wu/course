package com.personal.course.controller;

import com.personal.course.configuration.UserContext;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.User;
import com.personal.course.service.AuthService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    @Inject
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * @api {post} /api/v1/user 注册
     * @apiName 注册
     * @apiGroup 登录与鉴权
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/x-www-form-urlencoded
     *
     * @apiParam {String} username 用户名
     * @apiParam {String} password 密码
     * @apiParamExample Request-Example:
     *          username: Alice
     *          password: MySecretPassword
     *
     * @apiSuccess (Success 201) {User} data 创建的用户
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 Created
     *     {
     *         "id": 123,
     *         "username": "Alice"
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
     * @apiError 409 Conflict 若用户名已经被注册
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 409 Conflict
     *     {
     *       "message": "用户名已经被注册"
     *     }
     */
    /**
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping("/user")
    @ResponseBody
    public User register(@RequestParam("username") String username, @RequestParam("password") String password) {
        return null;
    }

    /**
     * @api {post} /api/v1/session 登录
     * @apiName 登录
     * @apiGroup 登录与鉴权
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/x-www-form-urlencoded
     *
     * @apiParam {String} username 用户名
     * @apiParam {String} password 密码
     * @apiParamExample Request-Example:
     *          username: Alice
     *          password: MySecretPassword
     *
     * @apiSuccess {User} data 登录的用户
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 Created
     *     {
     *       "user": {
     *           "id": 123,
     *           "username": "Alice"
     *       }
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping("/session")
    @ResponseBody
    public User login(@RequestParam("username") String username, @RequestParam("password") String password) {
        return null;
    }

    /**
     * @api {get} /api/v1/session 检查登录状态
     * @apiName 检查登录状态
     * @apiGroup 登录与鉴权
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiSuccess {User} user 用户信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "user": {
     *           "id": 123,
     *           "username": "Alice"
     *       }
     *     }
     * @apiError 401 Unauthorized 若用户未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "Unauthorized"
     *     }
     */
    /**
     * @return 已登录的用户
     */
    @GetMapping("/session")
    public User authStatus() {
        User user = UserContext.getUser();
        if (user == null) {
            throw HttpException.unauthorized();
        }
        return user;
    }

    /**
     * @api {delete} /api/v1/session 登出
     * @apiName 登出
     * @apiGroup 登录与鉴权
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 204 No Content
     * @apiError 401 Unauthorized 若用户未登录
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 401 Unauthorized
     *     {
     *       "message": "Unauthorized"
     *     }
     */
    /**
     *
     */
    @DeleteMapping("/session")
    public void logout() {
    }
}
