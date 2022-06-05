package com.personal.course.entity.DTO;

import com.personal.course.entity.Status;

public class PaymentTradeQueryResponse {
    private Status tradeStatus;
    private String payTradeNo;

    public PaymentTradeQueryResponse() {
    }

    public static PaymentTradeQueryResponse of(Status tradeStatus, String payTradeNo) {
        return new PaymentTradeQueryResponse(tradeStatus, payTradeNo);
    }

    public PaymentTradeQueryResponse(Status tradeStatus, String payTradeNo) {
        this.tradeStatus = tradeStatus;
        this.payTradeNo = payTradeNo;
    }

    public Status getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(Status tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public String getPayTradeNo() {
        return payTradeNo;
    }

    public void setPayTradeNo(String payTradeNo) {
        this.payTradeNo = payTradeNo;
    }
}
