package com.personal.course.service;

import com.personal.course.configuration.UserContext;
import com.personal.course.dao.CourseDao;
import com.personal.course.dao.OrderCourseDao;
import com.personal.course.dao.OrderDao;
import com.personal.course.dao.VideoDao;
import com.personal.course.entity.DO.Course;
import com.personal.course.entity.DO.Order;
import com.personal.course.entity.DO.OrderCourse;
import com.personal.course.entity.DO.Permission;
import com.personal.course.entity.DO.Role;
import com.personal.course.entity.DO.Video;
import com.personal.course.entity.HttpException;
import com.personal.course.entity.PageResponse;
import com.personal.course.entity.Query.CourseQuery;
import com.personal.course.entity.Status;
import com.personal.course.entity.VO.CourseVO;
import com.personal.course.entity.VO.VideoVO;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class CourseService {
    private final VideoDao videoDao;
    private final CourseDao courseDao;
    private final OrderCourseDao orderCourseDao;
    private final OrderDao orderDao;

    @Inject
    public CourseService(VideoDao videoDao, CourseDao courseDao, OrderCourseDao orderCourseDao, OrderDao orderDao) {
        this.videoDao = videoDao;
        this.courseDao = courseDao;
        this.orderCourseDao = orderCourseDao;
        this.orderDao = orderDao;
    }

    public CourseVO createCourse(CourseQuery course) {
        Course pendingCreateCourse = new Course(course);
        pendingCreateCourse.setVideoList(getVideos(course));
        Course courseInDb = courseDao.save(pendingCreateCourse);
        return new CourseVO(courseInDb);
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

    public OrderCourse getOrderCourseById(Integer courseId) {
        return orderCourseDao.findById(courseId).orElseThrow(() -> HttpException.notFound("没有此课程" + courseId));
    }

    public CourseVO getCourse(Integer courseId, Integer userId) {
        Course courseInDb = getCourseById(courseId);
        boolean canManagementCourse = UserContext.getUser().getRoles()
                .stream()
                .map(Role::getPermissionList)
                .flatMap(Collection::stream)
                .map(Permission::getName)
                .anyMatch("managementCourse"::equals);

        Order order = orderDao.findByCourseIdAndUserId(courseId, userId);
        CourseVO courseVo = new CourseVO(courseInDb);
        boolean isPaid = canManagementCourse || order != null && Status.PAID.equals(order.getStatus());
        courseVo.setPurchased(isPaid);
        courseVo.setVideoList(courseInDb.getVideoList().stream().map(VideoVO::new).collect(toList()));
        return courseVo;
    }

    public CourseVO updateCourse(Integer courseId, CourseQuery course) {
        Course courseInDb = getCourseById(courseId);
        courseInDb.setName(course.getName());
        courseInDb.setDescription(course.getDescription());
        courseInDb.setTeacherName(course.getTeacherName());
        courseInDb.setTeacherDescription(course.getTeacherDescription());
        courseInDb.setPrice(course.getPrice());
        courseInDb.setUpdatedOn(Instant.now());
        courseInDb.setVideoList(getVideos(course));
        Course updatedCourse = courseDao.save(courseInDb);
        return new CourseVO(updatedCourse);
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
            ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny().withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase()).withStringMatcher(StringMatcher.CONTAINING).withIgnoreNullValues();

            coursePage = courseDao.findAll(Example.of(course, customExampleMatcher), pageRequest);
        }
        return PageResponse.of(pageNum, pageSize, (int) coursePage.getTotalElements(), "OK", coursePage.getContent().stream().map(CourseVO::new).collect(toList()));
    }
}
