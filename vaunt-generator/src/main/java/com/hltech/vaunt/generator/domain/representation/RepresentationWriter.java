package com.hltech.vaunt.generator.domain.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hltech.vaunt.generator.domain.representation.model.Service;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class RepresentationWriter {

    private final ObjectMapper mapper;

    public void writeServiceRepresentation(Service service, String targetDirectory) throws IOException {
        File targetFile = Paths.get(targetDirectory).resolve(service.getName().concat(".json")).toFile();
        mapper.writeValue(targetFile, service);
    }
}
