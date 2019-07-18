package com.hltech.vaunt.validator;

import com.hltech.vaunt.core.domain.model.Contract;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ValidationResult {
    private final boolean isValid;
    private final String description;
    private final List<ValidationError> errors;

    static ValidationResult success(Contract expectation) {
        return new ValidationResult(
                true,
                String.format("Matching contract: %s %s, %s",
                        expectation.getDestinationType(),
                        expectation.getDestinationName(),
                        expectation.getBody().getId()),
                new ArrayList<>());
    }

    static ValidationResult failure(Contract expectation, ValidationError... errors) {
        return new ValidationResult(false, String.format("Expectation: %s", expectation), Arrays.asList(errors));
    }

    static ValidationResult failure(Contract expectation, Contract capability, ValidationError... errors) {
        return new ValidationResult(
                false,
                String.format("Expectation: %s, Capability: %s", expectation, capability),
                Arrays.asList(errors));
    }
}
