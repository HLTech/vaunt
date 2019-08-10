package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.google.common.collect.Lists;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.core.domain.model.Service;
import com.hltech.vaunt.validator.schema.SchemaValidator;

import java.util.List;
import java.util.stream.Collectors;

public class VauntValidator {
    private static final String MISSING_ENDPOINT = "Missing endpoint required by consumer";
    private static final String MISSING_MESSAGE_WITH_ID = "Missing message with given id required by consumer";
    private static final String DUPLICATE_MATCH = "More than one message with the same id for given endpoint";

    public List<ValidationResult> validate(Service consumer, Service provider) {
        return consumer.getExpectations().getProviderNameToContracts().get(provider.getName()).stream()
                .map(consumerContract -> validateWithMatchingProviderContract(
                        consumerContract, provider.getCapabilities().getContracts()))
                .collect(Collectors.toList());
    }

    public List<ValidationResult> validate(List<Contract> expectations, List<Contract> capabilities) {
        return expectations.stream()
                .map(consumerContract -> validateWithMatchingProviderContract(consumerContract, capabilities))
                .collect(Collectors.toList());
    }

    private ValidationResult validateWithMatchingProviderContract(Contract consumerContract,
                                                                  List<Contract> providerContracts) {
        List<Contract> endpointMatchingContracts = providerContracts.stream()
                .filter(providerContract -> isEndpointMatching(consumerContract, providerContract))
                .collect(Collectors.toList());

        if (endpointMatchingContracts.isEmpty()) {
            return ValidationResult.failure(
                    consumerContract.toString(), Lists.newArrayList(MISSING_ENDPOINT));
        }

        List<Contract> idMatchingContracts = endpointMatchingContracts.stream()
                .filter(providerContract -> isIdMatching(consumerContract.getMessage(), providerContract.getMessage()))
                .collect(Collectors.toList());

        if (idMatchingContracts.isEmpty()) {
            return ValidationResult.failure(
                    consumerContract.toString(),
                    Lists.newArrayList(MISSING_MESSAGE_WITH_ID));
        }

        if (idMatchingContracts.size() > 1) {
            return ValidationResult.failure(
                    consumerContract.toString(),
                    Lists.newArrayList(DUPLICATE_MATCH));
        }

        List<String> validationErrors =
                validateSchema(consumerContract.getMessage(), idMatchingContracts.get(0).getMessage());

        return validationErrors.size() == 0
                ? ValidationResult.success(consumerContract.toString())
                : ValidationResult.failure(consumerContract.toString(), validationErrors);
    }

    private boolean isEndpointMatching(Contract firstContract, Contract secondContract) {
        return isTmpQueueMatching(firstContract, secondContract)
                || (isDstTypeMatching(firstContract, secondContract)
                && isDstNameMatching(firstContract, secondContract));
    }

    private boolean isTmpQueueMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getDestinationType() == DestinationType.TEMPORARY_QUEUE
                && isDstTypeMatching(firstContract, secondContract);
    }

    private boolean isDstTypeMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getDestinationType() == secondContract.getDestinationType();
    }

    private boolean isDstNameMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getDestinationName().equals(secondContract.getDestinationName());
    }

    private boolean isIdMatching(JsonSchema consumerBody, JsonSchema providerBody) {
        return consumerBody.getId().equals(providerBody.getId());
    }

    private List<String> validateSchema(JsonSchema consumerBody, JsonSchema providerBody) {
        return SchemaValidator.validate(consumerBody, providerBody);
    }
}
