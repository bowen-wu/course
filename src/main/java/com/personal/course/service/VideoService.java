package com.personal.course.service;

import com.personal.course.dao.VideoDao;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Video;
import com.personal.course.entity.VideoVo;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.util.regex.Matcher;

import static com.personal.course.entity.Video.KEY_PATTERN;

@Service
public class VideoService {
    private final VideoDao videoDao;
    private final OSClientService osClientService;

    @Inject
    public VideoService(VideoDao videoDao, OSClientService osClientService) {
        this.videoDao = videoDao;
        this.osClientService = osClientService;
    }

    public VideoVo createVideo(VideoVo videoVo) {
        Video pendingCreateVideo = new Video(videoVo);
        Video createdVideo = videoDao.save(pendingCreateVideo);
        videoVo.setId(createdVideo.getId());
        return videoVo;
    }

    public void deleteVideo(Integer videoId) {
        Video videoInDb = getVideoById(videoId);
        if (videoInDb == null) {
            throw HttpException.notFound("该视频Id：" + videoId + "不合法!");
        }
        osClientService.deleteObject(videoInDb.getKey());
        videoDao.deleteById(videoId);
    }

    public VideoVo updateVideo(Integer videoId, VideoVo videoVo) {
        Video videoInDb = getVideoById(videoId);
        videoInDb.setName(videoVo.getName());
        String key = "";
        Matcher matcher = KEY_PATTERN.matcher(videoVo.getUrl());
        if (matcher.find()) {
            key = matcher.group(0);
        }
        if (!key.equals(videoInDb.getKey())) {
            osClientService.deleteObject(videoInDb.getKey());
        }
        videoInDb.setKey(key);
        videoInDb.setDescription(videoVo.getDescription());
        videoInDb.setUpdatedOn(Instant.now());
        Video updatedVideo = videoDao.save(videoInDb);
        return convertToVideoVoFromVideo(updatedVideo);
    }

    private VideoVo convertToVideoVoFromVideo(Video video) {
        String videoUrl = osClientService.generateSignUrl(video.getKey());
        return new VideoVo(video, videoUrl);
    }

    private Video getVideoById(Integer videoId) {
        return videoDao.findById(videoId).orElseThrow(() -> HttpException.notFound("该视频Id：" + videoId + "不合法!"));
    }

    public VideoVo getVideoVoById(Integer videoId) {
        Video video = videoDao.findById(videoId).orElseThrow(() -> HttpException.notFound("该视频Id：" + videoId + "不合法!"));
        return convertToVideoVoFromVideo(video);
    }
}
