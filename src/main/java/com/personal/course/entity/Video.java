package com.personal.course.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "VIDEO")
public class Video extends BaseEntity {
    public static Pattern KEY_PATTERN = Pattern.compile("com\\/.*\\?");
    private String name;
    private String description;
    private String key;

    public Video() {
    }

    public Video(VideoVo videoVo) {
        this.name = videoVo.getName();
        String key = "";
        Matcher matcher = KEY_PATTERN.matcher(videoVo.getUrl());
        if (matcher.find()) {
            key = matcher.group(0);
        }
        this.key = key;
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
