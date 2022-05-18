package com.personal.course.dao;

import com.personal.course.entity.DO.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDao extends JpaRepository<Course, Integer> {
}
