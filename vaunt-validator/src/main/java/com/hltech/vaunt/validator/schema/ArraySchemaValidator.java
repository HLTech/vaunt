package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;

import java.util.List;

public class ArraySchemaValidator extends ContainerTypeSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        ArraySchema consumerIntegerSchema = consumerSchema.asArraySchema();
        ArraySchema providerIntegerSchema = providerSchema.asArraySchema();

        if (!isValid(consumerIntegerSchema.getAdditionalItems(), providerIntegerSchema.getAdditionalItems())) {
            errors.add(String.format(ERROR_FORMAT_SHORT,
                    consumerIntegerSchema.getId(),
                    "additionalItems"));
        }

        if (!isValid(consumerIntegerSchema.getItems(), providerIntegerSchema.getItems())) {
            errors.add(String.format(ERROR_FORMAT_SHORT,
                    consumerIntegerSchema.getId(),
                    "items"));
        }

        if (!isValid(consumerIntegerSchema.getMaxItems(), providerIntegerSchema.getMaxItems())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerIntegerSchema.getId(),
                    "maxItems",
                    consumerIntegerSchema.getMaxItems(),
                    providerIntegerSchema.getMaxItems()));
        }

        if (!isValid(consumerIntegerSchema.getMinItems(), providerIntegerSchema.getMinItems())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerIntegerSchema.getId(),
                    "minItems",
                    consumerIntegerSchema.getMinItems(),
                    providerIntegerSchema.getMinItems()));
        }

        if (!isValid(consumerIntegerSchema.getUniqueItems(), providerIntegerSchema.getUniqueItems())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerIntegerSchema.getId(),
                    "uniqueItems",
                    consumerIntegerSchema.getUniqueItems(),
                    providerIntegerSchema.getUniqueItems()));
        }

        return errors;
    }

    @Override
    public Class<?> supportsSchemaType() {
        return ArraySchema.class;
    }
}
