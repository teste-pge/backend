package com.rideflow.shared.application.usecase;

public interface UseCase<I, O> {

    O execute(I input);
}
