package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SchemaValidator {

    static final String UNMATCHING_SCHEMA_TYPE =
            "Consumer schema with id %s and type %s does not match provider schema with id %s and type %s";

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
        Optional<JsonSchemaValidator> matchingValidator = schemaValidators.stream()
                .filter(v -> v.supportsSchemaType().equals(consumerSchema.getClass()))
                .filter(v -> v.supportsSchemaType().equals(providerSchema.getClass()))
                .findAny();

        if (!matchingValidator.isPresent()) {
            return Lists.newArrayList(
                    String.format(UNMATCHING_SCHEMA_TYPE,
                            consumerSchema.getId(),
                            consumerSchema.getClass().getSimpleName(),
                            providerSchema.getId(),
                            providerSchema.getClass().getSimpleName()));
        }

        return matchingValidator.get().validate(consumerSchema, providerSchema);
    }
}
