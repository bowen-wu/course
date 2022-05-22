package com.personal.course.dao;

import com.personal.course.entity.DO.CustomConfig;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomConfigDao extends CrudRepository<CustomConfig, Integer> {
    Optional<CustomConfig> findByName(String name);
}
