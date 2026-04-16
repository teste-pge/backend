package com.rideflow.modules.driver.usecase;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.domain.DriverStatus;
import com.rideflow.modules.driver.repository.DriverRepository;
import com.rideflow.modules.driver.usecase.command.MarkDriverAvailableUseCase;
import com.rideflow.modules.driver.usecase.command.MarkDriverBusyCommand;
import com.rideflow.modules.driver.usecase.command.MarkDriverBusyUseCase;
import com.rideflow.modules.driver.usecase.query.FindAllDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindAvailableDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindDriverByIdUseCase;
import com.rideflow.shared.exception.DriverNotAvailableException;
import com.rideflow.shared.exception.DriverNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverUseCaseTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private FindAllDriversUseCase findAllDriversUseCase;

    @InjectMocks
    private FindAvailableDriversUseCase findAvailableDriversUseCase;

    @InjectMocks
    private FindDriverByIdUseCase findDriverByIdUseCase;

    @InjectMocks
    private MarkDriverBusyUseCase markDriverBusyUseCase;

    @InjectMocks
    private MarkDriverAvailableUseCase markDriverAvailableUseCase;

    private static final UUID DRIVER_ID = UUID.fromString("d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1");

    private Driver availableDriver() {
        return Driver.builder().id(DRIVER_ID).name("Carlos Silva")
                .vehiclePlate("ABC-1234").status(DriverStatus.AVAILABLE).build();
    }

    private Driver busyDriver() {
        return Driver.builder().id(DRIVER_ID).name("Carlos Silva")
                .vehiclePlate("ABC-1234").status(DriverStatus.BUSY).build();
    }

    @Test
    @DisplayName("Deve retornar todos os motoristas")
    void findAll_shouldReturnAllDrivers() {
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver(), busyDriver()));
        List<Driver> drivers = findAllDriversUseCase.execute(null);
        assertThat(drivers).hasSize(2);
    }

    @Test
    @DisplayName("Deve retornar apenas motoristas disponíveis")
    void findAvailable_shouldReturnOnlyAvailable() {
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE))
                .thenReturn(List.of(availableDriver()));
        List<Driver> drivers = findAvailableDriversUseCase.execute(null);
        assertThat(drivers).hasSize(1);
        assertThat(drivers.get(0).isAvailable()).isTrue();
    }

    @Test
    @DisplayName("Deve encontrar motorista por ID")
    void findById_existing_shouldReturn() {
        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(availableDriver()));
        Driver driver = findDriverByIdUseCase.execute(DRIVER_ID);
        assertThat(driver.getId()).isEqualTo(DRIVER_ID);
        assertThat(driver.getName()).isEqualTo("Carlos Silva");
    }

    @Test
    @DisplayName("Deve lançar DriverNotFoundException para ID inexistente")
    void findById_notFound_shouldThrow() {
        UUID unknownId = UUID.randomUUID();
        when(driverRepository.findById(unknownId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> findDriverByIdUseCase.execute(unknownId))
                .isInstanceOf(DriverNotFoundException.class);
    }

    @Test
    @DisplayName("Deve marcar motorista disponível como ocupado")
    void markBusy_shouldUpdateStatus() {
        Driver driver = availableDriver();
        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(driver));
        markDriverBusyUseCase.execute(new MarkDriverBusyCommand(DRIVER_ID));
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao marcar motorista já ocupado como busy")
    void markBusy_alreadyBusy_shouldThrow() {
        when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(busyDriver()));
        assertThatThrownBy(() -> markDriverBusyUseCase.execute(new MarkDriverBusyCommand(DRIVER_ID)))
                .isInstanceOf(DriverNotAvailableException.class);
    }
}
