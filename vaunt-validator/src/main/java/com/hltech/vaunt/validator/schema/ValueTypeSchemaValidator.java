package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;

import java.util.List;

public abstract class ValueTypeSchemaValidator extends SimpleTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        ValueTypeSchema consumerValueTypeSchema = consumerSchema.asValueTypeSchema();
        ValueTypeSchema providerValueTypeSchema = providerSchema.asValueTypeSchema();

        if (!isEnumValid(consumerValueTypeSchema.getEnums(), providerValueTypeSchema.getEnums())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "enums",
                    consumerValueTypeSchema.getEnums(),
                    providerValueTypeSchema.getEnums()));
        }

        if (!isValid(consumerValueTypeSchema.getFormat(), providerValueTypeSchema.getFormat())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "format",
                    consumerValueTypeSchema.getFormat(),
                    providerValueTypeSchema.getFormat()));
        }

        return errors;
    }
}
