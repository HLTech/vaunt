package com.hltech.vaunt.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import java.io.File;
import java.io.IOException;

public class VauntSerializer {

    private final ObjectMapper mapper;
    private final JsonSchemaGenerator generator;

    public VauntSerializer() {
        mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new JavaTimeModule());

        generator = new JsonSchemaGenerator(mapper);
    }

    public JsonSchema generateSchema(Class<?> type) throws JsonMappingException {
        return generator.generateSchema(type);
    }

    public String serializeSchema(JsonSchema schema) {
        try {
            return mapper.writeValueAsString(schema);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return ""; //TODO: handle
    }

    public void serializeToFile(File resultFile, Object value) throws IOException {
        mapper.writeValue(resultFile, value);
    }
}
