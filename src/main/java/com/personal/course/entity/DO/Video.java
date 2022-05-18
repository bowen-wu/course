package com.personal.course.entity.DO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.course.common.utils.GetKeyFromUrlUtil;
import com.personal.course.entity.Query.VideoQuery;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VIDEO")
public class Video extends BaseEntity {
    private String name;
    private String description;

    @JsonIgnore
    private String key;

    public Video() {
    }

    public Video(VideoQuery videoQuery) {
        this.name = videoQuery.getName();
        this.key = GetKeyFromUrlUtil.getKeyFromUrl(videoQuery.getUrl());
        this.description = videoQuery.getDescription();
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
