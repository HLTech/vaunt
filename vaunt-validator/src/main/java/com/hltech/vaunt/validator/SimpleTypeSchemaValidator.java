package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT;
import static com.hltech.vaunt.validator.JsonSchemaValidator.ERROR_FORMAT_SHORT;

public class SimpleTypeSchemaValidator {

    public static List<String> validate(SimpleTypeSchema consumerSchema, SimpleTypeSchema providerSchema) {
        List<String> errors = new ArrayList<>(JsonSchemaValidator.validate(consumerSchema, providerSchema));

        if (!equals(consumerSchema.getDefault(), providerSchema.getDefault())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "default",
                    consumerSchema.getDefault(),
                    providerSchema.getDefault()));
        }

        if (!arraysEquals(consumerSchema.getLinks(), providerSchema.getLinks())) {
            errors.add(String.format(ERROR_FORMAT_SHORT,
                    consumerSchema.getId(),
                    "links"));
        }

        if (!equals(consumerSchema.getPathStart(), providerSchema.getPathStart())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "pathStart",
                    consumerSchema.getPathStart(),
                    providerSchema.getPathStart()));
        }

        if (!equals(consumerSchema.getTitle(), providerSchema.getTitle())) {
            errors.add(String.format(ERROR_FORMAT,
                    consumerSchema.getId(),
                    "title",
                    consumerSchema.getTitle(),
                    providerSchema.getTitle()));
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

    private static boolean arraysEquals(LinkDescriptionObject[] array1, LinkDescriptionObject[] array2) {
        if (array1 == null) {
            return array2 == null;
        }
        if (array2 == null) {
            return false;
        }
        int len = array1.length;
        if (len != array2.length) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            LinkDescriptionObject ob1 = array1[i];
            LinkDescriptionObject ob2 = array2[i];

            if (!equalsLinkDescriptionObject(ob1, ob2)) {
                return false;
            }
        }
        return true;
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
