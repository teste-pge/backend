package com.rideflow.modules.driver.usecase.query;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.repository.DriverRepository;
import com.rideflow.shared.application.usecase.UseCase;
import com.rideflow.shared.exception.DriverNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindDriverByIdUseCase implements UseCase<UUID, Driver> {

    private final DriverRepository driverRepository;

    @Override
    public Driver execute(UUID driverId) {
        return driverRepository.findById(
                driverId
        ).orElseThrow(() -> new DriverNotFoundException(driverId.toString()));
    }
}
