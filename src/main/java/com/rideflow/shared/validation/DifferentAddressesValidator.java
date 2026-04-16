package com.rideflow.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import java.util.Objects;

public class DifferentAddressesValidator implements ConstraintValidator<DifferentAddresses, Object> {

    private String originCepField;
    private String originNumeroField;
    private String destinationCepField;
    private String destinationNumeroField;

    @Override
    public void initialize(DifferentAddresses annotation) {
        this.originCepField = annotation.originCepField();
        this.originNumeroField = annotation.originNumeroField();
        this.destinationCepField = annotation.destinationCepField();
        this.destinationNumeroField = annotation.destinationNumeroField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        BeanWrapper wrapper = new BeanWrapperImpl(value);
        var originCep = (String) wrapper.getPropertyValue(originCepField);
        var originNumero = (String) wrapper.getPropertyValue(originNumeroField);
        var destCep = (String) wrapper.getPropertyValue(destinationCepField);
        var destNumero = (String) wrapper.getPropertyValue(destinationNumeroField);

        if (originCep == null || destCep == null) return true;

        boolean sameAddress = originCep.equals(destCep) && Objects.equals(originNumero, destNumero);
        if (sameAddress) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("destination.cep")
                    .addConstraintViolation();
        }
        return !sameAddress;
    }
}
