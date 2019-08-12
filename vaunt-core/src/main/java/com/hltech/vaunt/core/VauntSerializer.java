package com.hltech.vaunt.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.customProperties.ValidationSchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext;
import com.hltech.vaunt.core.domain.model.Contract;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class VauntSerializer {

    private final ObjectMapper mapper;
    private final SchemaFactoryWrapper wrapper;
    private final JsonSchemaGenerator generator;

    public VauntSerializer() {
        mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new JavaTimeModule());

        wrapper = new ValidationSchemaFactoryWrapper();
        generator = new JsonSchemaGenerator(mapper, wrapper);
    }

    public JsonSchema generateSchema(Class<?> type) throws JsonMappingException {
        wrapper.setVisitorContext(new VauntVisitorContext());
        return generator.generateSchema(type);
    }

    public String serializeSchema(JsonSchema schema) {
        try {
            return mapper.writeValueAsString(schema);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            throw new VauntSerializationException("Error during schema serialization", ex);
        }
    }

    public List<Contract> parseContracts(String contracts) {
        try {
            return mapper.readValue(contracts, new TypeReference<List<Contract>>(){});
        } catch (IOException ex) {
            throw new VauntSerializationException("Error during parsing contracts", ex);
        }
    }

    public void serializeToFile(File resultFile, Object value) {
        try {
            mapper.writeValue(resultFile, value);
        } catch (IOException ex) {
            throw new VauntSerializationException("Error during serialization to file", ex);
        }
    }

    class VauntVisitorContext extends VisitorContext {

        private final HashSet<JavaType> seenSchemas = new HashSet<>();

        @Override
        public String addSeenSchemaUri(JavaType seenSchema) {
            if (seenSchema != null && !seenSchema.isPrimitive()) {
                seenSchemas.add(seenSchema);
                return javaTypeToUrn(seenSchema);
            }
            return null;
        }

        @Override
        public String getSeenSchemaUri(JavaType seenSchema) {
            if (seenSchema.isTypeOrSubTypeOf(JsonSchema.class)) {
                return (seenSchemas.contains(seenSchema)) ? javaTypeToUrn(seenSchema) : null;
            }
            return null;
        }

        @Override
        public String javaTypeToUrn(JavaType jt) {
            return jt.getRawClass().getSimpleName();
        }
    }
}
