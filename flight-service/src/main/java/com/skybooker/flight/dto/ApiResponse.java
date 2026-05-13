package com.skybooker.flight.dto;

public record ApiResponse(String message, Object data) {

    public ApiResponse(String message) {
        this(message, null);
    }
}
