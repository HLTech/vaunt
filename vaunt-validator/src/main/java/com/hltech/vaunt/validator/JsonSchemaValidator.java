package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class JsonSchemaValidator {
    static final String ERROR_FORMAT = "Schema with id %s has not matching %s - consumer: %s, provider: %s";
    static final String ERROR_FORMAT_SHORT = "Schema with id %s has not matching %s";

    public abstract Class<?> supportsSchemaType();

    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = new ArrayList<>();

        if (!equals(consumerSchema.get$ref(), providerSchema.get$ref())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "$ref",
                    consumerSchema.get$ref(),
                    providerSchema.get$ref()));
        }

        if (!equals(consumerSchema.get$schema(), providerSchema.get$schema())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "$schema",
                    consumerSchema.get$schema(),
                    providerSchema.get$schema()));
        }

        if (!arraysEquals(consumerSchema.getDisallow(), providerSchema.getDisallow())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "disallow",
                    jsonArrayToString(consumerSchema.getDisallow()),
                    jsonArrayToString(providerSchema.getDisallow())));
        }

        if (!arraysEquals(consumerSchema.getExtends(), providerSchema.getExtends())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "extends",
                    jsonArrayToString(consumerSchema.getExtends()),
                    jsonArrayToString(providerSchema.getExtends())));
        }

        if (isRequired(consumerSchema) && !isRequired(providerSchema)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "required",
                    consumerSchema.getRequired(),
                    providerSchema.getRequired()));
        }

        if (!equals(consumerSchema.getReadonly(), providerSchema.getReadonly())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "readonly",
                    consumerSchema.getReadonly(),
                    providerSchema.getReadonly()));
        }

        if (!equals(consumerSchema.getDescription(), providerSchema.getDescription())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "description",
                    consumerSchema.getDescription(),
                    providerSchema.getDescription()));
        }

        return errors;
    }

    private boolean isRequired(JsonSchema schema) {
        return schema.getRequired() != null && schema.getRequired();
    }

    boolean equals(Object object1, Object object2) {
        return Objects.equals(object1, object2);
    }

    private <T> boolean arraysEquals(T[] array1, T[] array2) {
        if (array1 == null) {
            return array2 == null;
        }
        if (array2 == null) {
            return false;
        }

        return Arrays.equals(array1, array2);
    }

    private String jsonArrayToString(JsonSchema[] array) {
        if (array == null) {
            return "null";
        }

        int inMax = array.length - 1;
        if (inMax == -1) {
            return "[]";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        for (int i = 0; ; i++) {
            stringBuilder.append(jsonToString(array[i]));
            if (i == inMax) {
                return stringBuilder.append(']').toString();
            }
            stringBuilder.append(", ");
        }
    }

    String jsonToString(JsonSchema object) {
        return "JsonSchema(id=" + object.getId() + ")";
    }
}
