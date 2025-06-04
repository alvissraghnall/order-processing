package com.alviss.order_processing.order.dto;

import java.time.Instant;
import java.util.Map;

public class ErrorDto {
    private String code;
    private String message;
    private Map<String, String> details;
    private Instant timestamp;

    public ErrorDto() {}

    public ErrorDto(String code, String message, Map<String, String> details, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ErrorDto{" +
               "code='" + code + '\'' +
               ", message='" + message + '\'' +
               ", details=" + details +
               ", timestamp=" + timestamp +
               '}';
    }
}
