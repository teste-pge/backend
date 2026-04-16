package com.rideflow.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DifferentAddressesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DifferentAddresses {
    String message() default "Endereço de origem e destino não podem ser iguais";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String originCepField() default "origin.cep";
    String originNumeroField() default "origin.numero";
    String destinationCepField() default "destination.cep";
    String destinationNumeroField() default "destination.numero";
}
