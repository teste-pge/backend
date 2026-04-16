package com.rideflow.modules.driver.facade;

import com.rideflow.modules.driver.dto.DriverResponse;

import java.util.List;
import java.util.UUID;

public interface DriverFacade {

    List<DriverResponse> findAll();

    List<DriverResponse> findAvailable();

    DriverResponse findById(UUID driverId);

    void markBusy(UUID driverId);

    void markAvailable(UUID driverId);
}
