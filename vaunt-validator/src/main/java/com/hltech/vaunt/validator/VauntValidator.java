package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ContainerTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.core.domain.model.Service;

import java.util.List;
import java.util.Map;
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
            return StringSchemaValidator.validate(
                    consumerBody.asStringSchema(), providerBody.asStringSchema()).size() == 0;
        }

        if (isObjectSchema(consumerBody) && isObjectSchema(providerBody)) {
            return compareObjectSchema(consumerBody.asObjectSchema(), providerBody.asObjectSchema());
        }

        return consumerBody.equals(providerBody);
    }

    private boolean isRequired(JsonSchema schema) {
        return schema.getRequired() != null && schema.getRequired();
    }

    private boolean isObjectSchema(JsonSchema schema) {
        return schema.asObjectSchema() != null;
    }

    private boolean isStringSchema(JsonSchema schema) {
        return schema.asStringSchema() != null;
    }

    private boolean compareSimpleTypeSchemaPart(SimpleTypeSchema consumerBody, SimpleTypeSchema providerBody) {
        return equals(consumerBody.getDefault(), providerBody.getDefault())
                && equals(consumerBody.getTitle(), providerBody.getTitle())
                && equals(consumerBody.getPathStart(), providerBody.getPathStart())
                && equals(consumerBody.getLinks(), providerBody.getLinks());
    }

    private boolean compareJsonSchemaPart(JsonSchema consumerBody, JsonSchema providerBody) {
        return equals(consumerBody.getId(), providerBody.getId())
                && !isRequired(consumerBody) || isRequired(providerBody)
                && equals(consumerBody.getReadonly(), providerBody.getReadonly())
                && equals(consumerBody.get$ref(), providerBody.get$ref())
                && equals(consumerBody.get$schema(), providerBody.get$schema())
                && equals(consumerBody.getDisallow(), providerBody.getDisallow())
                && equals(consumerBody.getExtends(), providerBody.getExtends());
    }

    private boolean compareContainerTypeSchemaPart(ContainerTypeSchema consumerBody, ContainerTypeSchema providerBody) {
        return equals(consumerBody.getEnums(), providerBody.getEnums())
                && equals(consumerBody.getOneOf(), providerBody.getOneOf());
    }

    private boolean compareObjectSchema(ObjectSchema consumerBody, ObjectSchema providerBody) {
        return equals(consumerBody.getAdditionalProperties(), providerBody.getAdditionalProperties())
                && equals(consumerBody.getDependencies(), providerBody.getDependencies())
                && equals(consumerBody.getPatternProperties(), providerBody.getPatternProperties())
                && compareContainerTypeSchemaPart(consumerBody, providerBody)
                && compareSimpleTypeSchemaPart(consumerBody, providerBody)
                && compareJsonSchemaPart(consumerBody, providerBody)
                && compareObjectProperties(consumerBody.getProperties(), providerBody.getProperties());
    }

    private boolean compareObjectProperties(Map<String, JsonSchema> consumerProperties,
                                            Map<String, JsonSchema> providerProperties) {

        return providerProperties.keySet().containsAll(consumerProperties.keySet())
                && consumerProperties.keySet().stream()
                .allMatch(key -> isContentMatching(consumerProperties.get(key), providerProperties.get(key)));
    }

    private boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        } else {
            return object1.equals(object2);
        }
    }
}
