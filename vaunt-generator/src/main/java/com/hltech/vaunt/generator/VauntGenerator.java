package com.hltech.vaunt.generator;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.hltech.vaunt.core.domain.model.Service;
import com.hltech.vaunt.generator.domain.representation.RepresentationExtractor;
import com.hltech.vaunt.generator.domain.representation.RepresentationWriter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class VauntGenerator {

    private final RepresentationExtractor extractor;
    private final RepresentationWriter writer;

    public VauntGenerator() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);

        extractor = new RepresentationExtractor(generator);
        writer = new RepresentationWriter(mapper);
    }

    public void writeVauntFile(String packageRoot, String serviceName, String targetDirectory) {
        try {
            writer.writeServiceRepresentation(serviceRepresentation(packageRoot, serviceName), targetDirectory);
        } catch (IOException ex) {
            throw new RuntimeException("Error when trying to write service representation to file", ex);
        }
    }

    private Service serviceRepresentation(String packageRoot, String serviceName) {
        try {
            return extractor.extractServiceRepresentation(packageRoot, serviceName);
        } catch (JsonMappingException ex) {
            throw new RuntimeException(
                    String.format(
                            "Error when trying to extract service representation: package=%s, service name=%s",
                            packageRoot, serviceName), ex);
        }
    }
}
