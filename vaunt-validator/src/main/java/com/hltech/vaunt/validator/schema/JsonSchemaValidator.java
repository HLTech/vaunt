package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        if (!isValid(consumerSchema.get$ref(), providerSchema.get$ref())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "$ref",
                    consumerSchema.get$ref(),
                    providerSchema.get$ref()));
        }

        if (!isValid(consumerSchema.get$schema(), providerSchema.get$schema())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "$schema",
                    consumerSchema.get$schema(),
                    providerSchema.get$schema()));
        }

        if (!isArrayValid(consumerSchema.getDisallow(), providerSchema.getDisallow(), Object::equals)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "disallow",
                    jsonArrayToString(consumerSchema.getDisallow()),
                    jsonArrayToString(providerSchema.getDisallow())));
        }

        if (!isArrayValid(consumerSchema.getExtends(), providerSchema.getExtends(), Object::equals)) {
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

        if (!isValid(consumerSchema.getReadonly(), providerSchema.getReadonly())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "readonly",
                    consumerSchema.getReadonly(),
                    providerSchema.getReadonly()));
        }

        if (!isValid(consumerSchema.getDescription(), providerSchema.getDescription())) {
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

    boolean isValid(Object consumerObject, Object providerObject) {
        return consumerObject == null || Objects.equals(consumerObject, providerObject);
    }

    <T> boolean isArrayValid(T[] consumerArray, T[] providerArray, BiPredicate<T, T> checker) {
        if (consumerArray == null) {
            return true;
        }

        if (providerArray == null) {
            return false;
        }

        return consumerArray.length == providerArray.length && IntStream.range(0, consumerArray.length)
                .allMatch(i -> checker.test(consumerArray[i], providerArray[i]));
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

    <T> boolean isEnumValid(Set<T> consumerEnums, Set<T> providerEnums) {
        if (representsString(consumerEnums) && representsEnum(providerEnums)) {
            return false;
        }

        if (representsEnum(consumerEnums) && representsEnum(providerEnums)) {
            return providerEnums.containsAll(consumerEnums);
        }

        return true;
    }

    <T, S> boolean isMapValid(Map<T, S> consumerMap, Map<T, S> providerMap) {
        if (consumerMap.size() == 0) {
            return true;
        }

        if (providerMap.size() == 0) {
            return false;
        }

        return consumerMap.equals(providerMap);
    }

    private <T> boolean representsEnum(Set<T> enums) {
        return enums.size() > 0;
    }

    private <T> boolean representsString(Set<T> enums) {
        return enums.size() == 0;
    }
}
