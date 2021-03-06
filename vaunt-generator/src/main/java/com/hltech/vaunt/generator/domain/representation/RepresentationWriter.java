package com.hltech.vaunt.generator.domain.representation;

import com.hltech.vaunt.core.VauntSerializer;
import com.hltech.vaunt.core.domain.model.Service;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class RepresentationWriter {

    private final VauntSerializer serializer;

    public void writeServiceRepresentation(Service service, String targetDirectory) throws IOException {
        File targetDir = Paths.get(targetDirectory).toFile();
        if (targetDir != null && !targetDir.exists()) {
            targetDir.mkdirs();
        }

        File targetFile = Paths.get(targetDirectory).resolve(service.getName().concat(".json")).toFile();
        serializer.serializeToFile(targetFile, service);
    }
}
