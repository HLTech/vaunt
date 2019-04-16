package com.hltech.vaunt.generator.domain.representation;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hltech.vaunt.generator.domain.representation.annotation.Consumer;
import com.hltech.vaunt.generator.domain.representation.annotation.Provider;
import com.hltech.vaunt.generator.domain.representation.model.Capabilities;
import com.hltech.vaunt.generator.domain.representation.model.Contract;
import com.hltech.vaunt.generator.domain.representation.model.Expectations;
import com.hltech.vaunt.generator.domain.representation.model.Service;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RepresentationExtractor {

    private final JsonSchemaGenerator jsonSchemaGenerator;

    public Service extractServiceRepresentation(String packageRoot, String serviceName) throws JsonMappingException {
        return new Service(serviceName, extractCapabilities(packageRoot), extractExpectations(packageRoot));
    }

    private Capabilities extractCapabilities(String packageRoot) throws JsonMappingException {
        List<Contract> providerContracts = new ArrayList<>();
        for (Class<?> providerMessage : new Reflections(packageRoot).getTypesAnnotatedWith(Provider.class)) {
            providerContracts.add(new Contract(
                    providerMessage.getAnnotation(Provider.class).destinationType(),
                    providerMessage.getAnnotation(Provider.class).destinationName(),
                    jsonSchemaGenerator.generateSchema(providerMessage)));
        }

        return new Capabilities(providerContracts);
    }

    private Expectations extractExpectations(String packageRoot) throws JsonMappingException {
        Multimap<String, Contract> providerNameToContracts = ArrayListMultimap.create();
        for (Class<?> consumerMessage : new Reflections(packageRoot).getTypesAnnotatedWith(Consumer.class)) {
            Contract contract = new Contract(
                    consumerMessage.getAnnotation(Consumer.class).destinationType(),
                    consumerMessage.getAnnotation(Consumer.class).destinationName(),
                    jsonSchemaGenerator.generateSchema(consumerMessage));

            providerNameToContracts.put(consumerMessage.getAnnotation(Consumer.class).providerName(), contract);
        }

        return new Expectations(providerNameToContracts);
    }
}
