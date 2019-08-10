package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.core.domain.model.Service;

import java.util.List;
import java.util.stream.Collectors;

public class VauntValidator {

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
            return ValidationResult.failure(consumerContract, ValidationError.MISSING_ENDPOINT);
        }

        List<Contract> idMatchingContracts = endpointMatchingContracts.stream()
                .filter(providerContract -> isIdMatching(consumerContract.getBody(), providerContract.getBody()))
                .collect(Collectors.toList());

        if (idMatchingContracts.isEmpty()) {
            return ValidationResult.failure(consumerContract, ValidationError.MISSING_MESSAGE_WITH_NAME);
        }

        if (idMatchingContracts.size() > 1) {
            return ValidationResult.failure(consumerContract, ValidationError.DUPLICATE_MATCH);
        }

        return isContentMatching(consumerContract.getBody(),
                idMatchingContracts.get(0).getBody()) ? ValidationResult.success(consumerContract)
                : ValidationResult.failure(consumerContract, idMatchingContracts.get(0), ValidationError.WRONG_SCHEMA);
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

    private boolean isContentMatching(JsonSchema consumerBody, JsonSchema providerBody) {
        if (isStringSchema(consumerBody) && isStringSchema(providerBody)) {
            return VauntSchemaValidator.validate(consumerBody, providerBody).size() == 0;
        }

        if (isObjectSchema(consumerBody) && isObjectSchema(providerBody)) {
            return VauntSchemaValidator.validate(consumerBody, providerBody).size() == 0;
        }

        return consumerBody.equals(providerBody);
    }

    private boolean isObjectSchema(JsonSchema schema) {
        return schema.asObjectSchema() != null;
    }

    private boolean isStringSchema(JsonSchema schema) {
        return schema.asStringSchema() != null;
    }
}
