package com.personal.course.dao;

import com.personal.course.entity.DO.OrderCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCourseDao extends JpaRepository<OrderCourse, Integer> {
}
