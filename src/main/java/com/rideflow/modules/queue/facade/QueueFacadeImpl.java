package com.rideflow.modules.queue.facade;

import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.queue.dto.RideRejectedEvent;
import com.rideflow.modules.queue.producer.RideEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueFacadeImpl implements QueueFacade {

    private final RideEventProducer rideEventProducer;

    @Override
    public void publishRideCreated(RideCreatedEvent event) {
        rideEventProducer.publishRideCreated(event);
    }

    @Override
    public void publishRideAccepted(RideAcceptedEvent event) {
        rideEventProducer.publishRideAccepted(event);
    }

    @Override
    public void publishRideRejected(RideRejectedEvent event) {
        rideEventProducer.publishRideRejected(event);
    }
}
