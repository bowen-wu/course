package com.personal.course.entity;

import com.personal.course.common.utils.GetKeyFromUrlUtil;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VIDEO")
public class Video extends BaseEntity {
    private String name;
    private String description;
    private String key;

    public Video() {
    }

    public Video(VideoVo videoVo) {
        this.name = videoVo.getName();
        this.key = GetKeyFromUrlUtil.getKeyFromUrl(videoVo.getUrl());
        this.description = videoVo.getDescription();
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
