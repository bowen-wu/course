package com.personal.course.entity;

public class Whitelist {
    private String uri;
    private String method;

    public static Whitelist of(String uri, String method) {
        return new Whitelist(uri, method);
    }

    private Whitelist(String uri, String method) {
        this.uri = uri;
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
