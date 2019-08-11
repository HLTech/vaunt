package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObjectSchemaValidator extends ContainerTypeSchemaValidator {
    private static final String UNMATCHING_SCHEMA_TYPE =
            "Consumer schema with id %s and type %s does not match provider schema with id %s and type %s";

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        ObjectSchema consumerObjectSchema = consumerSchema.asObjectSchema();
        ObjectSchema providerObjectSchema = providerSchema.asObjectSchema();

        if (!equals(consumerObjectSchema.getAdditionalProperties(), providerObjectSchema.getAdditionalProperties())) {
            errors.add(String.format(ERROR_FORMAT_SHORT,
                    consumerSchema.getId(),
                    "additionalProperties"));
        }

        if (!equals(consumerObjectSchema.getDependencies(), providerObjectSchema.getDependencies())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "dependencies",
                    consumerObjectSchema.getDependencies(),
                    providerObjectSchema.getDependencies()));
        }

        if (!equals(consumerObjectSchema.getPatternProperties(), providerObjectSchema.getPatternProperties())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "patternProperties",
                    mapToString(consumerObjectSchema.getPatternProperties()),
                    mapToString(providerObjectSchema.getPatternProperties())));
        }

        errors.addAll(compareObjectProperties(
                consumerSchema.getId(),
                consumerObjectSchema.getProperties(),
                providerObjectSchema.getProperties()));

        return errors;
    }

    @Override
    public Class<ObjectSchema> supportsSchemaType() {
        return ObjectSchema.class;
    }

    private List<String> compareObjectProperties(String id, Map<String, JsonSchema> consumerProperties,
                                            Map<String, JsonSchema> providerProperties) {

        if (!providerProperties.keySet().containsAll(consumerProperties.keySet())) {
            return Lists.newArrayList(String.format(ERROR_FORMAT,
                    id,
                    "properties",
                    "ids of properties: " + consumerProperties.keySet(),
                    "ids of properties: " + providerProperties.keySet()
                    ));
        }

        return consumerProperties.keySet().stream()
                .flatMap(key -> validateProperty(consumerProperties.get(key), providerProperties.get(key)).stream())
                .collect(Collectors.toList());
    }

    private List<String> validateProperty(JsonSchema consumerProperty, JsonSchema providerProperty) {
        return consumerProperty.getClass() == providerProperty.getClass()
                ? SchemaValidator.validate(consumerProperty, providerProperty)
                : Lists.newArrayList(String.format(
                        UNMATCHING_SCHEMA_TYPE,
                        consumerProperty.getId(),
                        consumerProperty.getClass().getSimpleName(),
                        providerProperty.getId(),
                        providerProperty.getClass().getSimpleName()));
    }

    private String mapToString(Map<String, JsonSchema> props) {
        String content = props.entrySet()
                .stream()
                .map(e -> e.getKey() + '=' + jsonToString(e.getValue()))
                .collect(Collectors.joining(", "));

        return "{" + content + "}";
    }
}
