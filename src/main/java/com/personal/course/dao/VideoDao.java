package com.personal.course.dao;

import com.personal.course.entity.DO.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface VideoDao extends JpaRepository<Video, Integer> {
}
