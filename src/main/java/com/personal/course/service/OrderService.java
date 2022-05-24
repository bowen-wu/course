package com.personal.course.service;

import com.personal.course.configuration.UserContext;
import com.personal.course.dao.OrderDao;
import com.personal.course.entity.DO.Course;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.OrderWithComponentHtml;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Status;
import com.personal.course.entity.TradePayResponse;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final PaymentService paymentService;
    private final CourseService courseService;
    private final OrderDao orderDao;

    public OrderService(PaymentService paymentService, CourseService courseService, OrderDao orderDao) {
        this.paymentService = paymentService;
        this.courseService = courseService;
        this.orderDao = orderDao;
    }

    public OrderWithComponentHtml placeOrder(Integer courseId) {
        Course course = courseService.getCourseById(courseId);
        String tradeNo = UUID.randomUUID().toString();
        // TODO: returnUrl => 前端订单详情页面
        TradePayResponse tradePayResponse = paymentService.tradePayInWebPage(tradeNo, course.getPrice(), course.getName(), "");
        Order pendCreateOrder = new Order();
        pendCreateOrder.setTradeNo(tradeNo);
        pendCreateOrder.setCourse(course);
        pendCreateOrder.setPrice(course.getPrice());
        pendCreateOrder.setUserId(UserContext.getUser().getId());
        pendCreateOrder.setStatus(Status.UNPAID);
        pendCreateOrder.setPayTradeNo(tradePayResponse.getPayTradeNo());
        Order createdOrder = orderDao.save(pendCreateOrder);
        return OrderWithComponentHtml.of(createdOrder, tradePayResponse.getFromComponentHtml());
    }

    public Order getOrderById(Integer orderId) {
        Order orderInDb = orderDao.findById(orderId).orElseThrow(() -> HttpException.notFound("请检查订单ID是否正确！"));
        checkIsOwnerOrder(orderInDb);
        if (Status.DELETED.equals(orderInDb.getStatus())) {
            throw HttpException.notFound("请检查订单ID是否正确！");
        }
        return queryOrderPayStatus(orderInDb);
    }

    private Order queryOrderPayStatus(Order queryOrder) {
        Status tradeStatus = paymentService.getTradeStatusFromPayTradeNo(queryOrder.getPayTradeNo());
        if (!queryOrder.getStatus().equals(tradeStatus)) {
            queryOrder.setStatus(tradeStatus);
            queryOrder.setUpdatedOn(Instant.now());
            orderDao.save(queryOrder);
        }
        return queryOrder;
    }

    public void deleteOrderById(Integer orderId) {
        Order orderInDb = getOrderById(orderId);
        checkIsOwnerOrder(orderInDb);
        if (Status.DELETED.equals(orderInDb.getStatus())) {
            throw HttpException.notFound("请检查订单ID是否正确！");
        }
        orderInDb.setStatus(Status.DELETED);
        orderInDb.setUpdatedOn(Instant.now());
        orderDao.save(orderInDb);
    }

    public PageResponse<Order> getOrderList(Integer pageNum, Integer pageSize, String orderType, Direction orderBy, String search) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, orderBy, orderType);
        Order probe = new Order();
        probe.setUserId(UserContext.getUser().getId());
        Example<Order> orderExample = Example.of(probe);
        if (!search.isEmpty()) {
            Course exampleCourse = new Course();
            exampleCourse.setName(search);
            probe.setCourse(exampleCourse);
        }
        Page<Order> orderPage = orderDao.findAll(orderExample, pageRequest);
        List<Order> orderList = orderPage.getContent().stream().map(this::queryOrderPayStatus).collect(Collectors.toList());
        return PageResponse.of(pageNum, pageSize, (int) orderPage.getTotalElements(), "OK", orderList);
    }

    public Order closeOrder(Integer orderId) {
        Order orderInDb = getOrderById(orderId);
        checkIsOwnerOrder(orderInDb);
        if (Status.UNPAID.equals(orderInDb.getStatus())) {
            // 未支付状态下可以关闭订单
            paymentService.closeOrder(orderInDb.getPayTradeNo());
            return queryOrderPayStatus(orderInDb);
        }
        throw HttpException.of(HttpStatus.GONE, "该订单不是未支付状态！");
    }

    private void checkIsOwnerOrder(Order orderInDb) {
        if (!UserContext.getUser().getId().equals(orderInDb.getUserId())) {
            throw HttpException.forbidden();
        }
    }
}
