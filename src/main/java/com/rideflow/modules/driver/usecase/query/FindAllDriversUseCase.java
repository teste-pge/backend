package com.rideflow.modules.driver.usecase.query;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.repository.DriverRepository;
import com.rideflow.shared.application.usecase.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindAllDriversUseCase implements UseCase<Void, List<Driver>> {

    private final DriverRepository driverRepository;

    @Override
    public List<Driver> execute(Void input) {
        return driverRepository.findAll();
    }
}
