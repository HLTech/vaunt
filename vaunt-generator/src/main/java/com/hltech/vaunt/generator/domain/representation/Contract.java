package com.hltech.vaunt.generator.domain.representation;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import lombok.Data;

@Data
public class Contract {
    private final DestinationType destinationType;
    private final String destinationName;
    private final JsonSchema body;
}