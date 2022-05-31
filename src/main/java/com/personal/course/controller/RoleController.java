package com.personal.course.controller;

import com.personal.course.configuration.UserContext;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.DO.User;
import com.personal.course.entity.Response;
import com.personal.course.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    @Inject
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * @api {get} /api/v1/roleEnum 获取角色枚举
     * @apiName getRoleEnum
     * @apiDescription 管理员才能访问
     * @apiGroup Role Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *      GET /api/v1/roleEnum
     *
     * @apiSuccess (Success 200) {Role[]} data 角色枚举
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *      {
     *        "data": [
     *           {
     *               "id": 1,
     *               "name": "teacher"
     *           },
     *           {
     *               "id": 2,
     *               "name": "admin"
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
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 403 Forbidden
     *     {
     *       "message": "无权限"
     *     }
     *
     */
    /**
     * @return 角色枚举列表
     */
    @GetMapping("/roleEnum")
    public Response<List<Role>> getRoleEnum() {
        User loggedUser = UserContext.getUser();
        return Response.success(roleService.getRoleEnum(loggedUser));
    }
}
