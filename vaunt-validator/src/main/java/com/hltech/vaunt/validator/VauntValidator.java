package com.hltech.vaunt.validator;

import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.core.domain.model.Service;

import java.util.List;
import java.util.Optional;
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

    private ValidationResult validateWithMatchingProviderContract(
            Contract consumerContract, List<Contract> providerContracts) {
        List<Contract> contracts = providerContracts.stream()
                .filter(providerContract -> isEndpointMatching(consumerContract, providerContract))
                .collect(Collectors.toList());

        if (contracts.isEmpty()) {
            return ValidationResult.failure(consumerContract, ValidationError.MISSING_ENDPOINT);
        }

        Optional<Contract> matchingProviderContract = contracts.stream()
                .filter(providerContract -> isSchemaMatching(consumerContract, providerContract))
                .findFirst();

        return matchingProviderContract
                .map(contract -> ValidationResult.success(consumerContract, contract))
                .orElseGet(() -> ValidationResult.failure(consumerContract, contracts, ValidationError.WRONG_SCHEMA));

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

    private boolean isSchemaMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getBody().equals(secondContract.getBody());
    }

}
