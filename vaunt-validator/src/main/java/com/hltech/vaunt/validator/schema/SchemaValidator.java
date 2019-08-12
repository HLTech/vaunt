package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Sets;
import com.hltech.vaunt.validator.VauntValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SchemaValidator {

    private static final Set<JsonSchemaValidator> schemaValidators = new HashSet<>();

    static {
        schemaValidators.addAll(Sets.newHashSet(
                new StringSchemaValidator(),
                new ObjectSchemaValidator(),
                new BooleanSchemaValidator(),
                new IntegerSchemaValidator(),
                new NumberSchemaValidator()
        ));
    }

    public static List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        JsonSchemaValidator matchingValidator = schemaValidators.stream()
                .filter(v -> v.supportsSchemaType().equals(consumerSchema.getClass()))
                .filter(v -> v.supportsSchemaType().equals(providerSchema.getClass()))
                .findAny()
                .orElseThrow(() -> new VauntValidationException(
                        "Exactly one validator should exist for consumer and provider"));

        return matchingValidator.validate(consumerSchema, providerSchema);
    }
}
