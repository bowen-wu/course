package com.personal.course.service;

import com.personal.course.common.utils.GetKeyFromUrlUtil;
import com.personal.course.dao.VideoDao;
import com.personal.course.entity.DO.Video;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Query.VideoQuery;
import com.personal.course.entity.VO.VideoVO;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;

@Service
public class VideoService {
    private final VideoDao videoDao;
    private final OSClientService osClientService;

    @Inject
    public VideoService(VideoDao videoDao, OSClientService osClientService) {
        this.videoDao = videoDao;
        this.osClientService = osClientService;
    }

    public VideoVO createVideo(VideoQuery videoQuery) {
        Video pendingCreateVideo = new Video(videoQuery);
        Video createdVideo = videoDao.save(pendingCreateVideo);
        return videoInDbConvertToVideoVO(createdVideo);
    }

    public void deleteVideo(Integer videoId) {
        Video videoInDb = getVideoById(videoId);
        if (videoInDb == null) {
            throw HttpException.notFound("该视频Id：" + videoId + "不合法!");
        }
        osClientService.deleteObject(videoInDb.getKey());
        videoDao.deleteById(videoId);
    }

    public VideoVO updateVideo(Integer videoId, VideoQuery videoQuery) {
        Video videoInDb = getVideoById(videoId);
        videoInDb.setName(videoQuery.getName());
        String key = GetKeyFromUrlUtil.getKeyFromUrl(videoQuery.getUrl());
        if (!key.equals(videoInDb.getKey())) {
            osClientService.deleteObject(videoInDb.getKey());
        }
        videoInDb.setKey(key);
        videoInDb.setDescription(videoQuery.getDescription());
        videoInDb.setUpdatedOn(Instant.now());
        Video updatedVideo = videoDao.save(videoInDb);
        return videoInDbConvertToVideoVO(updatedVideo);
    }

    public VideoVO getVideoVoById(Integer videoId) {
        Video videoInDb = getVideoById(videoId);
        return videoInDbConvertToVideoVO(videoInDb);
    }

    public VideoVO videoInDbConvertToVideoVO(Video videoInDb) {
        VideoVO videoVO = new VideoVO(videoInDb);
        videoVO.setUrl(osClientService.generateSignUrl(videoInDb.getKey()));
        return videoVO;
    }

    private Video getVideoById(Integer videoId) {
        return videoDao.findById(videoId).orElseThrow(() -> HttpException.notFound("该视频Id：" + videoId + "不合法!"));
    }
}
