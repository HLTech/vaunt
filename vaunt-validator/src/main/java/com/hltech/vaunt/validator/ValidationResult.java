package com.hltech.vaunt.validator;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ValidationResult {
    private final boolean isValid;
    private final List<ValidationError> errors;

    public static ValidationResult success() {
        return new ValidationResult(true, new ArrayList<>());
    }

    public static ValidationResult failure(ValidationError... errors) {
        return new ValidationResult(false, Arrays.asList(errors));
    }
}
