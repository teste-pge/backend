package com.rideflow.modules.ride.controller;

import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.DriverActionRequest;
import com.rideflow.modules.ride.dto.RideResponse;
import com.rideflow.modules.ride.facade.RideFacade;
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

    private final RideFacade rideFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<RideResponse>> create(@Valid @RequestBody CreateRideRequest request) {
        RideResponse response = rideFacade.createRide(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of("Corrida criada com sucesso", response));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<ApiResponse<RideResponse>> findById(@PathVariable UUID rideId) {
        RideResponse response = rideFacade.findById(rideId);

        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @GetMapping(params = "status")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> findByStatus(
            @RequestParam RideStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RideResponse> page = rideFacade.findByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @GetMapping(params = "userId")
    public ResponseEntity<ApiResponse<Page<RideResponse>>> findByUserId(
            @RequestParam UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<RideResponse> page = rideFacade.findByUserId(userId, pageable);

        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<ApiResponse<RideResponse>> accept(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        RideResponse response = rideFacade.acceptRide(rideId, request.driverId());

        return ResponseEntity.ok(ApiResponse.of("Corrida aceita com sucesso", response));
    }

    @PostMapping("/{rideId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable UUID rideId,
            @Valid @RequestBody DriverActionRequest request) {

        rideFacade.rejectRide(rideId, request.driverId());

        return ResponseEntity.ok(ApiResponse.noContent("Corrida rejeitada com sucesso"));
    }
}
