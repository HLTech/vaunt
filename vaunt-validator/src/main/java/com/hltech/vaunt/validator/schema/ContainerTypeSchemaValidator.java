package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ContainerTypeSchema;

import java.util.List;

public abstract class ContainerTypeSchemaValidator extends SimpleTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        ContainerTypeSchema consumerContainerTypeSchema = consumerSchema.asContainerTypeSchema();
        ContainerTypeSchema providerContainerTypeSchema = providerSchema.asContainerTypeSchema();

        if (!isEnumValid(consumerContainerTypeSchema.getEnums(), providerContainerTypeSchema.getEnums())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "enums",
                    consumerContainerTypeSchema.getEnums(),
                    providerContainerTypeSchema.getEnums()));
        }

        if (!isEnumValid(consumerContainerTypeSchema.getOneOf(), providerContainerTypeSchema.getOneOf())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "oneOf",
                    consumerContainerTypeSchema.getOneOf(),
                    providerContainerTypeSchema.getOneOf()));
        }

        return errors;
    }
}
