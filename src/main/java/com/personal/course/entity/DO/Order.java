package com.personal.course.entity.DO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.personal.course.entity.Status;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ORDERS")
public class Order extends BaseEntity {
    private Integer userId;
    private OrderCourse course;
    private Integer price;
    private String tradeNo;
    @JsonIgnore
    private String payTradeNo;

    public Order() {
    }

    public Order(Integer id, Instant createdOn, Instant updatedOn, Status status, Integer userId, OrderCourse course, Integer price, String tradeNo, String payTradeNo) {
        super(id, createdOn, updatedOn, status);
        this.userId = userId;
        this.course = course;
        this.price = price;
        this.tradeNo = tradeNo;
        this.payTradeNo = payTradeNo;
    }

    public Order(Integer userId, OrderCourse course, Integer price, String tradeNo) {
        this.userId = userId;
        this.course = course;
        this.price = price;
        this.tradeNo = tradeNo;
    }

    @OneToOne
    public OrderCourse getCourse() {
        return course;
    }

    public void setCourse(OrderCourse course) {
        this.course = course;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getPayTradeNo() {
        return payTradeNo;
    }

    public void setPayTradeNo(String payTradeNo) {
        this.payTradeNo = payTradeNo;
    }
}
