package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;

import java.util.ArrayList;
import java.util.List;

import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT;

public class ValueTypeSchemaValidator {

    public static List<String> validate(ValueTypeSchema consumerSchema, ValueTypeSchema providerSchema) {
        List<String> errors = new ArrayList<>(SimpleTypeSchemaValidator.validate(consumerSchema, providerSchema));

        if (!isValidEnum(consumerSchema, providerSchema)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "enums",
                    consumerSchema.getEnums(),
                    providerSchema.getEnums()));
        }

        if (!equals(consumerSchema.getFormat(), providerSchema.getFormat())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "format",
                    consumerSchema.getFormat(),
                    providerSchema.getFormat()));
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

    private static boolean isValidEnum(ValueTypeSchema consumerBody, ValueTypeSchema providerBody) {
        if (representsString(consumerBody) && representsEnum(providerBody)) {
            return false;
        }

        if (representsEnum(consumerBody) && representsEnum(providerBody)) {
            return providerBody.getEnums().containsAll(consumerBody.getEnums());
        }

        return true;
    }

    private static boolean representsEnum(ValueTypeSchema body) {
        return body.getEnums().size() > 0;
    }

    private static boolean representsString(ValueTypeSchema body) {
        return body.getEnums().size() == 0;
    }
}
