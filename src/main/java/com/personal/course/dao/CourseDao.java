package com.personal.course.dao;

import com.personal.course.entity.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseDao extends CrudRepository<Course, Integer> {
}
