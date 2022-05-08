package com.personal.course.entity;

public class OSSecret {
    private String secretId;
    private String secretKey;
    private String sessionToken;

    public static OSSecret of(String secretId, String secretKey, String sessionToken) {
        return new OSSecret(secretId, secretKey, sessionToken);
    }

    private OSSecret(String secretId, String secretKey, String sessionToken) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.sessionToken = sessionToken;
    }

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
