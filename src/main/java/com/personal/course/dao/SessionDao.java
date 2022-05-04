package com.personal.course.dao;

import com.personal.course.entity.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SessionDao extends CrudRepository<Session, Integer> {
    Optional<Session> findByCookie(String cookie);

    void deleteByCookie(String cookie);

    void deleteByUserId(Integer userId);
}
