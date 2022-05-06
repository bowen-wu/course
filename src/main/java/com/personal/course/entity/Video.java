package com.personal.course.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VIDEO")
public class Video extends BaseEntity {
    private String name;
    private String description;
    private String url;

    public Video() {
    }

    public Video(Video video) {
        this.name = video.getName();
        this.url = video.getUrl();
        this.description = video.getDescription();
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
