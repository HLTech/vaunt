package com.hltech.vaunt.validator.schema;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;

import java.util.List;
import java.util.Objects;

public abstract class SimpleTypeSchemaValidator extends JsonSchemaValidator {

    @Override
    public List<String> validate(JsonSchema consumerSchema, JsonSchema providerSchema) {
        List<String> errors = super.validate(consumerSchema, providerSchema);

        SimpleTypeSchema consumerSimpleTypeSchema = consumerSchema.asSimpleTypeSchema();
        SimpleTypeSchema providerSimpleTypeSchema = providerSchema.asSimpleTypeSchema();

        if (!equals(consumerSimpleTypeSchema.getDefault(), providerSimpleTypeSchema.getDefault())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "default",
                    consumerSimpleTypeSchema.getDefault(),
                    providerSimpleTypeSchema.getDefault()));
        }

        if (!arraysEquals(consumerSimpleTypeSchema.getLinks(),
                providerSimpleTypeSchema.getLinks(),
                SimpleTypeSchemaValidator::equalsLinkDescriptionObject)) {

            errors.add(String.format(ERROR_FORMAT_SHORT,
                    consumerSchema.getId(),
                    "links"));
        }

        if (!equals(consumerSimpleTypeSchema.getPathStart(), providerSimpleTypeSchema.getPathStart())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "pathStart",
                    consumerSimpleTypeSchema.getPathStart(),
                    providerSimpleTypeSchema.getPathStart()));
        }

        if (!equals(consumerSimpleTypeSchema.getTitle(), providerSimpleTypeSchema.getTitle())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "title",
                    consumerSimpleTypeSchema.getTitle(),
                    providerSimpleTypeSchema.getTitle()));
        }

        return errors;
    }

    private static boolean equalsLinkDescriptionObject(LinkDescriptionObject ob1, LinkDescriptionObject ob2) {
        return Objects.equals(ob1.getHref(), ob2.getHref())
                && Objects.equals(ob1.getRel(), ob2.getRel())
                && Objects.equals(ob1.getTargetSchema(), ob2.getTargetSchema())
                && Objects.equals(ob1.getMethod(), ob2.getMethod())
                && Objects.equals(ob1.getEnctype(), ob2.getEnctype())
                && Objects.equals(ob1.getSchema(), ob2.getSchema())
                && Objects.equals(ob1.getTitle(), ob2.getTitle())
                && Objects.equals(ob1.getMediaType(), ob2.getMediaType());
    }
}
