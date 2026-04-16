package com.rideflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class RideFlowApplicationTests {

    @Test
    void contextLoads() {
        // Garante que nenhuma @Bean, @Configuration ou @Component está quebrada.
    }
}
