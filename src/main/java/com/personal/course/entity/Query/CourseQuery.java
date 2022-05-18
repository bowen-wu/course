package com.personal.course.entity.Query;

import java.util.List;

public class CourseQuery {
    private String name;
    private String description;
    private String teacherName;
    private String teacherDescription;
    private Integer price;
    private List<Integer> videoIdList;

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

    public List<Integer> getVideoIdList() {
        return videoIdList;
    }

    public void setVideoIdList(List<Integer> videoIdList) {
        this.videoIdList = videoIdList;
    }
}
