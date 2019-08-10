package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;

import java.util.List;

public class StringSchemaValidator extends ValueTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        StringSchema consumerStringSchema = consumerSchema.asStringSchema();
        StringSchema providerStringSchema = providerSchema.asStringSchema();

        if (!equals(consumerStringSchema.getMinLength(), providerStringSchema.getMinLength())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerStringSchema.getId(),
                    "minLength",
                    consumerStringSchema.getMinLength(),
                    providerStringSchema.getMinLength()));
        }

        if (!equals(consumerStringSchema.getMaxLength(), providerStringSchema.getMaxLength())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerStringSchema.getId(),
                    "maxLength",
                    consumerStringSchema.getMaxLength(),
                    providerStringSchema.getMaxLength()));
        }

        if (!equals(consumerStringSchema.getPattern(), providerStringSchema.getPattern())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerStringSchema.getId(),
                    "pattern",
                    consumerStringSchema.getPattern(),
                    providerStringSchema.getPattern()));
        }

        return errors;
    }

    @Override
    public Class<?> supportsSchemaType() {
        return StringSchema.class;
    }
}