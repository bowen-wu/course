package com.personal.course.entity.base;

import com.personal.course.entity.DO.BaseEntity;
import com.personal.course.entity.Status;

import java.time.Instant;

public class CourseBase extends BaseEntity {
    private String name;
    private String description;
    private String teacherName;
    private String teacherDescription;
    private Integer price;

    public CourseBase() {
    }

    public CourseBase(String name, String description, String teacherName, String teacherDescription, Integer price) {
        this.name = name;
        this.description = description;
        this.teacherName = teacherName;
        this.teacherDescription = teacherDescription;
        this.price = price;
    }

    public CourseBase(Integer id, Instant createdOn, Instant updatedOn, Status status, String name, String description, String teacherName, String teacherDescription, Integer price) {
        super(id, createdOn, updatedOn, status);
        this.name = name;
        this.description = description;
        this.teacherName = teacherName;
        this.teacherDescription = teacherDescription;
        this.price = price;
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
