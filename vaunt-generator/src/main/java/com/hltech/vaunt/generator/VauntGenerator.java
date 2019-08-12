package com.hltech.vaunt.generator;

import com.hltech.vaunt.core.VauntSerializer;
import com.hltech.vaunt.core.domain.model.Service;
import com.hltech.vaunt.generator.domain.representation.RepresentationExtractor;
import com.hltech.vaunt.generator.domain.representation.RepresentationWriter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Properties;

@RequiredArgsConstructor
public class VauntGenerator {

    private final RepresentationExtractor extractor;
    private final RepresentationWriter writer;

    public VauntGenerator() {
        VauntSerializer serializer = new VauntSerializer();
        extractor = new RepresentationExtractor(serializer);
        writer = new RepresentationWriter(serializer);
    }

    public void writeVauntFile(String packageRoot, String serviceName, String targetDirectory) {
        writeVauntFile(packageRoot, serviceName, targetDirectory, new Properties());
    }

    public void writeVauntFile(String packageRoot, String serviceName, String targetDirectory, Properties props) {
        try {
            writer.writeServiceRepresentation(
                    serviceRepresentation(packageRoot, serviceName, props), targetDirectory);
        } catch (IOException ex) {
            throw new VauntGenerationException("Error when trying to write service representation to file", ex);
        }
    }

    private Service serviceRepresentation(String packageRoot, String serviceName, Properties props) {
        return extractor.extractServiceRepresentation(packageRoot, serviceName, props);
    }
}
