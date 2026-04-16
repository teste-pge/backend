package com.rideflow.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CepValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCep {
    String message() default "CEP deve estar no formato 00000-000";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
