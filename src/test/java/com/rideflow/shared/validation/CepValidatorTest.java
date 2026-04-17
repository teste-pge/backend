package com.rideflow.shared.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CepValidatorTest {

    private final CepValidator validator = new CepValidator();

    @Test
    void nullValue_shouldBeValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"01310-100", "20040-020", "00000-000"})
    void validCeps_shouldBeValid(String cep) {
        assertThat(validator.isValid(cep, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"01310100", "1234-567", "abcde-fgh", "01310-10", ""})
    void invalidCeps_shouldBeInvalid(String cep) {
        assertThat(validator.isValid(cep, null)).isFalse();
    }
}
