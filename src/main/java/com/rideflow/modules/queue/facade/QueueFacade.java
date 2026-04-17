package com.rideflow.modules.queue.facade;

import com.rideflow.modules.queue.dto.RideAcceptedEvent;
import com.rideflow.modules.queue.dto.RideCompletedEvent;
import com.rideflow.modules.queue.dto.RideCreatedEvent;
import com.rideflow.modules.queue.dto.RideRejectedEvent;

public interface QueueFacade {

    void publishRideCreated(RideCreatedEvent event);

    void publishRideAccepted(RideAcceptedEvent event);

    void publishRideRejected(RideRejectedEvent event);

    void publishRideCompleted(RideCompletedEvent event);
}
