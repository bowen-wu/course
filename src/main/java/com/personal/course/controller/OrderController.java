package com.personal.course.controller;

import com.personal.course.entity.Order;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Response;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    /**
     * @api {get} /api/v1/order 获取订单列表
     * @apiName getOrderList
     * @apiGroup Order Management
     * @apiDescription
     *  获取分页的订单信息。订单信息里包含课程信息和价格。
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParam {String} [search] 搜索关键字
     * @apiParam {Number} [pageSize] 每页包含多少个订单
     * @apiParam {Number} [pageNum] 页码，从1开始
     * @apiParam {String} [orderBy] 排序字段，如price/createdAt
     * @apiParam {String} [orderType] 排序方法，ASC/DESC
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/order?pageSize=10&pageNum=1&orderBy=price&orderType=Desc&search=21天
     * @apiSuccess {Number} totalPage 总页数
     * @apiSuccess {Number} pageNum 当前页码，从1开始
     * @apiSuccess {Number} pageSize 每页包含多少个订单
     * @apiSuccess {Order[]} data 订单列表
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
     *             ],
     *             price: 9900, // 单位 分
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
     * @param pageSize  每页包含多少个订单
     * @param pageNum   页码
     * @param orderType 排序方法
     * @param orderBy   排序字段
     * @param search    搜索值
     * @return 订单列表
     */
    @GetMapping("/order")
    public PageResponse<Order> getOrderList(@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNum") Integer pageNum, @RequestParam("orderBy") Direction orderBy, @RequestParam("orderType") String orderType, @RequestParam("search") String search) {
        return null;
    }

    /**
     * @api {get} /api/v1/order/{id} 获取订单
     * @apiName getOrderById
     * @apiGroup Order Management
     * @apiDescription
     *  获取指定id的订单信息。订单信息里包含课程信息和价格和状态。
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/order/123
     *
     * @apiSuccess {Order} order 订单信息
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
     *             ],
     *             "price": 9900,  // 单位 分
     *          }
     * @apiError 400 Bad request 若请求中包含错误
     * @apiError 404 Not Found 没有该订单
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/order/{id}")
    public Response<Order> getOrderById(@PathVariable("id") Integer orderId) {
        return null;
    }

    /**
     * @api {post} /api/v1/order 下订单
     * @apiName placeOrder
     * @apiGroup Order Management
     * @apiDescription 填写必要信息，下订单
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} courseId 课程Id
     *
     * @apiParamExample Request-Example:
     *          POST /api/v1/order
     *          {
     *             "courseId": 1234,
     *          }
     *
     * @apiSuccess (Success 201) {Order} order 新创建的订单信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 OK
     *          {
     *             "id": 1,
     *             "course": 12345,
     *             "name": "21天精通C++",
     *             "teacherName": "Torvalds Linus",
     *             "teacherDescription": "Creator of Linux",
     *             "price": 9900
     *          }
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
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
     * @return 创建的订单信息
     */
    @PostMapping("/order/{courseId}")
    public Response<Order> placeOrder(@PathVariable("courseId") Integer courseId) {
        return null;
    }

    /**
     * @api {delete} /api/v1/order/{id} 删除订单
     * @apiName deleteOrder
     * @apiGroup Order Management
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParamExample Request-Example:
     *            DELETE /api/v1/order/123
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 204 No Content
     *
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 404 Not Found 没有该订单
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param orderId  订单ID
     * @param response response
     */
    @DeleteMapping("/order/{id}")
    public void deleteOrderById(@PathVariable("id") Integer orderId, HttpServletResponse response) {
    }
}
