package com.rideflow.shared.exception;

import org.springframework.http.HttpStatus;

public abstract class RideFlowException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected RideFlowException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
