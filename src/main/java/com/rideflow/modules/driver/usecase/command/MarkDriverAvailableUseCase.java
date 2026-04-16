package com.rideflow.modules.driver.usecase.command;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.repository.DriverRepository;
import com.rideflow.shared.application.usecase.VoidUseCase;
import com.rideflow.shared.exception.DriverNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MarkDriverAvailableUseCase implements VoidUseCase<MarkDriverAvailableCommand> {

    private final DriverRepository driverRepository;

    @Override
    @Transactional
    public void execute(MarkDriverAvailableCommand input) {
        final Driver driver = driverRepository.findById(
                input.driverId()
        ).orElseThrow(() -> new DriverNotFoundException(input.driverId().toString()));

        driver.markAvailable();
        driverRepository.save(driver);
    }
}
