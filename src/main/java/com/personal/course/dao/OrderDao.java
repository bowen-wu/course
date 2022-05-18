package com.personal.course.dao;

import com.personal.course.entity.DO.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDao extends JpaRepository<Order, Integer> {
    Order findByCourseIdAndUserId(Integer courseId, Integer userId);
}
