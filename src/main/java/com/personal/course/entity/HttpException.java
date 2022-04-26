package com.personal.course.entity;

import org.springframework.http.HttpStatus;

public class HttpException extends RuntimeException {
    private HttpStatus statusCode;
    private String message;

    public static HttpException of(HttpStatus statusCode, String message) {
        return new HttpException(statusCode, message);
    }

    public static HttpException unauthorized() {
        return HttpException.unauthorized("请登录！");
    }

    public static HttpException unauthorized(String message) {
        return new HttpException(HttpStatus.UNAUTHORIZED, message);
    }

    public static HttpException badRequest(String message) {
        return new HttpException(HttpStatus.BAD_REQUEST, message);
    }

    private HttpException(HttpStatus statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
