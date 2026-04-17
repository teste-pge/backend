package com.rideflow.modules.driver.facade;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.domain.DriverStatus;
import com.rideflow.modules.driver.dto.DriverResponse;
import com.rideflow.modules.driver.mapper.DriverMapper;
import com.rideflow.modules.driver.usecase.command.MarkDriverAvailableUseCase;
import com.rideflow.modules.driver.usecase.command.MarkDriverBusyUseCase;
import com.rideflow.modules.driver.usecase.query.FindAllDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindAvailableDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindDriverByIdUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverFacadeImplTest {

    @Mock private FindAllDriversUseCase findAllDriversUseCase;
    @Mock private FindAvailableDriversUseCase findAvailableDriversUseCase;
    @Mock private FindDriverByIdUseCase findDriverByIdUseCase;
    @Mock private MarkDriverBusyUseCase markDriverBusyUseCase;
    @Mock private MarkDriverAvailableUseCase markDriverAvailableUseCase;
    @Mock private DriverMapper driverMapper;

    @InjectMocks
    private DriverFacadeImpl driverFacade;

    @Test
    void findAll_shouldReturnMappedDrivers() {
        UUID id = UUID.randomUUID();
        Driver driver = Driver.builder().id(id).name("Carlos").vehiclePlate("ABC-1234").build();
        DriverResponse response = new DriverResponse(id, "Carlos", "ABC-1234", DriverStatus.AVAILABLE);

        when(findAllDriversUseCase.execute(null)).thenReturn(List.of(driver));
        when(driverMapper.toDriverResponse(driver)).thenReturn(response);

        List<DriverResponse> result = driverFacade.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Carlos");
    }

    @Test
    void findAvailable_shouldReturnOnlyAvailable() {
        when(findAvailableDriversUseCase.execute(null)).thenReturn(List.of());

        List<DriverResponse> result = driverFacade.findAvailable();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnMappedDriver() {
        UUID id = UUID.randomUUID();
        Driver driver = Driver.builder().id(id).name("Ana").vehiclePlate("DEF-5678").build();
        DriverResponse response = new DriverResponse(id, "Ana", "DEF-5678", DriverStatus.AVAILABLE);

        when(findDriverByIdUseCase.execute(id)).thenReturn(driver);
        when(driverMapper.toDriverResponse(driver)).thenReturn(response);

        DriverResponse result = driverFacade.findById(id);

        assertThat(result.name()).isEqualTo("Ana");
    }

    @Test
    void markBusy_shouldDelegateToUseCase() {
        UUID id = UUID.randomUUID();
        driverFacade.markBusy(id);
        verify(markDriverBusyUseCase).execute(any());
    }

    @Test
    void markAvailable_shouldDelegateToUseCase() {
        UUID id = UUID.randomUUID();
        driverFacade.markAvailable(id);
        verify(markDriverAvailableUseCase).execute(any());
    }
}
