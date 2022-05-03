package com.personal.course.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ROLE", schema = "public")
public class Role extends BaseEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
