package com.personal.course.entity;

import java.time.Instant;

public class OrderWithComponentHtml extends Order {
    private String formComponentHtml;

    public static OrderWithComponentHtml of(Order order, String formHtml) {
        return new OrderWithComponentHtml(order.getId(), order.getCreatedOn(), order.getUpdatedOn(), order.getStatus(), order.getUserId(), order.getCourse(), order.getPrice(), order.getTradeNo(), formHtml);
    }

    private OrderWithComponentHtml(Integer id, Instant createdOn, Instant updatedOn, Status status, Integer userId, Course course, Integer price, String tradeNo, String formComponentHtml) {
        super(id, createdOn, updatedOn, status, userId, course, price, tradeNo);
        this.formComponentHtml = formComponentHtml;
    }

    public String getFormComponentHtml() {
        return formComponentHtml;
    }

    public void setFormComponentHtml(String formComponentHtml) {
        this.formComponentHtml = formComponentHtml;
    }
}
