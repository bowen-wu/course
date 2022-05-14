package com.personal.course.entity;

public class TradePayResponse {
    private String fromComponentHtml;
    private String payTradeNo;

    public static TradePayResponse of(String fromComponentHtml, String payTradeNo) {
        return new TradePayResponse(fromComponentHtml, payTradeNo);
    }

    private TradePayResponse(String fromComponentHtml, String payTradeNo) {
        this.fromComponentHtml = fromComponentHtml;
        this.payTradeNo = payTradeNo;
    }

    public String getFromComponentHtml() {
        return fromComponentHtml;
    }

    public void setFromComponentHtml(String fromComponentHtml) {
        this.fromComponentHtml = fromComponentHtml;
    }

    public String getPayTradeNo() {
        return payTradeNo;
    }

    public void setPayTradeNo(String payTradeNo) {
        this.payTradeNo = payTradeNo;
    }
}
