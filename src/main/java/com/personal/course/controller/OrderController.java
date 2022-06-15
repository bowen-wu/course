package com.personal.course.controller;

import com.personal.course.configuration.UserContext;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.OrderWithComponentHtml;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Response;
import com.personal.course.service.OrderService;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import static com.personal.course.entity.PageResponse.DEFAULT_ORDER_BY;
import static com.personal.course.entity.PageResponse.DEFAULT_ORDER_TYPE;
import static com.personal.course.entity.PageResponse.DEFAULT_PAGE_NUM;
import static com.personal.course.entity.PageResponse.DEFAULT_PAGE_SIZE;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private final OrderService orderService;

    @Inject
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * @api {post} /api/v1/order/{courseId} 下订单
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
     *          POST /api/v1/order/1234
     *
     * @apiSuccess (Success 201) {Order} order 新创建的订单信息，其中包括了支付的部分HTML，可嵌入页面
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 201 Created
     *          {
     *             "id": 1234,
     *             "status": "UNPAID"  // DELETED, UNPAID, CLOSED, PAID
     *             "userId": 123,
     *             "course": {
     *                 "id": 1,
     *                 "createdOn": "2022-06-03T14:20:45.760671Z",
     *                 "name": "21天精通C++",
     *                 "teacherName": "Torvalds Linus",
     *                 "teacherDescription": "Creator of Linux",
     *                 "price": 9900,
     *             },
     *             "tradeNo": "course_1_20220605095549",
     *             "price": 9900,
     *             "formComponentHtml":  <form name="submit_form" method="post" action="https://openapi.alipay.com/gateway.do?charset=UTF-8&method=alipay.trade.page.pay&sign=k0w1DePFqNMQWyGBwOaEsZEJuaIEQufjoPLtwYBYgiX%2FRSkBFY38VuhrNumXpoPY9KgLKtm4nwWz4DEQpGXOOLaqRZg4nDOGOyCmwHmVSV5qWKDgWMiW%2BLC2f9Buil%2BEUdE8CFnWhM8uWBZLGUiCrAJA14hTjVt4BiEyiPrtrMZu0o6%2FXsBu%2Fi6y4xPR%2BvJ3KWU8gQe82dIQbowLYVBuebUMc79Iavr7XlhQEFf%2F7WQcWgdmo2pnF4tu0CieUS7Jb0FfCwV%2F8UyrqFXzmCzCdI2P5FlMIMJ4zQp%2BTBYsoTVK6tg12stpJQGa2u3%2BzZy1r0KNzxcGLHL%2BwWRTx%2FCU%2Fg%3D%3D&notify_url=http%3A%2F%2F114.55.81.185%2Fopendevtools%2Fnotify%2Fdo%2Fbf70dcb4-13c9-4458-a547-3a5a1e8ead04&version=1.0&app_id=2014100900013222&sign_type=RSA&timestamp=2021-02-02+14%3A11%3A40&alipay_sdk=alipay-sdk-java-dynamicVersionNo&format=json">
     *                              <input type="submit" value="提交" style="display:none" >
     *                          </form>
     *                          <script>document.forms[0].submit();</script>
     *          }
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 404 Not Found 没有该课程
     * @apiError 503 Service Unavailable 服务器繁忙，请稍后重试
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param courseId 课程ID
     * @return 创建的订单信息，其中包括了支付的部分HTML，可嵌入页面
     */
    @PostMapping("/order/{courseId}")
    public Response<OrderWithComponentHtml> placeOrder(@PathVariable("courseId") Integer courseId, HttpServletResponse response) {
        response.setStatus(HttpStatus.CREATED.value());
        return Response.success(orderService.placeOrder(courseId, UserContext.getUser().getId()));
    }

    /**
     * @api {patch} /api/v1/order/{orderId} 取消订单
     * @apiName closeOrder
     * @apiGroup Order Management
     * @apiDescription 用于交易创建后，在一定时间内未进行支付，可将未付款的交易进行关闭
     *
     * @apiHeader {String} Accept application/json
     * @apiHeader {String} Content-Type application/json
     *
     * @apiParam {String} orderId 订单Id
     *
     * @apiParamExample Request-Example:
     *          PUT /api/v1/order/1234
     *
     * @apiSuccess {Order} order 取消的订单信息
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *          {
     *             "id": 12345,
     *             "status": "CLOSE",
     *             "createdOn": "2022-06-05T01:55:50.136276Z",
     *             "userId": 1,
     *             "course": {
     *                 "id": 1,
     *                 "createdOn": "2022-06-03T14:20:45.760671Z",
     *                 "name": "21天精通C++",
     *                 "teacherName": "Torvalds Linus",
     *                 "teacherDescription": "Creator of Linux",
     *                 "price": 9900,  // 单位 分
     *             },
     *             "price": 9900,  // 单位 分
     *             "tradeNo": "course_1_20220605095549"
     *          }
     * @apiError 400 Bad Request 若请求中包含错误
     * @apiError 401 Unauthorized 若未登录
     * @apiError 403 Forbidden 取消非自己的订单
     * @apiError 410 Gone 该订单不能取消。所请求的资源不再可用
     * @apiError 503 Service Unavailable 服务器繁忙，请稍后重试
     *
     * @apiErrorExample Error-Response:
     *     HTTP/1.1 400 Bad Request
     *     {
     *       "message": "Bad Request"
     *     }
     */
    /**
     * @param orderId 订单ID
     * @return 取消的订单信息
     */
    @PatchMapping("/order/{orderId}")
    public Response<Order> closeOrder(@PathVariable("orderId") Integer orderId) {
        return Response.success(orderService.closeOrder(orderId));
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
     *             "status": "UNPAID",
     *             "createdOn": "2022-06-05T01:55:50.136276Z",
     *             "userId": 1,
     *             "course": {
     *                 "id": 1,
     *                 "createdOn": "2022-06-03T14:20:45.760671Z",
     *                 "name": "21天精通C++",
     *                 "teacherName": "Torvalds Linus",
     *                 "teacherDescription": "Creator of Linux",
     *                 "price": 9900,  // 单位 分
     *             },
     *             "price": 9900,  // 单位 分
     *             "tradeNo": "course_1_20220605095549"
     *          }
     * @apiError 400 Bad request 若请求中包含错误
     * @apiError 403 Forbidden 获取非自己的订单
     * @apiError 404 Not Found 没有该订单
     * @apiError 503 Service Unavailable 服务器繁忙，请稍后重试
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
        return Response.success(orderService.getOrderById(orderId));
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
     * @apiError 403 Forbidden 删除非自己的订单
     * @apiError 404 Not Found 没有该订单
     * @apiError 503 Service Unavailable 服务器繁忙，请稍后重试
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
        response.setStatus(HttpStatus.NO_CONTENT.value());
        orderService.deleteOrderById(orderId);
    }

    /**
     * @api {get} /api/v1/order 获取订单列表
     * @apiName getOrderList
     * @apiGroup Order Management
     * @apiDescription
     *  获取分页的订单信息。订单信息里包含课程信息和价格。
     *
     * @apiHeader {String} Accept application/json
     *
     * @apiParam {String} [search] 搜索关键字 - 搜索课程名称
     * @apiParam {Number} [pageSize] 每页包含多少个订单
     * @apiParam {Number} [pageNum] 页码，从1开始
     * @apiParam {String} [orderBy] 排序字段，如price/createdAt
     * @apiParam {String} [orderType] 排序方法，ASC/DESC
     *
     * @apiParamExample Request-Example:
     *            GET /api/v1/order?pageSize=10&pageNum=1&orderBy=price&orderType=Desc&search=21天
     * @apiSuccess {Number} total 总数
     * @apiSuccess {Number} pageNum 当前页码，从1开始
     * @apiSuccess {Number} pageSize 每页包含多少个订单
     * @apiSuccess {Order[]} data 订单列表
     *
     * @apiSuccessExample Success-Response:
     *     HTTP/1.1 200 OK
     *     {
     *       "total": 100,
     *       "pageSize": 10,
     *       "pageNum": 1,
     *       "data": [
     *          {
     *             "id": 12345,
     *             "status": "UNPAID",
     *             "createdOn": "2022-06-05T01:55:50.136276Z",
     *             "userId": 1,
     *             "course": {
     *                 "id": 1,
     *                 "createdOn": "2022-06-03T14:20:45.760671Z",
     *                 "name": "21天精通C++",
     *                 "teacherName": "Torvalds Linus",
     *                 "teacherDescription": "Creator of Linux",
     *                 "price": 9900,  // 单位 分
     *             },
     *             "price": 9900,  // 单位 分
     *             "tradeNo": "course_1_20220605095549"
     *          }
     *       ]
     *     }
     * @apiError 400 Bad request 若请求中包含错误
     * @apiError 503 Service Unavailable 服务器繁忙，请稍后重试
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
    public PageResponse<Order> getOrderList(
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "orderBy", required = false) Direction orderBy,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "search", required = false) String search) {
        if (pageNum == null) pageNum = DEFAULT_PAGE_NUM;
        if (pageSize == null) pageSize = DEFAULT_PAGE_SIZE;
        if (orderBy == null) orderBy = DEFAULT_ORDER_BY;
        if (orderType == null) orderType = DEFAULT_ORDER_TYPE;
        return orderService.getOrderList(pageNum, pageSize, orderType, orderBy, search);
    }

    /**
     * 支付宝 notify url
     */
    @GetMapping("/order/status")
    public void getOrderStatus(@RequestParam("out_trade_no") String tradeNo) {
        orderService.getOrderStatusFromAlipay(tradeNo);
    }
}
