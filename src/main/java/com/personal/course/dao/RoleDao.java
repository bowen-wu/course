package com.personal.course.dao;

import com.personal.course.entity.DO.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleDao extends CrudRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
