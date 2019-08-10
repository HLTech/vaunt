package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema;

import java.util.List;

public class IntegerSchemaValidator extends NumberSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        IntegerSchema consumerIntegerSchema = consumerSchema.asIntegerSchema();
        IntegerSchema providerIntegerSchema = providerSchema.asIntegerSchema();

        if (!equals(consumerIntegerSchema.getDivisibleBy(), providerIntegerSchema.getDivisibleBy())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerIntegerSchema.getId(),
                    "divisibleBy",
                    consumerIntegerSchema.getDivisibleBy(),
                    providerIntegerSchema.getDivisibleBy()));
        }

        return errors;
    }

    @Override
    public Class<?> supportsSchemaType() {
        return IntegerSchema.class;
    }
}
