package com.rideflow.shared.exception;

import org.springframework.http.HttpStatus;

public class RideAlreadyAcceptedException extends RideFlowException {

    public RideAlreadyAcceptedException(String rideId) {
        super("Corrida já foi aceita por outro motorista: " + rideId, HttpStatus.CONFLICT, "RIDE_ALREADY_ACCEPTED");
    }
}
