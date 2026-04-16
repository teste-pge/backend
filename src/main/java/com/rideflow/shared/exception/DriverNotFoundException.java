package com.rideflow.shared.exception;

import org.springframework.http.HttpStatus;

public class DriverNotFoundException extends RideFlowException {

    public DriverNotFoundException(String driverId) {
        super("Motorista não encontrado: " + driverId, HttpStatus.NOT_FOUND, "DRIVER_NOT_FOUND");
    }
}
