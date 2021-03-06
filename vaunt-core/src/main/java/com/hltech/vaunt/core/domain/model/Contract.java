package com.hltech.vaunt.core.domain.model;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.Data;

@Data
public class Contract {
    private final DestinationType destinationType;
    private final String destinationName;
    private final JsonSchema message;

    @Override
    public String toString() {
        return "Contract(destinationType = " + destinationType.toString()
                + ", destinationName = \"" + destinationName + "\""
                + ", messageId = \"" + message.getId() + "\""
                + ")";
    }
}
