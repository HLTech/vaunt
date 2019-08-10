package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema;

import java.util.List;

public class NumberSchemaValidator extends ValueTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        NumberSchema consumerNumberSchema = consumerSchema.asNumberSchema();
        NumberSchema providerNumberSchema = providerSchema.asNumberSchema();

        if (!equals(consumerNumberSchema.getExclusiveMaximum(), providerNumberSchema.getExclusiveMaximum())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerNumberSchema.getId(),
                    "exclusiveMaximum",
                    consumerNumberSchema.getExclusiveMaximum(),
                    providerNumberSchema.getExclusiveMaximum()));
        }

        if (!equals(consumerNumberSchema.getExclusiveMinimum(), providerNumberSchema.getExclusiveMinimum())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerNumberSchema.getId(),
                    "exclusiveMinimum",
                    consumerNumberSchema.getExclusiveMinimum(),
                    providerNumberSchema.getExclusiveMinimum()));
        }

        if (!equals(consumerNumberSchema.getMaximum(), providerNumberSchema.getMaximum())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerNumberSchema.getId(),
                    "maximum",
                    consumerNumberSchema.getMaximum(),
                    providerNumberSchema.getMaximum()));
        }

        if (!equals(consumerNumberSchema.getMinimum(), providerNumberSchema.getMinimum())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerNumberSchema.getId(),
                    "minimum",
                    consumerNumberSchema.getMinimum(),
                    providerNumberSchema.getMinimum()));
        }

        if (!equals(consumerNumberSchema.getMultipleOf(), providerNumberSchema.getMultipleOf())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerNumberSchema.getId(),
                    "multipleOf",
                    consumerNumberSchema.getMultipleOf(),
                    providerNumberSchema.getMultipleOf()));
        }

        return errors;
    }

    @Override
    public Class<?> supportsSchemaType() {
        return NumberSchema.class;
    }
}