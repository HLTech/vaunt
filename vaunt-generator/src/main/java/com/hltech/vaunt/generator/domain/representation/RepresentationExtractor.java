package com.hltech.vaunt.generator.domain.representation;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hltech.vaunt.core.VauntSerializer;
import com.hltech.vaunt.core.domain.model.Capabilities;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.core.domain.model.Expectations;
import com.hltech.vaunt.core.domain.model.Service;
import com.hltech.vaunt.generator.domain.representation.annotation.Consumer;
import com.hltech.vaunt.generator.domain.representation.annotation.Consumers;
import com.hltech.vaunt.generator.domain.representation.annotation.Provider;
import com.hltech.vaunt.generator.domain.representation.annotation.Providers;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class RepresentationExtractor {

    private final VauntSerializer serializer;

    public Service extractServiceRepresentation(String packageRoot, String serviceName) throws JsonMappingException {
        return new Service(serviceName, extractCapabilities(packageRoot), extractExpectations(packageRoot));
    }

    private Capabilities extractCapabilities(String packageRoot) {
        List<Contract> providerContracts = new ArrayList<>();

        new Reflections(packageRoot).getTypesAnnotatedWith(Providers.class)
                .forEach(providerMessage -> Arrays.stream(providerMessage.getAnnotation(Providers.class).value())
                        .forEach(providerAnnotation -> providerContracts.add(
                                extractProviderContract(providerMessage, providerAnnotation))));

        new Reflections(packageRoot).getTypesAnnotatedWith(Provider.class)
                .forEach(providerMessage -> providerContracts.add(
                        extractProviderContract(providerMessage, providerMessage.getAnnotation(Provider.class))));

        return new Capabilities(providerContracts);
    }

    private Expectations extractExpectations(String packageRoot) {
        Multimap<String, Contract> providerNameToContracts = ArrayListMultimap.create();

        new Reflections(packageRoot).getTypesAnnotatedWith(Consumers.class)
                .forEach(consumerMessage -> Arrays.stream(consumerMessage.getAnnotation(Consumers.class).value())
                        .forEach(consumerAnnotation -> providerNameToContracts.put(
                                consumerAnnotation.providerName(),
                                extractConsumerContract(consumerMessage, consumerAnnotation))));

        new Reflections(packageRoot).getTypesAnnotatedWith(Consumer.class)
                .forEach(consumerMessage -> providerNameToContracts.put(
                        consumerMessage.getAnnotation(Consumer.class).providerName(),
                        extractConsumerContract(consumerMessage, consumerMessage.getAnnotation(Consumer.class))));

        return new Expectations(providerNameToContracts);
    }

    private Contract extractProviderContract(Class<?> providerMessage, Provider providerAnnotation) {
        try {
            return new Contract(
                    providerAnnotation.destinationType(),
                    providerAnnotation.destinationName(),
                    serializer.generateSchema(providerMessage));
        } catch (JsonMappingException ex) {
            throw new RuntimeException("Unable to extract contract for given provider", ex);
        }
    }

    private Contract extractConsumerContract(Class<?> consumerMessage, Consumer consumerAnnotation) {
        try {
            return new Contract(
                    consumerAnnotation.destinationType(),
                    consumerAnnotation.destinationName(),
                    serializer.generateSchema(consumerMessage));
        } catch (JsonMappingException ex) {
            throw new RuntimeException("Unable to extract contract for given consumer", ex);
        }
    }
}
