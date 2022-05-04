package com.personal.course.dao;

import com.personal.course.entity.Video;
import org.springframework.data.repository.CrudRepository;

public interface VideoDao extends CrudRepository<Video, Integer> {
}
