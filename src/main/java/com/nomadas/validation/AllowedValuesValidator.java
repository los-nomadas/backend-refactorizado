package com.nomadas.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

    private Set<String> allowedValues;

    @Override
    public void initialize(AllowedValues constraintAnnotation) {
        allowedValues = Arrays.stream(constraintAnnotation.value())
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return allowedValues.contains(value.trim().toUpperCase());
    }
}
