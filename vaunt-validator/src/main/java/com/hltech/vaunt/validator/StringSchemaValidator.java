package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import java.util.ArrayList;
import java.util.List;

import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT;

public class StringSchemaValidator {

    public static List<String> validate(StringSchema consumerSchema, StringSchema providerSchema) {
        List<String> errors = new ArrayList<>(ValueTypeSchemaValidator.validate(consumerSchema, providerSchema));

        if (!equals(consumerSchema.getMinLength(), providerSchema.getMinLength())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "minLength",
                    consumerSchema.getMinLength(),
                    providerSchema.getMinLength()));
        }

        if (!equals(consumerSchema.getMaxLength(), providerSchema.getMaxLength())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "maxLength",
                    consumerSchema.getMaxLength(),
                    providerSchema.getMaxLength()));
        }

        if (!equals(consumerSchema.getPattern(), providerSchema.getPattern())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "pattern",
                    consumerSchema.getPattern(),
                    providerSchema.getPattern()));
        }

        return errors;
    }

    private static boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        } else {
            return object1.equals(object2);
        }
    }
}
