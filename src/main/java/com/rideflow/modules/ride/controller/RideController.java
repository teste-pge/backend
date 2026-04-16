package com.rideflow.modules.ride.controller;

import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.DriverActionRequest;
import com.rideflow.modules.ride.dto.RideResponse;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.usecase.command.AcceptRideCommand;
import com.rideflow.modules.ride.usecase.command.AcceptRideUseCase;
import com.rideflow.modules.ride.usecase.command.CreateRideCommand;
import com.rideflow.modules.ride.usecase.command.CreateRideUseCase;
import com.rideflow.modules.ride.usecase.command.RejectRideCommand;
import com.rideflow.modules.ride.usecase.command.RejectRideUseCase;
import com.rideflow.modules.ride.usecase.query.FindRideByIdUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusQuery;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserQuery;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserUseCase;
import com.rideflow.shared.response.ApiResponse;
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
public class RideController {

    private final CreateRideUseCase createRideUseCase;
    private final AcceptRideUseCase acceptRideUseCase;
    private final RejectRideUseCase rejectRideUseCase;
    private final FindRideByIdUseCase findRideByIdUseCase;
    private final FindRidesByStatusUseCase findRidesByStatusUseCase;
    private final FindRidesByUserUseCase findRidesByUserUseCase;
    private final RideMapper rideMapper;

    @PostMapping
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
    public ResponseEntity<ApiResponse<RideResponse>> findById(@PathVariable UUID rideId) {
        final Ride ride = findRideByIdUseCase.execute(rideId);
        final RideResponse response = rideMapper.toRideResponse(ride);

        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping(params = "status")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> findByStatus(
            @RequestParam RideStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        final Page<RideResponse> page = findRidesByStatusUseCase
                .execute(new FindRidesByStatusQuery(status, pageable))
                .map(rideMapper::toRideResponse);

        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @GetMapping(params = "userId")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> findByUserId(
            @RequestParam UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        final Page<RideResponse> page = findRidesByUserUseCase
                .execute(new FindRidesByUserQuery(userId, pageable))
                .map(rideMapper::toRideResponse);

        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<ApiResponse<RideResponse>> accept(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        final Ride ride = acceptRideUseCase.execute(new AcceptRideCommand(rideId, request.driverId()));
        final RideResponse response = rideMapper.toRideResponse(ride);

        return ResponseEntity.ok(ApiResponse.of("Corrida aceita com sucesso", response));
    }

    @PostMapping("/{rideId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        rejectRideUseCase.execute(new RejectRideCommand(rideId, request.driverId()));

        return ResponseEntity.ok(ApiResponse.noContent("Corrida rejeitada com sucesso"));
    }
}
