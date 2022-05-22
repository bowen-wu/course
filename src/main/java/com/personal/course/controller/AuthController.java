package com.personal.course.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.personal.course.configuration.UserContext;
import com.personal.course.entity.DO.Session;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Response;
import com.personal.course.entity.VO.UsernameAndPassword;
import com.personal.course.service.AuthService;
import com.personal.course.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;

import static com.personal.course.configuration.AuthInterceptor.COOKIE_NAME;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;

    @Inject
    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    /**
     * @api {post} /api/v1/user 注册
     * @apiName 注册
     * @apiGroup Auth Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
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
     *         "username": "Alice",
     *         "roles": [
     *              {
     *                  name: "student" // 角色名称
     *                  id: 12
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
     * @apiError 409 Conflict 若用户名已经被注册
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 409 Conflict
     *     {
     *       "message": "用户名已经被注册"
     *     }
     */
    /**
     * @param usernameAndPassword 用户名和密码
     * @param response            response
     */
    @PostMapping("/user")
    @ResponseBody
    public Response<User> register(@RequestBody UsernameAndPassword usernameAndPassword, HttpServletResponse response) {
        cleanParameter(usernameAndPassword);
        response.setStatus(HttpStatus.CREATED.value());
        return Response.success(authService.registerUser(usernameAndPassword));
    }

    /**
     * @api {post} /api/v1/session 登录
     * @apiName 登录
     * @apiGroup Auth Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} username 用户名
     * @apiParam {String} password 密码
     * @apiParamExample Request-Example:
     *          username: Alice
     *          password: MySecretPassword
     *
     * @apiSuccess {User} data 登录的用户
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "user": {
     *           "id": 123,
     *           "username": "Alice",
     *           "roles": [
     *               {
     *                   name: "student" // 角色名称
     *                   id: 12
     *               }
     *           ]
     *        }
     *     }
     *
     * @apiError 400 Bad Request 若用户的请求包含错误
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     * @apiError 404 Not Found 该用户没有注册
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 404 Not Found
     *     {
     *       "message": "Not Found"
     *     }
     */
    /**
     * @param usernameAndPassword 用户名和密码
     * @param response            response
     */
    @PostMapping("/session")
    @ResponseBody
    public Response<User> login(@RequestBody UsernameAndPassword usernameAndPassword, HttpServletResponse response) {
        cleanParameter(usernameAndPassword);
        User userInDB = authService.getUserByUsername(usernameAndPassword.getUsername());
        if (userInDB == null) {
            throw HttpException.notFound("该用户尚未注册！");
        }
        if (BCrypt.verifyer().verify(usernameAndPassword.getPassword().toCharArray(), userInDB.getEncrypted_password()).verified) {
            // 账号密码正确
            String cookieValue = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
            response.addCookie(cookie);

            Session session = new Session(cookieValue, userInDB);
            sessionService.deleteSessionByUserId(userInDB.getId());
            sessionService.save(session);
            return Response.success(userInDB);
        } else {
            throw HttpException.badRequest("密码错误！");
        }
    }

    /**
     * @api {get} /api/v1/session 检查登录状态
     * @apiName 检查登录状态
     * @apiGroup Auth Management
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiSuccess {User} user 用户信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "user": {
     *           "id": 123,
     *           "username": "Alice",
     *           "roles": [
     *               {
     *                   name: "student" // 角色名称
     *                   id: 12
     *               }
     *           ]
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
    public Response<User> authStatus() {
        User user = UserContext.getUser();
        if (user == null) {
            throw HttpException.unauthorized();
        }
        return Response.success(user);
    }

    /**
     * @api {delete} /api/v1/session 登出
     * @apiName 登出
     * @apiGroup Auth Management
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
     * @param request  request
     * @param response response
     */
    @DeleteMapping("/session")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (UserContext.getUser() == null) {
            throw HttpException.unauthorized("未登录");
        }
        Arrays.stream(request.getCookies())
                .filter(item -> item.getName().equals(COOKIE_NAME))
                .map(Cookie::getValue)
                .findFirst()
                .ifPresent(sessionService::deleteSessionByCookie);

        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }

    private void cleanParameter(UsernameAndPassword usernameAndPassword) {
        // 清洗参数
        if (usernameAndPassword.getUsername().length() < 6 || usernameAndPassword.getPassword().length() < 6) {
            throw HttpException.badRequest("账号密码长度不够");
        }
    }

}
