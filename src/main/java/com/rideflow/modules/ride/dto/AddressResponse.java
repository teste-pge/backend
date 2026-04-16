package com.rideflow.modules.ride.dto;

public record AddressResponse(
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String displayString
        ) {

}
