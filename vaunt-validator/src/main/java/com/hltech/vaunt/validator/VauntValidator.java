package com.hltech.vaunt.validator;

import com.hltech.vaunt.core.domain.model.Contract;
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
        Optional<Contract> matchingProviderContract = providerContracts.stream()
                .filter(providerContract -> isEndpointMatching(consumerContract, providerContract))
                .findFirst();

        if (!matchingProviderContract.isPresent()) {
            return ValidationResult.failure(ValidationError.MISSING_ENDPOINT);
        }

        if (!consumerContract.getBody().equals(matchingProviderContract.get().getBody())) {
            return ValidationResult.failure(ValidationError.WRONG_SCHEMA);
        }

        return ValidationResult.success();
    }

    private boolean isEndpointMatching(Contract firstContract, Contract secondContract) {
        return firstContract.getDestinationType().equals(secondContract.getDestinationType())
                && firstContract.getDestinationName().equals(secondContract.getDestinationName());
    }

}
