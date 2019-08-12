package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        if (!arraysEquals(consumerSchema.getDisallow(), providerSchema.getDisallow(), Object::equals)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "disallow",
                    jsonArrayToString(consumerSchema.getDisallow()),
                    jsonArrayToString(providerSchema.getDisallow())));
        }

        if (!arraysEquals(consumerSchema.getExtends(), providerSchema.getExtends(), Object::equals)) {
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

    <T> boolean arraysEquals(T[] array1, T[] array2, BiPredicate<T, T> checker) {
        if (array1 == null) {
            return array2 == null;
        }
        if (array2 == null) {
            return false;
        }

        return array1.length == array2.length && IntStream.range(0, array1.length)
                .allMatch(i -> checker.test(array1[i], array2[i]));
    }

    private String jsonArrayToString(JsonSchema[] array) {
        String content = Arrays.stream(array)
                .map(this::jsonToString)
                .collect(Collectors.joining(", "));

        return "[" + content + "]";
    }

    String jsonToString(JsonSchema object) {
        return "JsonSchema(id=" + object.getId() + ")";
    }

    boolean isEnumValid(Set<String> consumerEnums, Set<String> providerEnums) {
        if (representsString(consumerEnums) && representsEnum(providerEnums)) {
            return false;
        }

        if (representsEnum(consumerEnums) && representsEnum(providerEnums)) {
            return providerEnums.containsAll(consumerEnums);
        }

        return true;
    }

    private boolean representsEnum(Set<String> enums) {
        return enums.size() > 0;
    }

    private boolean representsString(Set<String> enums) {
        return enums.size() == 0;
    }
}
