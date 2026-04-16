package com.rideflow.modules.driver.controller;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.domain.DriverStatus;
import com.rideflow.modules.driver.dto.DriverResponse;
import com.rideflow.modules.driver.mapper.DriverMapper;
import com.rideflow.modules.driver.usecase.query.FindAllDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindAvailableDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindDriverByIdUseCase;
import com.rideflow.shared.exception.DriverNotFoundException;
import com.rideflow.shared.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {DriverController.class, GlobalExceptionHandler.class})
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindAllDriversUseCase findAllDriversUseCase;

    @MockBean
    private FindAvailableDriversUseCase findAvailableDriversUseCase;

    @MockBean
    private FindDriverByIdUseCase findDriverByIdUseCase;

    @MockBean
    private DriverMapper driverMapper;

    private final UUID driverId = UUID.randomUUID();

    private Driver buildDriver(DriverStatus status) {
        return Driver.builder()
                .id(driverId)
                .name("Carlos Silva")
                .vehiclePlate("ABC-1234")
                .status(status)
                .build();
    }

    private DriverResponse buildResponse(DriverStatus status) {
        return new DriverResponse(driverId, "Carlos Silva", "ABC-1234", status);
    }

    @Test
    void findAll_shouldReturn200WithDriverList() throws Exception {
        Driver driver = buildDriver(DriverStatus.AVAILABLE);
        when(findAllDriversUseCase.execute(null)).thenReturn(List.of(driver));
        when(driverMapper.toDriverResponse(driver)).thenReturn(buildResponse(DriverStatus.AVAILABLE));

        mockMvc.perform(get("/api/v1/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Carlos Silva"));
    }

    @Test
    void findAvailable_shouldReturn200WithAvailableDrivers() throws Exception {
        Driver driver = buildDriver(DriverStatus.AVAILABLE);
        when(findAvailableDriversUseCase.execute(null)).thenReturn(List.of(driver));
        when(driverMapper.toDriverResponse(driver)).thenReturn(buildResponse(DriverStatus.AVAILABLE));

        mockMvc.perform(get("/api/v1/drivers/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
    }

    @Test
    void findById_existing_shouldReturn200() throws Exception {
        Driver driver = buildDriver(DriverStatus.AVAILABLE);
        when(findDriverByIdUseCase.execute(driverId)).thenReturn(driver);
        when(driverMapper.toDriverResponse(driver)).thenReturn(buildResponse(DriverStatus.AVAILABLE));

        mockMvc.perform(get("/api/v1/drivers/{driverId}", driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(driverId.toString()));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        when(findDriverByIdUseCase.execute(driverId)).thenThrow(new DriverNotFoundException(driverId.toString()));

        mockMvc.perform(get("/api/v1/drivers/{driverId}", driverId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
