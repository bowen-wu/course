package com.personal.course.service;

import com.personal.course.common.utils.Schedule;
import com.personal.course.configuration.UserContext;
import com.personal.course.dao.CustomConfigDao;
import com.personal.course.dao.OrderDao;
import com.personal.course.entity.DO.CustomConfig;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.DO.OrderCourse;
import com.personal.course.entity.DTO.PaymentTradeQueryResponse;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.OrderWithComponentHtml;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final PaymentService paymentService;
    private final CourseService courseService;
    private final OrderDao orderDao;
    private final CustomConfigDao customConfigDao;
    private final Schedule schedule;

    public OrderService(PaymentService paymentService, CourseService courseService, OrderDao orderDao, CustomConfigDao customConfigDao) {
        this.paymentService = paymentService;
        this.courseService = courseService;
        this.orderDao = orderDao;
        this.customConfigDao = customConfigDao;
        this.schedule = new Schedule();
    }

    public OrderWithComponentHtml placeOrder(Integer courseId, Integer userId) {
        OrderCourse course = courseService.getOrderCourseById(courseId);
        Order orderInDb = orderDao.findByCourseIdAndUserId(courseId, userId);

        if (orderInDb != null && orderInDb.getStatus() == Status.PAID) {
            // 已经购买此课程
            throw HttpException.of(HttpStatus.CONFLICT, "您已经购买此课程！");
        }

        String tradeNo = orderInDb == null ? "course_" + courseId + "_" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Shanghai")).format(Instant.now()) : orderInDb.getTradeNo();
        String payTradeNo = orderInDb == null ? null : orderInDb.getPayTradeNo();

        CustomConfig paymentReturnUrlConfig = customConfigDao.findByName("paymentReturnUrl").orElseThrow(() -> {
            throw new RuntimeException("在数据库 CUSTOM_CONFIG 表中没有 paymentReturnUrl 配置");
        });

        CustomConfig paymentNotifyUrlConfig = customConfigDao.findByName("paymentNotifyUrl").orElseThrow(() -> {
            throw new RuntimeException("在数据库 CUSTOM_CONFIG 表中没有 paymentNotifyUrl 配置");
        });

        CustomConfig paymentTimeExpireConfig = customConfigDao.findByName("paymentTimeExpire").orElseThrow(() -> {
            throw new RuntimeException("在数据库 CUSTOM_CONFIG 表中没有 paymentNotifyUrl 配置");
        });

        String expireTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai")).format(Instant.now().plus(Duration.ofMinutes(Integer.parseInt(paymentTimeExpireConfig.getValue()))));
        TradePayResponse tradePayResponse = paymentService.tradePayInWebPage(tradeNo, payTradeNo, course.getPrice(), course.getName(), paymentReturnUrlConfig.getValue(), paymentNotifyUrlConfig.getValue(), expireTime);

        if (orderInDb == null) {
            Order pendCreateOrder = new Order();
            pendCreateOrder.setTradeNo(tradeNo);
            pendCreateOrder.setCourse(course);
            pendCreateOrder.setPrice(course.getPrice());
            pendCreateOrder.setUserId(UserContext.getUser().getId());
            pendCreateOrder.setStatus(Status.UNPAID);
            orderInDb = orderDao.save(pendCreateOrder);
        }

        Order finalOrderInDb = orderInDb;
        schedule.timer(() -> {
            // 多余订单过期10s去查询 => 例： 订单过期时间为15min，15min10s后去查询订单状态
            logger.info("Auto get order status. tradeNo -> " + finalOrderInDb.getTradeNo());
            queryOrderPayStatus(orderDao.findById(finalOrderInDb.getId()).orElseThrow(() -> HttpException.notFound("请检查订单ID是否正确！")));
        }, (Long.parseLong(paymentTimeExpireConfig.getValue()) * 60) + 10, TimeUnit.SECONDS);

        return OrderWithComponentHtml.of(orderInDb, tradePayResponse.getFromComponentHtml());
    }

    public Order getOrderById(Integer orderId) {
        Order orderInDb = orderDao.findById(orderId).orElseThrow(() -> HttpException.notFound("请检查订单ID是否正确！"));
        checkIsOwnerOrder(orderInDb);
        if (Status.DELETED.equals(orderInDb.getStatus())) {
            throw HttpException.notFound("请检查订单ID是否正确！");
        }

        // CLOSED 状态的不用去支付宝查询状态
        if (Status.CLOSED.equals(orderInDb.getStatus())) {
            return orderInDb;
        }
        return queryOrderPayStatus(orderInDb);
    }

    public Order closeOrder(Integer orderId) {
        Order orderInDb = getOrderById(orderId);

        // 通过是否有 payTradeNo 判断该笔订单是否在支付宝内生成订单
        if (Status.UNPAID.equals(orderInDb.getStatus())) {
            // 未支付状态下可以关闭订单
            if (orderInDb.getPayTradeNo() == null) {
                // 支付宝没有生成订单
                orderInDb.setStatus(Status.CLOSED);
                orderInDb.setUpdatedOn(Instant.now());
                return orderDao.save(orderInDb);
            }
            paymentService.closeOrder(orderInDb.getPayTradeNo(), orderInDb.getTradeNo());
            return queryOrderPayStatus(orderInDb);
        }
        throw HttpException.of(HttpStatus.GONE, "该订单不是未支付状态！");
    }

    public void deleteOrderById(Integer orderId) {
        Order orderInDb = getOrderById(orderId);
        if (Status.DELETED.equals(orderInDb.getStatus())) {
            throw HttpException.notFound("请检查订单ID是否正确！");
        }
        orderInDb.setStatus(Status.DELETED);
        orderInDb.setUpdatedOn(Instant.now());
        orderDao.save(orderInDb);
    }

    public PageResponse<Order> getOrderList(Integer pageNum, Integer pageSize, String orderType, Direction orderBy, String search) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, orderBy, orderType);
        // TODO: 有 userId 结果为空
        Page<Order> orderPage;
        Order probe = new Order();
        probe.setCreatedOn(null);
        probe.setUpdatedOn(null);
        probe.setUserId(UserContext.getUser().getId());
        Example<Order> orderExample = Example.of(probe);
        if (search != null) {
            OrderCourse exampleCourse = new OrderCourse();
            exampleCourse.setName(search);
            probe.setCourse(exampleCourse);
        }
        orderPage = orderDao.findAll(pageRequest);

        // CLOSE & DELETE 状态的不用去支付宝查询状态
        List<Order> orderList = orderPage.getContent().stream().filter(order -> order.getStatus() != Status.CLOSED || order.getStatus() != Status.DELETED).map(this::queryOrderPayStatus).collect(Collectors.toList());
        return PageResponse.of(pageNum, pageSize, (int) orderPage.getTotalElements(), "OK", orderList);
    }

    public void getOrderStatusFromAlipay(String tradeNo) {
        logger.info("notify url tradeNo -> " + tradeNo);
        orderDao.findByTradeNo(tradeNo).ifPresent(this::queryOrderPayStatus);
    }

    private Order queryOrderPayStatus(Order queryOrder) {
        PaymentTradeQueryResponse paymentTradeQueryResponse = paymentService.getTradeStatusFromPayTradeNo(queryOrder.getPayTradeNo(), queryOrder.getTradeNo(), queryOrder.getStatus());
        Status tradeStatus = paymentTradeQueryResponse.getTradeStatus();
        String payTradeNo = paymentTradeQueryResponse.getPayTradeNo();

        // 1. 更新状态 => 现有状态和支付宝状态不同
        // 2. 更新 payTradeNo
        //      a. 数据库是 null，支付宝不是 null
        if (!queryOrder.getStatus().equals(tradeStatus) || (queryOrder.getPayTradeNo() == null && payTradeNo != null)) {
            queryOrder.setStatus(tradeStatus);
            queryOrder.setPayTradeNo(payTradeNo);
            queryOrder.setUpdatedOn(Instant.now());
            return orderDao.save(queryOrder);
        }
        return queryOrder;
    }

    private void checkIsOwnerOrder(Order orderInDb) {
        if (!UserContext.getUser().getId().equals(orderInDb.getUserId())) {
            throw HttpException.forbidden();
        }
    }

}
