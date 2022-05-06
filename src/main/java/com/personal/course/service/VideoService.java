package com.personal.course.service;

import com.personal.course.dao.VideoDao;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Video;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;

@Service
public class VideoService {
    private final VideoDao videoDao;

    @Inject
    public VideoService(VideoDao videoDao) {
        this.videoDao = videoDao;
    }

    public Video createVideo(Video video) {
        return videoDao.save(video);
    }

    public void deleteVideo(Integer videoId) {
        try {
            videoDao.deleteById(videoId);
        } catch (EmptyResultDataAccessException e) {
            throw HttpException.notFound("该视频Id：" + videoId + "不合法!");
        }
    }

    public Video updateVideo(Integer videoId, Video video) {
        Video videoInDb = getVideoById(videoId);
        videoInDb.setName(video.getName());
        videoInDb.setUrl(video.getUrl());
        videoInDb.setDescription(video.getDescription());
        videoInDb.setUpdatedOn(Instant.now());
        return videoDao.save(videoInDb);
    }

    public Video getVideoById(Integer videoId) {
        return videoDao.findById(videoId).orElseThrow(() -> HttpException.notFound("该视频Id：" + videoId + "不合法!"));
    }
}
