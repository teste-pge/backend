package com.rideflow;

import com.rideflow.modules.driver.repository.DriverRepository;
import com.rideflow.modules.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
        }
)
@ActiveProfiles("test")
class RideFlowApplicationTests {

    @MockBean
    private DriverRepository driverRepository;

    @MockBean
    private RideRepository rideRepository;

    @SuppressWarnings("rawtypes")
    @MockBean
    private KafkaTemplate kafkaTemplate;

    @SuppressWarnings("rawtypes")
    @MockBean
    private org.springframework.data.redis.core.RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }
}
