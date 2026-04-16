package com.rideflow.modules.ride.mapper;

import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.dto.AddressRequest;
import com.rideflow.modules.ride.dto.AddressResponse;
import com.rideflow.modules.ride.dto.CreateRideRequest;
import com.rideflow.modules.ride.dto.RideResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RideMapper {

    @Mapping(target = "displayString", expression = "java(address.toDisplayString())")
    AddressResponse toAddressResponse(Address address);

    Address toAddress(AddressRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    Ride toRide(CreateRideRequest request);

    RideResponse toRideResponse(Ride ride);
}
