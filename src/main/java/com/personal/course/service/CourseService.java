package com.personal.course.service;

import com.personal.course.dao.CourseDao;
import com.personal.course.dao.OrderDao;
import com.personal.course.dao.VideoDao;
import com.personal.course.entity.DO.Course;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.DO.Video;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Query.CourseQuery;
import com.personal.course.entity.VO.CourseVO;
import com.personal.course.entity.VO.VideoVO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class CourseService {
    private final VideoDao videoDao;
    private final CourseDao courseDao;
    private final OrderDao orderDao;
    private final VideoService videoService;

    @Inject
    public CourseService(VideoDao videoDao, CourseDao courseDao, OrderDao orderDao, VideoService videoService) {
        this.videoDao = videoDao;
        this.courseDao = courseDao;
        this.orderDao = orderDao;
        this.videoService = videoService;
    }

    public CourseVO createCourse(CourseQuery course) {
        Course pendingCreateCourse = new Course(course);
        pendingCreateCourse.setVideoList(getVideos(course));
        Course courseInDb = courseDao.save(pendingCreateCourse);
        return courseInDbConvertToCourseVO(courseInDb);
    }

    private List<Video> getVideos(CourseQuery course) {
        List<Video> videoList = new ArrayList<>();
        if (course.getVideoIdList() != null) {
            videoList = course.getVideoIdList().stream().map(videoDao::findById).filter(Optional::isPresent).map(Optional::get).collect(toList());
        }
        return videoList;
    }

    public Course getCourseById(Integer courseId) {
        return courseDao.findById(courseId).orElseThrow(() -> HttpException.notFound("没有此课程" + courseId));
    }

    public CourseVO getCourse(Integer courseId, Integer userId) {
        Course courseInDb = getCourseById(courseId);
        Order order = orderDao.findByCourseIdAndUserId(courseId, userId);
        CourseVO courseVo = new CourseVO(courseInDb);
        if (order != null) {
            courseVo.setPurchased(true);
            List<VideoVO> videoVOList = courseInDb.getVideoList().stream().map(videoService::videoInDbConvertToVideoVO).collect(toList());
            courseVo.setVideoList(videoVOList);
        }
        return courseVo;
    }

    public CourseVO updateCourse(Integer courseId, CourseQuery course, Integer userId) {
        Course courseInDb = getCourseById(courseId);
        courseInDb.setName(course.getName());
        courseInDb.setDescription(course.getDescription());
        courseInDb.setTeacherName(course.getTeacherName());
        courseInDb.setTeacherDescription(course.getTeacherDescription());
        courseInDb.setPrice(course.getPrice());
        courseInDb.setUpdatedOn(Instant.now());
        courseInDb.setVideoList(getVideos(course));
        Course updatedCourse = courseDao.save(courseInDb);
        return courseInDbConvertToCourseVO(updatedCourse);
    }

    public void deleteCourseById(Integer courseId) {
        getCourseById(courseId);
        courseDao.deleteById(courseId);
    }

    public PageResponse<CourseVO> getCourseList(Integer pageNum, Integer pageSize, String orderType, Direction orderBy, String search) {
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize, orderBy, orderType);
        Page<Course> coursePage;
        if (search == null) {
            coursePage = courseDao.findAll(pageRequest);
        } else {
            Course course = new Course();
            course.setName(search);
            coursePage = courseDao.findAll(Example.of(course), pageRequest);
        }
        return PageResponse.of(pageNum, pageSize, coursePage.getTotalPages(), "OK", coursePage.getContent().stream().map(this::courseInDbConvertToCourseVO).collect(toList()));
    }

    private CourseVO courseInDbConvertToCourseVO(Course courseInDb) {
        CourseVO courseVo = new CourseVO(courseInDb);
        List<VideoVO> videoVOList = courseInDb.getVideoList().stream().map(videoService::videoInDbConvertToVideoVO).collect(toList());
        courseVo.setVideoList(videoVOList);
        return courseVo;
    }
}
