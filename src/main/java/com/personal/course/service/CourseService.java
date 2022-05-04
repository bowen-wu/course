package com.personal.course.service;

import com.personal.course.dao.CourseDao;
import com.personal.course.dao.VideoDao;
import com.personal.course.entity.Course;
import com.personal.course.entity.CourseVO;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.Video;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private final VideoDao videoDao;
    private final CourseDao courseDao;

    @Inject
    public CourseService(VideoDao videoDao, CourseDao courseDao) {
        this.videoDao = videoDao;
        this.courseDao = courseDao;
    }

    public Course createCourse(CourseVO course) {
        Course pendingCreateCourse = new Course(course);
        pendingCreateCourse.setVideoList(getVideos(course));
        return courseDao.save(pendingCreateCourse);
    }

    private List<Video> getVideos(CourseVO course) {
        List<Video> videoList = new ArrayList<>();
        if (course.getVideoIdList() != null) {
            videoList = course.getVideoIdList().stream().map(videoDao::findById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        }
        return videoList;
    }

    public Course getCourse(Integer courseId) {
        return courseDao.findById(courseId).orElseThrow(() -> HttpException.notFound("没有此课程" + courseId));
    }

    public Course updateCourse(Integer courseId, CourseVO course) {
        Course courseInDb = getCourse(courseId);
        courseInDb.setName(course.getName());
        courseInDb.setDescription(course.getDescription());
        courseInDb.setTeacherName(course.getTeacherName());
        courseInDb.setTeacherDescription(course.getTeacherDescription());
        courseInDb.setPrice(course.getPrice());
        courseInDb.setUpdatedOn(Instant.now());
        courseInDb.setVideoList(getVideos(course));
        return courseDao.save(courseInDb);
    }

    public void deleteCourseById(Integer courseId) {
        getCourse(courseId);
        courseDao.deleteById(courseId);
    }
}
