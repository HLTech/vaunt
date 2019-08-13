package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hltech.vaunt.validator.VauntValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SchemaValidator {

    static final String UNMATCHING_SCHEMA_TYPE =
            "Consumer schema with id %s and type %s does not match provider schema with id %s and type %s";
    private static final String VALIDATOR_SEARCH_ERROR =
            "Exactly one validator should exist for consumer and provider of type %s";

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

        if (!(consumerSchema.getClass() == providerSchema.getClass())) {
            return Lists.newArrayList(
                    String.format(UNMATCHING_SCHEMA_TYPE,
                            consumerSchema.getId(),
                            consumerSchema.getClass().getSimpleName(),
                            providerSchema.getId(),
                            providerSchema.getClass().getSimpleName()));
        }

        List<JsonSchemaValidator> matchingValidators = schemaValidators.stream()
                .filter(v -> v.supportsSchemaType().equals(consumerSchema.getClass()))
                .collect(Collectors.toList());

        if (matchingValidators.size() != 1) {
            throw new VauntValidationException(String.format(
                    VALIDATOR_SEARCH_ERROR, consumerSchema.getClass().getSimpleName()));
        }

        return matchingValidators.get(0).validate(consumerSchema, providerSchema);
    }
}
