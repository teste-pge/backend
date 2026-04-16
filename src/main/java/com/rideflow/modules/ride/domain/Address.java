package com.rideflow.modules.ride.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false, length = 255)
    private String logradouro;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(nullable = false, length = 100)
    private String bairro;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    public String toDisplayString() {
        String comp = (complemento != null && !complemento.isBlank())
                ? " (" + complemento + ")" : "";
        return String.format("%s, %s%s - %s, %s/%s - CEP %s",
                logradouro, numero, comp, bairro, cidade, estado, cep);
    }
}
