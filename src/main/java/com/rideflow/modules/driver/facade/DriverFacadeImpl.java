package com.rideflow.modules.driver.facade;

import com.rideflow.modules.driver.dto.DriverResponse;
import com.rideflow.modules.driver.mapper.DriverMapper;
import com.rideflow.modules.driver.usecase.command.MarkDriverAvailableCommand;
import com.rideflow.modules.driver.usecase.command.MarkDriverAvailableUseCase;
import com.rideflow.modules.driver.usecase.command.MarkDriverBusyCommand;
import com.rideflow.modules.driver.usecase.command.MarkDriverBusyUseCase;
import com.rideflow.modules.driver.usecase.query.FindAllDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindAvailableDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindDriverByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DriverFacadeImpl implements DriverFacade {

    private final FindAllDriversUseCase findAllDriversUseCase;
    private final FindAvailableDriversUseCase findAvailableDriversUseCase;
    private final FindDriverByIdUseCase findDriverByIdUseCase;
    private final MarkDriverBusyUseCase markDriverBusyUseCase;
    private final MarkDriverAvailableUseCase markDriverAvailableUseCase;
    private final DriverMapper driverMapper;

    @Override
    public List<DriverResponse> findAll() {
        return findAllDriversUseCase.execute(null).stream()
                .map(driverMapper::toDriverResponse)
                .toList();
    }

    @Override
    public List<DriverResponse> findAvailable() {
        return findAvailableDriversUseCase.execute(null).stream()
                .map(driverMapper::toDriverResponse)
                .toList();
    }

    @Override
    public DriverResponse findById(UUID driverId) {
        return driverMapper.toDriverResponse(findDriverByIdUseCase.execute(driverId));
    }

    @Override
    public void markBusy(UUID driverId) {
        markDriverBusyUseCase.execute(new MarkDriverBusyCommand(driverId));
    }

    @Override
    public void markAvailable(UUID driverId) {
        markDriverAvailableUseCase.execute(new MarkDriverAvailableCommand(driverId));
    }
}
