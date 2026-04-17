package com.rideflow.shared.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BrazilianStateValidatorTest {

    private final BrazilianStateValidator validator = new BrazilianStateValidator();

    @Test
    void nullValue_shouldBeValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SP", "RJ", "MG", "BA", "RS", "PR", "CE", "DF", "GO", "PA",
            "AM", "MA", "SC", "PE", "PB", "ES", "AL", "RN", "PI", "SE", "TO",
            "RO", "AC", "AP", "RR", "MT", "MS"})
    void validStates_shouldBeValid(String state) {
        assertThat(validator.isValid(state, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"sp", "rj"})
    void lowercaseStates_shouldBeValid(String state) {
        assertThat(validator.isValid(state, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"XX", "AB", "ZZ", "", "SPA"})
    void invalidStates_shouldBeInvalid(String state) {
        assertThat(validator.isValid(state, null)).isFalse();
    }
}
