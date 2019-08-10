package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema;

import java.util.List;

public class BooleanSchemaValidator extends ValueTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        return super.validate(consumerSchema, providerSchema);
    }

    @Override
    public Class<?> supportsSchemaType() {
        return BooleanSchema.class;
    }
}
