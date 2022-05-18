package com.personal.course.entity.VO;

import com.personal.course.entity.DO.Course;
import com.personal.course.entity.Status;
import com.personal.course.entity.base.CourseBase;

import java.time.Instant;
import java.util.List;

public class CourseVO extends CourseBase {
    private List<VideoVO> videoList;
    private boolean purchased;

    public CourseVO() {
        super();
    }

    public CourseVO(Course courseInDb) {
        super(courseInDb.getId(), courseInDb.getCreatedOn(), courseInDb.getUpdatedOn(), courseInDb.getStatus(), courseInDb.getName(), courseInDb.getDescription(), courseInDb.getTeacherName(), courseInDb.getTeacherDescription(), courseInDb.getPrice());
    }

    public CourseVO(String name, String description, String teacherName, String teacherDescription, Integer price, List<VideoVO> videoList, boolean purchased) {
        super(name, description, teacherName, teacherDescription, price);
        this.videoList = videoList;
        this.purchased = purchased;
    }

    public CourseVO(Integer id, Instant createdOn, Instant updatedOn, Status status, String name, String description, String teacherName, String teacherDescription, Integer price) {
        super(id, createdOn, updatedOn, status, name, description, teacherName, teacherDescription, price);
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public List<VideoVO> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<VideoVO> videoList) {
        this.videoList = videoList;
    }
}
