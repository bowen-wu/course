package com.personal.course.entity;

public enum PayStatus {
    WAIT_BUYER_PAY, // 交易创建，等待买家付款 -> UNPAID
    TRADE_CLOSED,  // 未付款交易超时关闭，或支付完成后全额退款 -> CLOSED
    TRADE_SUCCESS, // 交易支付成功 -> PAID
    TRADE_FINISHED // 交易结束，不可退款 -> PAID
}
