package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT;
import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT_SHORT;

public class ObjectSchemaValidator {

    public static List<String> validate(ObjectSchema consumerSchema, ObjectSchema providerSchema) {
        List<String> errors = new ArrayList<>(ContainerTypeSchemaValidator.validate(consumerSchema, providerSchema));

        if (!equals(consumerSchema.getAdditionalProperties(), providerSchema.getAdditionalProperties())) {
            errors.add(String.format(ERROR_FORMAT_SHORT,
                    consumerSchema.getId(),
                    "additionalProperties"));
        }

        if (!equals(consumerSchema.getDependencies(), providerSchema.getDependencies())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "dependencies",
                    consumerSchema.getDependencies(),
                    providerSchema.getDependencies()));
        }

        if (!equals(consumerSchema.getPatternProperties(), providerSchema.getPatternProperties())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "patternProperties",
                    mapToString(consumerSchema.getPatternProperties()),
                    mapToString(providerSchema.getPatternProperties())));
        }

        errors.addAll(compareObjectProperties(
                consumerSchema.getId(),
                consumerSchema.getProperties(),
                providerSchema.getProperties()));

        return errors;
    }

    private static List<String> compareObjectProperties(String id, Map<String, JsonSchema> consumerProperties,
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
                .flatMap(key -> VauntSchemaValidator.validate(
                        consumerProperties.get(key), providerProperties.get(key)).stream())
                .collect(Collectors.toList());
    }

    private static boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        } else {
            return object1.equals(object2);
        }
    }

    private static String mapToString(Map<String, JsonSchema> props) {
        int max = props.size() - 1;
        if (max == -1) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, JsonSchema>> it = props.entrySet().iterator();

        sb.append('{');
        for (int i = 0; ; i++) {
            Map.Entry<String, JsonSchema> entry = it.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(jsonToString(entry.getValue()));

            if (i == max) {
                return sb.append('}').toString();
            }
            sb.append(", ");
        }
    }

    private static String jsonToString(JsonSchema object) {
        return "JsonSchema(id=" + object.getId() + ")";
    }
}
