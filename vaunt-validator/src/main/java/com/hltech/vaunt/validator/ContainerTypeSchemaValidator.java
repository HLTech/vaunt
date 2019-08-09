package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.types.ContainerTypeSchema;

import java.util.ArrayList;
import java.util.List;

import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT;

public class ContainerTypeSchemaValidator {

    public static List<String> validate(ContainerTypeSchema consumerSchema, ContainerTypeSchema providerSchema) {
        List<String> errors = new ArrayList<>(SimpleTypeSchemaValidator.validate(consumerSchema, providerSchema));

        if (!isValidEnum(consumerSchema, providerSchema)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "enums",
                    consumerSchema.getEnums(),
                    providerSchema.getEnums()));
        }

        if (!equals(consumerSchema.getOneOf(), providerSchema.getOneOf())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "oneOf",
                    consumerSchema.getOneOf(),
                    providerSchema.getOneOf()));
        }

        return errors;
    }

    private static boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        } else {
            return object1.equals(object2);
        }
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
