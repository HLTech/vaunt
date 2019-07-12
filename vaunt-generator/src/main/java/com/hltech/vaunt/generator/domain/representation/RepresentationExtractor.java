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
import java.util.Properties;

@RequiredArgsConstructor
public class RepresentationExtractor {

    private final VauntSerializer serializer;

    public Service extractServiceRepresentation(String packageRoot, String serviceName, Properties props)
            throws JsonMappingException {
        return new Service(serviceName,
                extractCapabilities(packageRoot, props),
                extractExpectations(packageRoot, props));
    }

    private Capabilities extractCapabilities(String packageRoot, Properties props) {
        List<Contract> providerContracts = new ArrayList<>();

        new Reflections(packageRoot).getTypesAnnotatedWith(Providers.class)
                .forEach(providerMessage -> Arrays.stream(providerMessage.getAnnotation(Providers.class).value())
                        .forEach(providerAnnotation -> providerContracts.add(
                                extractProviderContract(providerMessage, providerAnnotation, props))));

        new Reflections(packageRoot).getTypesAnnotatedWith(Provider.class)
                .forEach(providerMessage -> providerContracts.add(extractProviderContract(
                        providerMessage, providerMessage.getAnnotation(Provider.class), props)));

        return new Capabilities(providerContracts);
    }

    private Expectations extractExpectations(String packageRoot, Properties props) {
        Multimap<String, Contract> providerNameToContracts = ArrayListMultimap.create();

        new Reflections(packageRoot).getTypesAnnotatedWith(Consumers.class)
                .forEach(consumerMessage -> Arrays.stream(consumerMessage.getAnnotation(Consumers.class).value())
                        .forEach(consumerAnnotation -> providerNameToContracts.put(
                                consumerAnnotation.providerName(),
                                extractConsumerContract(consumerMessage, consumerAnnotation, props))));

        new Reflections(packageRoot).getTypesAnnotatedWith(Consumer.class)
                .forEach(consumerMessage -> providerNameToContracts.put(
                        consumerMessage.getAnnotation(Consumer.class).providerName(), extractConsumerContract(
                                consumerMessage, consumerMessage.getAnnotation(Consumer.class), props)));

        return new Expectations(providerNameToContracts);
    }

    private Contract extractProviderContract(Class<?> providerMessage, Provider providerAnnotation, Properties props) {
        try {
            return new Contract(
                    providerAnnotation.destinationType(),
                    props.getProperty(providerAnnotation.destinationName(), providerAnnotation.destinationName()),
                    serializer.generateSchema(providerMessage));
        } catch (JsonMappingException ex) {
            throw new RuntimeException("Unable to extract contract for given provider", ex);
        }
    }

    private Contract extractConsumerContract(Class<?> consumerMessage, Consumer consumerAnnotation, Properties props) {
        try {
            return new Contract(
                    consumerAnnotation.destinationType(),
                    props.getProperty(consumerAnnotation.destinationName(), consumerAnnotation.destinationName()),
                    serializer.generateSchema(consumerMessage));
        } catch (JsonMappingException ex) {
            throw new RuntimeException("Unable to extract contract for given consumer", ex);
        }
    }
}
