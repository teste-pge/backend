package com.rideflow.shared.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DifferentAddressesValidatorTest {

    private DifferentAddressesValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new DifferentAddressesValidator();

        DifferentAddresses annotation = mock(DifferentAddresses.class);
        when(annotation.originCepField()).thenReturn("originCep");
        when(annotation.originNumeroField()).thenReturn("originNumero");
        when(annotation.destinationCepField()).thenReturn("destCep");
        when(annotation.destinationNumeroField()).thenReturn("destNumero");
        validator.initialize(annotation);

        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(context.getDefaultConstraintMessageTemplate()).thenReturn("msg");
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    @Test
    void nullValue_shouldBeValid() {
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    void differentAddresses_shouldBeValid() {
        TestDTO dto = new TestDTO("01310-100", "100", "20040-020", "1");
        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    void sameAddresses_shouldBeInvalid() {
        TestDTO dto = new TestDTO("01310-100", "100", "01310-100", "100");
        assertThat(validator.isValid(dto, context)).isFalse();
    }

    @Test
    void nullOriginCep_shouldBeValid() {
        TestDTO dto = new TestDTO(null, "100", "20040-020", "1");
        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    void nullDestCep_shouldBeValid() {
        TestDTO dto = new TestDTO("01310-100", "100", null, "1");
        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    void sameCepDifferentNumero_shouldBeValid() {
        TestDTO dto = new TestDTO("01310-100", "100", "01310-100", "200");
        assertThat(validator.isValid(dto, context)).isTrue();
    }

    public static class TestDTO {
        private String originCep;
        private String originNumero;
        private String destCep;
        private String destNumero;

        public TestDTO(String originCep, String originNumero, String destCep, String destNumero) {
            this.originCep = originCep;
            this.originNumero = originNumero;
            this.destCep = destCep;
            this.destNumero = destNumero;
        }

        public String getOriginCep() { return originCep; }
        public String getOriginNumero() { return originNumero; }
        public String getDestCep() { return destCep; }
        public String getDestNumero() { return destNumero; }
    }
}
