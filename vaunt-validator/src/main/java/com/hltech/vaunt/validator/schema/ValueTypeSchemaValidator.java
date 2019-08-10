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

        if (!isValidEnum(consumerValueTypeSchema, providerValueTypeSchema)) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "enums",
                    consumerValueTypeSchema.getEnums(),
                    providerValueTypeSchema.getEnums()));
        }

        if (!equals(consumerValueTypeSchema.getFormat(), providerValueTypeSchema.getFormat())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "format",
                    consumerValueTypeSchema.getFormat(),
                    providerValueTypeSchema.getFormat()));
        }

        return errors;
    }

    private boolean isValidEnum(ValueTypeSchema consumerBody, ValueTypeSchema providerBody) {
        if (representsString(consumerBody) && representsEnum(providerBody)) {
            return false;
        }

        if (representsEnum(consumerBody) && representsEnum(providerBody)) {
            return providerBody.getEnums().containsAll(consumerBody.getEnums());
        }

        return true;
    }

    private boolean representsEnum(ValueTypeSchema body) {
        return body.getEnums().size() > 0;
    }

    private boolean representsString(ValueTypeSchema body) {
        return body.getEnums().size() == 0;
    }
}
