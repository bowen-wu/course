package com.personal.course.entity.DO;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CUSTOM_CONFIG")
public class CustomConfig extends BaseEntity {
    private String name;
    private String value;

    public CustomConfig() {
    }

    public CustomConfig(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
