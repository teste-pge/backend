package com.rideflow.modules.ride.dto;

import com.rideflow.shared.validation.BrazilianState;
import com.rideflow.shared.validation.ValidCep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotNull
        @ValidCep
        String cep,
        @NotBlank
        @Size(min = 3, max = 255)
        String logradouro,
        @NotBlank
        @Size(min = 1, max = 20)
        String numero,
        @Size(max = 100)
        String complemento,
        @NotBlank
        @Size(min = 3, max = 100)
        String bairro,
        @NotBlank
        @Size(min = 3, max = 100)
        String cidade,
        @NotNull
        @BrazilianState
        String estado
        ) {

}
