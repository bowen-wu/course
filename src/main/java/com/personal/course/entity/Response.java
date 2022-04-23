package com.personal.course.entity;

public class Response<T> {
    private String message;
    private T data;

    public static <R> Response<R> of(String message, R data) {
        return new Response<>(message, data);
    }

    private Response() {
    }

    private Response(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
