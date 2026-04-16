package com.rideflow.shared.handler;

import com.rideflow.shared.exception.DriverNotAvailableException;
import com.rideflow.shared.exception.DriverNotFoundException;
import com.rideflow.shared.exception.RideAlreadyAcceptedException;
import com.rideflow.shared.exception.RideNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn404WhenRideNotFound() throws Exception {
        mockMvc.perform(get("/test/ride-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RIDE_NOT_FOUND"));
    }

    @Test
    void shouldReturn409WhenRideAlreadyAccepted() throws Exception {
        mockMvc.perform(get("/test/ride-already-accepted"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RIDE_ALREADY_ACCEPTED"));
    }

    @Test
    void shouldReturn404WhenDriverNotFound() throws Exception {
        mockMvc.perform(get("/test/driver-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DRIVER_NOT_FOUND"));
    }

    @Test
    void shouldReturn422WhenDriverNotAvailable() throws Exception {
        mockMvc.perform(get("/test/driver-not-available"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("DRIVER_NOT_AVAILABLE"));
    }

    @Test
    void shouldReturn400WhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/test/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));
    }

    @Test
    void shouldReturn400WithFieldErrorsOnValidationFailure() throws Exception {
        mockMvc.perform(post("/test/echo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void shouldReturn500OnUnexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnTimestampInAllErrorResponses() throws Exception {
        mockMvc.perform(get("/test/ride-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @RestController
    @RequestMapping("/test")
    static class FakeController {

        record NameRequest(@jakarta.validation.constraints.NotBlank String name) {

        }

        @GetMapping("/ride-not-found")
        void rideNotFound() {
            throw new RideNotFoundException("abc-123");
        }

        @GetMapping("/ride-already-accepted")
        void rideAlreadyAccepted() {
            throw new RideAlreadyAcceptedException("abc-123");
        }

        @GetMapping("/driver-not-found")
        void driverNotFound() {
            throw new DriverNotFoundException("drv-456");
        }

        @GetMapping("/driver-not-available")
        void driverNotAvailable() {
            throw new DriverNotAvailableException("drv-456");
        }

        @GetMapping("/unexpected-error")
        void unexpectedError() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/echo")
        void echo(@RequestBody @jakarta.validation.Valid NameRequest req) {
        }
    }
}
