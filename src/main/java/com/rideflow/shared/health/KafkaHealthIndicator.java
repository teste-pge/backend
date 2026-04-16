package com.rideflow.shared.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component("kafka")
@RequiredArgsConstructor
public class KafkaHealthIndicator extends AbstractHealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            var options = new DescribeClusterOptions().timeoutMs(5000);
            var result = client.describeCluster(options);
            var nodes = result.nodes().get(5, TimeUnit.SECONDS);
            var clusterId = result.clusterId().get(5, TimeUnit.SECONDS);

            builder.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("brokerCount", nodes.size());
        } catch (Exception ex) {
            log.warn("Kafka health check falhou: {}", ex.getMessage());
            builder.down(ex)
                    .withDetail("error", ex.getMessage());
        }
    }
}
