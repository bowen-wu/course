package com.personal.course.entity.DO;

import com.personal.course.entity.Query.CourseQuery;
import com.personal.course.entity.Status;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "COURSE")
public class Course extends BaseEntity {
    private String name;
    private String description;
    private String teacherName;
    private String teacherDescription;
    private Integer price;
    private List<Video> videoList;

    public Course() {
    }

    public Course(CourseQuery course) {
        this.name = course.getName();
        this.description = course.getDescription();
        this.teacherName = course.getTeacherName();
        this.price = course.getPrice();
        this.teacherDescription = course.getTeacherDescription();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "COURSE_VIDEO",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "video_id")
    )
    public List<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherDescription() {
        return teacherDescription;
    }

    public void setTeacherDescription(String teacherDescription) {
        this.teacherDescription = teacherDescription;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
