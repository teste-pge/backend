package com.rideflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class KafkaConfig {

    @Bean
    public NewTopic rideCreatedTopic() {
        return TopicBuilder.name("ride.created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic rideAcceptedTopic() {
        return TopicBuilder.name("ride.accepted").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic rideRejectedTopic() {
        return TopicBuilder.name("ride.rejected").partitions(3).replicas(1).build();
    }
}
