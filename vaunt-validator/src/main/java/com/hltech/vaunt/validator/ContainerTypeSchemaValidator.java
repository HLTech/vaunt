package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ContainerTypeSchema;

import java.util.List;

public abstract class ContainerTypeSchemaValidator extends SimpleTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        ContainerTypeSchema consumerContainerTypeSchema = consumerSchema.asContainerTypeSchema();
        ContainerTypeSchema providerContainerTypeSchema = providerSchema.asContainerTypeSchema();

        if (!isValidEnum(consumerContainerTypeSchema, providerContainerTypeSchema)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "enums",
                    consumerContainerTypeSchema.getEnums(),
                    providerContainerTypeSchema.getEnums()));
        }

        if (!equals(consumerContainerTypeSchema.getOneOf(), providerContainerTypeSchema.getOneOf())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "oneOf",
                    consumerContainerTypeSchema.getOneOf(),
                    providerContainerTypeSchema.getOneOf()));
        }

        return errors;
    }

    private static boolean isValidEnum(ContainerTypeSchema consumerBody, ContainerTypeSchema providerBody) {
        if (representsString(consumerBody) && representsEnum(providerBody)) {
            return false;
        }

        if (representsEnum(consumerBody) && representsEnum(providerBody)) {
            return providerBody.getEnums().containsAll(consumerBody.getEnums());
        }

        return true;
    }

    private static boolean representsEnum(ContainerTypeSchema body) {
        return body.getEnums().size() > 0;
    }

    private static boolean representsString(ContainerTypeSchema body) {
        return body.getEnums().size() == 0;
    }
}
