package com.personal.course.dao;

import com.personal.course.entity.DO.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderDao extends JpaRepository<Order, Integer> {
    Order findByCourseIdAndUserId(Integer courseId, Integer userId);

    Optional<Order> findByTradeNo(String tradeNo);
}
