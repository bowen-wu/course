package com.personal.course.dao;

import com.personal.course.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User, Integer> {
    User findByUsername(String username);
}
