package com.personal.course.entity.DO;

import com.personal.course.entity.Query.CourseQuery;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "COURSE")
public class OrderCourse extends BaseEntity {
    private String name;
    private String description;
    private String teacherName;
    private String teacherDescription;
    private Integer price;

    public OrderCourse() {
    }

    public OrderCourse(CourseQuery course) {
        this.name = course.getName();
        this.description = course.getDescription();
        this.teacherName = course.getTeacherName();
        this.price = course.getPrice();
        this.teacherDescription = course.getTeacherDescription();
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
