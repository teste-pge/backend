package com.rideflow.shared.response;

import java.time.Instant;
import java.util.List;

public record ApiError(
        boolean success,
        String errorCode,
        String message,
        List<FieldError> fieldErrors,
        Instant timestamp
) {

    public record FieldError(String field, String message, Object rejectedValue) {}

    public static ApiError of(String errorCode, String message) {
        return new ApiError(false, errorCode, message, null, Instant.now());
    }

    public static ApiError withFieldErrors(String errorCode, String message, List<FieldError> fieldErrors) {
        return new ApiError(false, errorCode, message, fieldErrors, Instant.now());
    }
}
