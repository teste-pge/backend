package com.rideflow.modules.driver.controller;

import com.rideflow.modules.driver.dto.DriverResponse;
import com.rideflow.modules.driver.mapper.DriverMapper;
import com.rideflow.modules.driver.usecase.query.FindAllDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindAvailableDriversUseCase;
import com.rideflow.modules.driver.usecase.query.FindDriverByIdUseCase;
import com.rideflow.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Motoristas", description = "Consulta de motoristas e disponibilidade")
public class DriverController {

    private final FindAllDriversUseCase findAllDriversUseCase;
    private final FindAvailableDriversUseCase findAvailableDriversUseCase;
    private final FindDriverByIdUseCase findDriverByIdUseCase;
    private final DriverMapper driverMapper;

    @GetMapping
    @Operation(summary = "Listar todos os motoristas", description = "Retorna todos os motoristas cadastrados (seed via Flyway)")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> findAll() {
        List<DriverResponse> drivers = findAllDriversUseCase.execute(null
        ).stream().map(driverMapper::toDriverResponse).toList();

        return ResponseEntity.ok(ApiResponse.of(drivers));
    }

    @GetMapping("/available")
    @Operation(summary = "Listar motoristas disponíveis", description = "Retorna apenas motoristas com status AVAILABLE")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> findAvailable() {
        List<DriverResponse> drivers = findAvailableDriversUseCase.execute(null
        ).stream().map(driverMapper::toDriverResponse).toList();

        return ResponseEntity.ok(ApiResponse.of(drivers));
    }

    @GetMapping("/{driverId}")
    @Operation(summary = "Buscar motorista por ID", description = "Retorna detalhes de um motorista específico")
    public ResponseEntity<ApiResponse<DriverResponse>> findById(@PathVariable UUID driverId) {
        DriverResponse response = driverMapper.toDriverResponse(findDriverByIdUseCase.execute(driverId));

        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
