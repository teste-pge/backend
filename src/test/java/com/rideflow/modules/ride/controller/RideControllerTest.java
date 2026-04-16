package com.rideflow.modules.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.AddressResponse;
import com.rideflow.modules.ride.dto.RideResponse;
import com.rideflow.modules.ride.mapper.RideMapper;
import com.rideflow.modules.ride.usecase.command.AcceptRideUseCase;
import com.rideflow.modules.ride.usecase.command.CreateRideUseCase;
import com.rideflow.modules.ride.usecase.command.RejectRideUseCase;
import com.rideflow.modules.ride.usecase.query.FindRideByIdUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByStatusUseCase;
import com.rideflow.modules.ride.usecase.query.FindRidesByUserUseCase;
import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import com.rideflow.shared.exception.RideNotFoundException;
import com.rideflow.shared.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {RideController.class, GlobalExceptionHandler.class})
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateRideUseCase createRideUseCase;

    @MockBean
    private AcceptRideUseCase acceptRideUseCase;

    @MockBean
    private RejectRideUseCase rejectRideUseCase;

    @MockBean
    private FindRideByIdUseCase findRideByIdUseCase;

    @MockBean
    private FindRidesByStatusUseCase findRidesByStatusUseCase;

    @MockBean
    private FindRidesByUserUseCase findRidesByUserUseCase;

    @MockBean
    private RideMapper rideMapper;

    private static final UUID RIDE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID DRIVER_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private RideResponse sampleRideResponse(RideStatus status) {
        return new RideResponse(
                RIDE_ID, USER_ID, status == RideStatus.ACCEPTED ? DRIVER_ID : null,
                new AddressResponse("01310-100", "Av Paulista", "1000", null,
                        "Bela Vista", "São Paulo", "SP",
                        "Av Paulista, 1000 - Bela Vista, São Paulo/SP - CEP 01310-100"),
                new AddressResponse("04543-907", "Av Faria Lima", "3477", null,
                        "Itaim Bibi", "São Paulo", "SP",
                        "Av Faria Lima, 3477 - Itaim Bibi, São Paulo/SP - CEP 04543-907"),
                status,
                Instant.parse("2026-04-16T12:00:00Z"),
                Instant.parse("2026-04-16T12:00:00Z"),
                status == RideStatus.ACCEPTED ? Instant.parse("2026-04-16T12:01:00Z") : null
        );
    }

    @Test
    void createRide_valid_shouldReturn201() throws Exception {
        Ride dummyRide = Ride.builder().id(RIDE_ID).userId(USER_ID).status(RideStatus.PENDING).build();
        when(createRideUseCase.execute(any())).thenReturn(dummyRide);
        when(rideMapper.toRideResponse(any(Ride.class))).thenReturn(sampleRideResponse(RideStatus.PENDING));

        String body = """
                {
                    "userId": "%s",
                    "origin": {
                        "cep": "01310-100", "logradouro": "Av Paulista", "numero": "1000",
                        "bairro": "Bela Vista", "cidade": "São Paulo", "estado": "SP"
                    },
                    "destination": {
                        "cep": "04543-907", "logradouro": "Av Faria Lima", "numero": "3477",
                        "bairro": "Itaim Bibi", "cidade": "São Paulo", "estado": "SP"
                    }
                }
                """.formatted(USER_ID);

        mockMvc.perform(post("/api/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(RIDE_ID.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createRide_invalid_shouldReturn400WithFieldErrors() throws Exception {
        String body = """
                {
                }
                """;

        mockMvc.perform(post("/api/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void findById_existing_shouldReturn200() throws Exception {
        Ride dummyRide = Ride.builder().id(RIDE_ID).userId(USER_ID).status(RideStatus.PENDING).build();
        when(findRideByIdUseCase.execute(RIDE_ID)).thenReturn(dummyRide);
        when(rideMapper.toRideResponse(any(Ride.class))).thenReturn(sampleRideResponse(RideStatus.PENDING));

        mockMvc.perform(get("/api/v1/rides/{rideId}", RIDE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(RIDE_ID.toString()))
                .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        when(findRideByIdUseCase.execute(RIDE_ID)).thenThrow(new RideNotFoundException(RIDE_ID.toString()));

        mockMvc.perform(get("/api/v1/rides/{rideId}", RIDE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RIDE_NOT_FOUND"));
    }

    @Test
    void findByStatus_shouldReturn200WithPage() throws Exception {
        Ride dummyRide = Ride.builder().id(RIDE_ID).userId(USER_ID).status(RideStatus.PENDING).build();
        var page = new PageImpl<>(List.of(dummyRide), PageRequest.of(0, 20), 1);
        when(findRidesByStatusUseCase.execute(any())).thenReturn(page);
        when(rideMapper.toRideResponse(any(Ride.class))).thenReturn(sampleRideResponse(RideStatus.PENDING));

        mockMvc.perform(get("/api/v1/rides")
                .param("status", "PENDING")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void acceptRide_pending_shouldReturn200() throws Exception {
        Ride dummyRide = Ride.builder().id(RIDE_ID).userId(USER_ID).driverId(DRIVER_ID).status(RideStatus.ACCEPTED).build();
        when(acceptRideUseCase.execute(any())).thenReturn(dummyRide);
        when(rideMapper.toRideResponse(any(Ride.class))).thenReturn(sampleRideResponse(RideStatus.ACCEPTED));

        String body = """
                { "driverId": "%s" }
                """.formatted(DRIVER_ID);

        mockMvc.perform(post("/api/v1/rides/{rideId}/accept", RIDE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.driverId").value(DRIVER_ID.toString()));
    }

    @Test
    void acceptRide_alreadyAccepted_shouldReturn409() throws Exception {
        when(acceptRideUseCase.execute(any()))
                .thenThrow(new RideAlreadyAcceptedException(RIDE_ID.toString()));

        String body = """
                { "driverId": "%s" }
                """.formatted(DRIVER_ID);

        mockMvc.perform(post("/api/v1/rides/{rideId}/accept", RIDE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RIDE_ALREADY_ACCEPTED"));
    }

    @Test
    void rejectRide_shouldReturn200() throws Exception {
        doNothing().when(rejectRideUseCase).execute(any());

        String body = """
                { "driverId": "%s" }
                """.formatted(DRIVER_ID);

        mockMvc.perform(post("/api/v1/rides/{rideId}/reject", RIDE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Corrida rejeitada com sucesso"));
    }
}
