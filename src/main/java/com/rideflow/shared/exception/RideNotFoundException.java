package com.rideflow.shared.exception;

import org.springframework.http.HttpStatus;

public class RideNotFoundException extends RideFlowException {

    public RideNotFoundException(String rideId) {
        super("Corrida não encontrada: " + rideId, HttpStatus.NOT_FOUND, "RIDE_NOT_FOUND");
    }
}
