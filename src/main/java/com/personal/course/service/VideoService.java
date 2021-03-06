package com.personal.course.service;

import com.personal.course.common.utils.GetKeyFromUrlUtil;
import com.personal.course.configuration.UserContext;
import com.personal.course.dao.OrderDao;
import com.personal.course.dao.VideoDao;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.DO.Permission;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.DO.Video;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Query.VideoQuery;
import com.personal.course.entity.Status;
import com.personal.course.entity.VO.VideoVO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collection;

@Service
public class VideoService {
    private final VideoDao videoDao;
    private final OSClientService osClientService;
    private final OrderDao orderDao;

    @Inject
    public VideoService(VideoDao videoDao, OSClientService osClientService, OrderDao orderDao) {
        this.videoDao = videoDao;
        this.osClientService = osClientService;
        this.orderDao = orderDao;
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

    private VideoVO videoInDbConvertToVideoVO(Video videoInDb) {
        VideoVO videoVO = new VideoVO(videoInDb);
        videoVO.setUrl(osClientService.generateSignUrl(videoInDb.getKey()));
        return videoVO;
    }

    private Video getVideoById(Integer videoId) {
        return videoDao.findById(videoId).orElseThrow(() -> HttpException.notFound("该视频Id：" + videoId + "不合法!"));
    }

    public PageResponse<Video> getVideoList(Integer pageNum, Integer pageSize, String orderType, Direction orderBy, String search) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, orderBy, orderType);
        Page<Video> videoPage;
        if (search == null) {
            videoPage = videoDao.findAll(pageRequest);
        } else {
            Video video = new Video();
            video.setName(search);
            videoPage = videoDao.findAll(Example.of(video), pageRequest);
        }
        return PageResponse.of(pageNum, pageSize, (int) videoPage.getTotalElements(), "OK", videoPage.getContent());
    }

    public VideoVO getVideoVoByVideoIdAndCourseId(Integer videoId, Integer courseId, Integer userId) {
        boolean canManagementCourse = UserContext.getUser().getRoles()
                .stream()
                .map(Role::getPermissionList)
                .flatMap(Collection::stream)
                .map(Permission::getName)
                .anyMatch("managementCourse"::equals);

        Order order = orderDao.findByCourseIdAndUserId(courseId, userId);
        if (canManagementCourse || (order != null && Status.PAID.equals(order.getStatus()))) {
            return getVideoVoById(videoId);
        }
        throw HttpException.forbidden();
    }
}
