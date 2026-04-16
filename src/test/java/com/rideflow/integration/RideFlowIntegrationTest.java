package com.rideflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideflow.modules.ride.domain.RideStatus;
import com.rideflow.modules.ride.dto.AddressRequest;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.DriverActionRequest;
import com.rideflow.modules.ride.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
        }
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RideFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("rideflow_integration")
            .withUsername("rideflow")
            .withPassword("rideflow");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RideRepository rideRepository;

    @SuppressWarnings("rawtypes")
    @MockBean
    private KafkaTemplate kafkaTemplate;

    @MockBean
    private KafkaAdmin kafkaAdmin;

    @SuppressWarnings("rawtypes")
    @MockBean
    private org.springframework.data.redis.core.RedisTemplate redisTemplate;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void cleanUp() {
        rideRepository.deleteAll();
        org.mockito.Mockito.when(kafkaTemplate.send(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));
    }

    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DRIVER_ID_1 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID DRIVER_ID_2 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private AddressRequest originAddress() {
        return new AddressRequest("01310-100", "Av. Paulista", "1000", null,
                "Bela Vista", "São Paulo", "SP");
    }

    private AddressRequest destinationAddress() {
        return new AddressRequest("20040-020", "Av. Rio Branco", "200", null,
                "Centro", "Rio de Janeiro", "RJ");
    }

    private CreateRideRequest createRideRequest() {
        return new CreateRideRequest(USER_ID, originAddress(), destinationAddress());
    }

    private String createRideAndGetId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRideRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).path("data").path("id").asText();
    }

    // ════════════════════════════════════════════════════════════════
    //  1. Criar corrida → PG persistido
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("1. POST /rides → 201, persiste no PostgreSQL com status PENDING")
    void createRide_shouldPersistInPostgres() throws Exception {
        mockMvc.perform(post("/api/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRideRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.origin.cep", is("01310-100")))
                .andExpect(jsonPath("$.data.destination.cidade", is("Rio de Janeiro")));

        assertThat(rideRepository.count()).isEqualTo(1);
        assertThat(rideRepository.findAll().getFirst().getStatus()).isEqualTo(RideStatus.PENDING);
    }

    // ════════════════════════════════════════════════════════════════
    //  2. Buscar corrida por ID → 200
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("2. GET /rides/{id} → 200, retorna corrida persistida")
    void findById_shouldReturnPersistedRide() throws Exception {
        String rideId = createRideAndGetId();

        mockMvc.perform(get("/api/v1/rides/{rideId}", rideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(rideId)))
                .andExpect(jsonPath("$.data.status", is("PENDING")));
    }

    // ════════════════════════════════════════════════════════════════
    //  3. Aceitar corrida → ACCEPTED + driverId
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("3. POST /rides/{id}/accept → 200, ACCEPTED com driverId e acceptedAt")
    void acceptRide_shouldUpdateToAccepted() throws Exception {
        String rideId = createRideAndGetId();

        mockMvc.perform(post("/api/v1/rides/{rideId}/accept", rideId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DriverActionRequest(DRIVER_ID_1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.data.driverId", is(DRIVER_ID_1.toString())))
                .andExpect(jsonPath("$.data.acceptedAt", notNullValue()));

        var ride = rideRepository.findAll().getFirst();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(ride.getDriverId()).isEqualTo(DRIVER_ID_1);
    }

    // ════════════════════════════════════════════════════════════════
    //  4. Race condition: 2 motoristas aceitam → 1 sucesso + 1 erro 409
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("4. Race condition: 2 accepts concorrentes → 1x200 + 1x409")
    void raceCondition_twoDriversAccept_onlyOneSucceeds() throws Exception {
        String rideId = createRideAndGetId();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            for (UUID driverId : new UUID[]{DRIVER_ID_1, DRIVER_ID_2}) {
                executor.submit(() -> {
                    try {
                        var result = mockMvc.perform(post("/api/v1/rides/{rideId}/accept", rideId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new DriverActionRequest(driverId))))
                                .andReturn();

                        int status = result.getResponse().getStatus();
                        if (status == 200) {
                            successCount.incrementAndGet();
                        }
                        if (status == 409) {
                            conflictCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // ignored
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);

        var ride = rideRepository.findAll().getFirst();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ACCEPTED);
    }

    // ════════════════════════════════════════════════════════════════
    //  5. Rejeitar corrida → permanece PENDING
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("5. POST /rides/{id}/reject → 200, corrida permanece PENDING")
    void rejectRide_shouldRemainPending() throws Exception {
        String rideId = createRideAndGetId();

        mockMvc.perform(post("/api/v1/rides/{rideId}/reject", rideId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DriverActionRequest(DRIVER_ID_1))))
                .andExpect(status().isOk());

        var ride = rideRepository.findAll().getFirst();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.PENDING);
    }

    // ════════════════════════════════════════════════════════════════
    //  6. Listar por status → filtro funciona
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("6. GET /rides?status=PENDING → retorna apenas PENDING")
    void findByStatus_shouldFilterCorrectly() throws Exception {
        createRideAndGetId();
        createRideAndGetId();
        String acceptedRideId = createRideAndGetId();

        mockMvc.perform(post("/api/v1/rides/{rideId}/accept", acceptedRideId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new DriverActionRequest(DRIVER_ID_1))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/rides")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(2)));
    }

    // ════════════════════════════════════════════════════════════════
    //  7. Corrida inexistente → 404
    // ════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("7. GET /rides/{uuid-inexistente} → 404 NOT_FOUND")
    void findById_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/rides/{rideId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is("RIDE_NOT_FOUND")));
    }
}
