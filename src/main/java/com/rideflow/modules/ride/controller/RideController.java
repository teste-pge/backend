package com.rideflow.modules.ride.controller;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.DriverActionRequest;
import com.rideflow.modules.ride.dto.RideResponse;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.repository.RideRepository;
import com.rideflow.modules.ride.usecase.command.*;
import com.rideflow.modules.ride.usecase.query.FindRideByIdUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusQuery;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserQuery;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserUseCase;
import com.rideflow.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@Tag(name = "Corridas", description = "Criação, consulta, aceitação e rejeição de corridas")
public class RideController {

    private final CreateRideUseCase createRideUseCase;
    private final AcceptRideUseCase acceptRideUseCase;
    private final RejectRideUseCase rejectRideUseCase;
    private final CompleteRideUseCase completeRideUseCase;
    private final FindRideByIdUseCase findRideByIdUseCase;
    private final FindRidesByStatusUseCase findRidesByStatusUseCase;
    private final FindRidesByUserUseCase findRidesByUserUseCase;
    private final RideRepository rideRepository;
    private final RideMapper rideMapper;

    @PostMapping
    @Operation(summary = "Criar corrida", description = "Cria uma nova corrida com status PENDING e publica evento no Kafka")
    public ResponseEntity<ApiResponse<RideResponse>> create(@Valid @RequestBody CreateRideRequest request) {
        final CreateRideCommand command = new CreateRideCommand(
                request.userId(), request.origin(), request.destination());
        final Ride ride = createRideUseCase.execute(command);
        final RideResponse response = rideMapper.toRideResponse(ride);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of("Corrida criada com sucesso", response));
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Buscar corrida por ID", description = "Consulta Redis (cache hit) ou PostgreSQL (cache miss)")
    public ResponseEntity<ApiResponse<RideResponse>> findById(@PathVariable UUID rideId) {
        final Ride ride = findRideByIdUseCase.execute(rideId);
        final RideResponse response = rideMapper.toRideResponse(ride);

        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping(params = "status")
    @Operation(summary = "Listar corridas por status", description = "Retorna corridas filtradas por status com paginação")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> findByStatus(
            @RequestParam RideStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        final Page<RideResponse> page = findRidesByStatusUseCase
                .execute(new FindRidesByStatusQuery(status, pageable))
                .map(rideMapper::toRideResponse);

        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @GetMapping(params = "userId")
    @Operation(summary = "Listar corridas por usuário", description = "Retorna corridas de um usuário específico com paginação")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> findByUserId(
            @RequestParam UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        final Page<RideResponse> page = findRidesByUserUseCase
                .execute(new FindRidesByUserQuery(userId, pageable))
                .map(rideMapper::toRideResponse);

        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @PostMapping("/{rideId}/accept")
    @Operation(summary = "Aceitar corrida", description = "Motorista aceita corrida PENDING. Grava no Redis, publica no Kafka e notifica via SSE")
    public ResponseEntity<ApiResponse<RideResponse>> accept(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        final Ride ride = acceptRideUseCase.execute(new AcceptRideCommand(rideId, request.driverId()));
        final RideResponse response = rideMapper.toRideResponse(ride);

        return ResponseEntity.ok(ApiResponse.of("Corrida aceita com sucesso", response));
    }

    @PostMapping("/{rideId}/reject")
    @Operation(summary = "Rejeitar corrida", description = "Motorista rejeita corrida. Status permanece PENDING para outros motoristas")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        rejectRideUseCase.execute(new RejectRideCommand(rideId, request.driverId()));

        return ResponseEntity.ok(ApiResponse.noContent("Corrida rejeitada com sucesso"));
    }

    @PostMapping("/{rideId}/complete")
    @Operation(summary = "Completar corrida", description = "Motorista marca corrida ACCEPTED como COMPLETED")
    public ResponseEntity<ApiResponse<RideResponse>> complete(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        final Ride ride = completeRideUseCase.execute(new CompleteRideCommand(rideId, request.driverId()));
        final RideResponse response = rideMapper.toRideResponse(ride);

        return ResponseEntity.ok(ApiResponse.of("Corrida completada com sucesso", response));
    }

    @GetMapping("/active/user/{userId}")
    @Operation(summary = "Buscar corrida ativa do passageiro", description = "Retorna corrida PENDING ou ACCEPTED do passageiro")
    public ResponseEntity<ApiResponse<RideResponse>> findActiveByUser(@PathVariable UUID userId) {
        return rideRepository.findActiveByUserId(userId)
                .map(ride -> ResponseEntity.ok(ApiResponse.of(rideMapper.toRideResponse(ride))))
                .orElse(ResponseEntity.ok(ApiResponse.of("Nenhuma corrida ativa", null)));
    }

    @GetMapping("/active/driver/{driverId}")
    @Operation(summary = "Buscar corrida ativa do motorista", description = "Retorna corrida ACCEPTED do motorista")
    public ResponseEntity<ApiResponse<RideResponse>> findActiveByDriver(@PathVariable UUID driverId) {
        return rideRepository.findActiveByDriverId(driverId)
                .map(ride -> ResponseEntity.ok(ApiResponse.of(rideMapper.toRideResponse(ride))))
                .orElse(ResponseEntity.ok(ApiResponse.of("Nenhuma corrida ativa", null)));
    }
}
