package com.rideflow.modules.ride.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    @Test
    void toDisplayString_withoutComplemento() {
        Address addr = new Address("01310-100", "Av Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        String result = addr.toDisplayString();
        assertThat(result).isEqualTo("Av Paulista, 1000 - Bela Vista, São Paulo/SP - CEP 01310-100");
    }

    @Test
    void toDisplayString_withComplemento() {
        Address addr = new Address("01310-100", "Av Paulista", "1000", "Apto 42", "Bela Vista", "São Paulo", "SP");
        String result = addr.toDisplayString();
        assertThat(result).contains("(Apto 42)");
        assertThat(result).startsWith("Av Paulista, 1000");
    }

    @Test
    void toDisplayString_withBlankComplemento() {
        Address addr = new Address("01310-100", "Av Paulista", "1000", "  ", "Bela Vista", "São Paulo", "SP");
        String result = addr.toDisplayString();
        assertThat(result).doesNotContain("(");
    }

    @Test
    void gettersAndSetters() {
        Address addr = new Address();
        addr.setCep("01310-100");
        addr.setLogradouro("Rua A");
        addr.setNumero("1");
        addr.setComplemento("Sala 1");
        addr.setBairro("Centro");
        addr.setCidade("SP");
        addr.setEstado("SP");

        assertThat(addr.getCep()).isEqualTo("01310-100");
        assertThat(addr.getLogradouro()).isEqualTo("Rua A");
        assertThat(addr.getNumero()).isEqualTo("1");
        assertThat(addr.getComplemento()).isEqualTo("Sala 1");
        assertThat(addr.getBairro()).isEqualTo("Centro");
        assertThat(addr.getCidade()).isEqualTo("SP");
        assertThat(addr.getEstado()).isEqualTo("SP");
    }

    @Test
    void equalsAndHashCode() {
        Address a1 = new Address("01310-100", "Av Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        Address a2 = new Address("01310-100", "Av Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
        Address a3 = new Address("20040-020", "Av Rio Branco", "1", null, "Centro", "Rio de Janeiro", "RJ");

        assertThat(a1).isEqualTo(a2);
        assertThat(a1).isNotEqualTo(a3);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }
}
