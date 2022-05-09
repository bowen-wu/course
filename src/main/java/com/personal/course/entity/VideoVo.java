package com.personal.course.entity;

import java.time.Instant;

public class VideoVo {
    private Integer id;
    private String name;
    private String description;
    private String url;
    private Instant createdOn = Instant.now();
    private Instant updatedOn = Instant.now();
    private Status status = Status.OK;

    public VideoVo() {
    }

    public VideoVo(Video video, String url) {
        this.id = video.getId();
        this.name = video.getName();
        this.description = video.getDescription();
        this.createdOn = video.getCreatedOn();
        this.updatedOn = video.getUpdatedOn();
        this.status = video.getStatus();
        this.url = url;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Instant getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
