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
public class MarkDriverBusyUseCase implements VoidUseCase<MarkDriverBusyCommand> {

    private final DriverRepository driverRepository;

    @Override
    @Transactional
    public void execute(MarkDriverBusyCommand input) {
        final Driver driver = driverRepository.findById(
                input.driverId()
        ).orElseThrow(() -> new DriverNotFoundException(input.driverId().toString()));

        driver.markBusy();
        driverRepository.save(driver);
    }
}
