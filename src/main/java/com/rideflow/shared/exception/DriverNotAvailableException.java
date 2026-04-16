package com.rideflow.shared.exception;

import org.springframework.http.HttpStatus;

public class DriverNotAvailableException extends RideFlowException {

    public DriverNotAvailableException(String driverId) {
        super("Motorista indisponível para aceitar corridas: " + driverId, HttpStatus.UNPROCESSABLE_ENTITY, "DRIVER_NOT_AVAILABLE");
    }
}
