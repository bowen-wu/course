package com.personal.course.entity;

public class Response<T> {
    private String message;
    private T data;

    public static <R> Response<R> of(String message, R data) {
        return new Response<>(message, data);
    }

    public static <R> Response<R> success(R data) {
        return new Response<>("OK", data);
    }

    public static <R> Response<R> fail(R data) {
        return new Response<>("FAIL", data);
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
