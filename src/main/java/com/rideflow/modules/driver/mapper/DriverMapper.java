package com.rideflow.modules.driver.mapper;

import com.rideflow.modules.driver.domain.Driver;
import com.rideflow.modules.driver.dto.DriverResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    DriverResponse toDriverResponse(Driver driver);
}
