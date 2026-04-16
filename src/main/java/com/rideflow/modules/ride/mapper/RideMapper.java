package com.rideflow.modules.ride.mapper;

import com.rideflow.modules.ride.domain.Address;
import com.rideflow.modules.ride.domain.Ride;
import com.rideflow.modules.ride.dto.AddressRequest;
import com.rideflow.modules.ride.dto.AddressResponse;
import com.rideflow.modules.ride.dto.RideResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RideMapper {

    @Mapping(target = "displayString", expression = "java(address.toDisplayString())")
    AddressResponse toAddressResponse(Address address);

    Address toAddress(AddressRequest request);

    RideResponse toRideResponse(Ride ride);
}
