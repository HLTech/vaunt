package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class VauntSchemaValidator {

    public static List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {

        if (consumerSchema.isBooleanSchema() && providerSchema.isBooleanSchema()) {
            return new ArrayList<>();
        }

        if (consumerSchema.isIntegerSchema() && providerSchema.isIntegerSchema()) {
            return new ArrayList<>();
        }

        if (consumerSchema.isNumberSchema() && providerSchema.isNumberSchema()) {
            return new ArrayList<>();
        }

        if (consumerSchema.isStringSchema() && providerSchema.isStringSchema()) {
            return StringSchemaValidator.validate(consumerSchema.asStringSchema(), providerSchema.asStringSchema());
        }

        if (consumerSchema.isArraySchema() && providerSchema.isArraySchema()) {
            return new ArrayList<>();
        }

        if (consumerSchema.isObjectSchema() && providerSchema.isObjectSchema()) {
            return ObjectSchemaValidator.validate(consumerSchema.asObjectSchema(), providerSchema.asObjectSchema());
        }

        return Lists.newArrayList("error");
    }
}
