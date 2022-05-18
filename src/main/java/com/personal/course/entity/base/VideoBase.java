package com.personal.course.entity.base;

import com.personal.course.entity.DO.BaseEntity;
import com.personal.course.entity.Status;

import java.time.Instant;

public class VideoBase extends BaseEntity {
    private String name;
    private String description;
    private String url;

    public VideoBase() {
    }

    public VideoBase(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
    }

    public VideoBase(Integer id, Instant createdOn, Instant updatedOn, Status status, String name, String description) {
        super(id, createdOn, updatedOn, status);
        this.name = name;
        this.description = description;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
