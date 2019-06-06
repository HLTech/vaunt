package com.hltech.vaunt.core.domain.model;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.hltech.vaunt.core.VauntSerializer;
import lombok.Data;

@Data
public class Contract {
    private final DestinationType destinationType;
    private final String destinationName;
    private final JsonSchema body;

    @Override
    public String toString() {
        return "Contract(destinationType=" + destinationType.toString()
                + ", destinationName=" + destinationName
                + ", body =" + new VauntSerializer().serializeSchema(body);
    }
}
