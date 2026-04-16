package com.rideflow.shared.handler;

import com.rideflow.shared.exception.RideFlowException;
import com.rideflow.shared.response.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RideFlowException.class)
    public ResponseEntity<ApiError> handleRideFlowException(RideFlowException ex) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiError.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage(), e.getRejectedValue()))
                .toList();

        var globalErrors = ex.getBindingResult().getGlobalErrors().stream()
                .map(e -> new ApiError.FieldError(e.getObjectName(), e.getDefaultMessage(), null))
                .toList();

        var allErrors = new java.util.ArrayList<>(fieldErrors);
        allErrors.addAll(globalErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.withFieldErrors("VALIDATION_ERROR", "Dados inválidos na requisição", allErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<ApiError.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(
                v.getPropertyPath().toString(),
                v.getMessage(),
                v.getInvalidValue()))
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.withFieldErrors("VALIDATION_ERROR", "Dados inválidos na requisição", fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("MALFORMED_JSON", "Corpo da requisição inválido ou malformado"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("ENDPOINT_NOT_FOUND", "Endpoint não encontrado: " + ex.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }
}
