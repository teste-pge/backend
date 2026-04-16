package com.rideflow.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BrazilianStateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BrazilianState {
    String message() default "Estado deve ser uma sigla UF válida (ex: SP, RJ, MG)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
