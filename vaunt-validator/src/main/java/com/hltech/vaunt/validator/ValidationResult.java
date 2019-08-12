package com.hltech.vaunt.validator;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.FAILED;
import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.OK;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult {
    private final String name;
    private final ValidationStatus result;
    private final List<String> errors;

    public static ValidationResult success(String name) {
        return new ValidationResult(name, OK, new ArrayList<>());
    }

    public static ValidationResult failure(String name, String error) {
        return new ValidationResult(name, FAILED, Lists.newArrayList(error));
    }

    public static ValidationResult failure(String name, List<String> errors) {
        return new ValidationResult(name, FAILED, errors);
    }

    public enum ValidationStatus {
        OK,
        FAILED
    }
}
