package com.hltech.vaunt.generator;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hltech.vaunt.generator.domain.representation.RepresentationExtractor;
import com.hltech.vaunt.generator.domain.representation.model.Service;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class VauntGenerator {

    private final RepresentationExtractor extractor;
    private final ObjectMapper mapper;

    public void writeVauntFile(String packageRoot, String serviceName, String targetDirectory) {
        write(serviceRepresentation(packageRoot, serviceName), targetFile(serviceName, targetDirectory));
    }

    private File targetFile(String serviceName, String targetDirectory) {
        return Paths.get(targetDirectory).resolve(serviceName.concat(".json")).toFile();
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

    private void write(Service service, File targetFile) {
        try {
            mapper.writeValue(targetFile, service);
        } catch (IOException ex) {
            throw new RuntimeException("Error when trying to write vaunt file: " + targetFile.getAbsolutePath(), ex);
        }
    }
}
