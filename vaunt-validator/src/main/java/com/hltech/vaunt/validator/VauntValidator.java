package com.hltech.vaunt.validator;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.module.jsonSchema.types.SimpleTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import com.hltech.vaunt.core.domain.model.Contract;
import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.core.domain.model.Service;

import java.util.List;
import java.util.Map;
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

    private ValidationResult validateWithMatchingProviderContract(Contract consumerContract,
                                                                  List<Contract> providerContracts) {
        List<Contract> contracts = providerContracts.stream()
                .filter(providerContract -> isEndpointMatching(consumerContract, providerContract))
                .collect(Collectors.toList());

        if (contracts.isEmpty()) {
            return ValidationResult.failure(consumerContract, ValidationError.MISSING_ENDPOINT);
        }

        Optional<Contract> matchingProviderContract = contracts.stream()
                .filter(providerContract -> isSchemaAndIdMatching(consumerContract.getDestinationType(),
                        consumerContract.getBody(), providerContract.getBody()))
                .findFirst();

        return matchingProviderContract
                .map(contract -> ValidationResult.success(consumerContract))
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

    private boolean isSchemaAndIdMatching(DestinationType dstType, JsonSchema consumerBody, JsonSchema providerBody) {
        if (!consumerBody.getId().equals(providerBody.getId())) {
            return false;
        }

        return isSchemaMatching(dstType, consumerBody, providerBody);
    }

    private boolean isSchemaMatching(DestinationType dstType, JsonSchema consumerBody, JsonSchema providerBody) {
        if (isStringSchema(consumerBody) && isStringSchema(providerBody)) {
            return compareEnumSchema(dstType, consumerBody, providerBody);
        }

        if (isObjectSchema(consumerBody) && isObjectSchema(providerBody)) {
            return compareObjectSchema(dstType, consumerBody.asObjectSchema(), providerBody.asObjectSchema());
        }

        return consumerBody.equals(providerBody);
    }

    private boolean isObjectSchema(JsonSchema schema) {
        return schema.asObjectSchema() != null;
    }

    private boolean isStringSchema(JsonSchema schema) {
        return schema.asStringSchema() != null;
    }

    private boolean compareEnumSchema(DestinationType dstType, JsonSchema consumerBody, JsonSchema providerBody) {
        return compareStringSchemaPart(dstType, consumerBody.asStringSchema(), providerBody.asStringSchema())
                && compareSimpleTypeSchemaPart(consumerBody.asSimpleTypeSchema(), providerBody.asSimpleTypeSchema())
                && compareJsonSchemaPart(consumerBody, providerBody);
    }

    private boolean compareStringSchemaPart(DestinationType dstType,
                                            StringSchema consumerBody,
                                            StringSchema providerBody) {
        return equals(consumerBody.getMaxLength(), providerBody.getMaxLength())
                && equals(consumerBody.getMinLength(), providerBody.getMinLength())
                && equals(consumerBody.getPattern(), providerBody.getPattern())
                && equals(consumerBody.getFormat(), providerBody.getFormat())
                && compareEnum(dstType, consumerBody, providerBody);
    }

    private boolean compareSimpleTypeSchemaPart(SimpleTypeSchema consumerBody, SimpleTypeSchema providerBody) {
        return equals(consumerBody.getDefault(), providerBody.getDefault())
                && equals(consumerBody.getTitle(), providerBody.getTitle())
                && equals(consumerBody.getPathStart(), providerBody.getPathStart())
                && equals(consumerBody.getLinks(), providerBody.getLinks());
    }

    private boolean compareJsonSchemaPart(JsonSchema consumerBody, JsonSchema providerBody) {
        return equals(consumerBody.getId(), providerBody.getId())
                && equals(consumerBody.getRequired(), providerBody.getRequired())
                && equals(consumerBody.getReadonly(), providerBody.getReadonly())
                && equals(consumerBody.get$ref(), providerBody.get$ref())
                && equals(consumerBody.get$schema(), providerBody.get$schema())
                && equals(consumerBody.getDisallow(), providerBody.getDisallow())
                && equals(consumerBody.getExtends(), providerBody.getExtends());
    }

    private boolean compareEnum(DestinationType dstType, StringSchema consumerBody, StringSchema providerBody) {
        switch (dstType) {
            case QUEUE:
            case TEMPORARY_QUEUE:
                if (representsString(consumerBody) && representsEnum(providerBody)) {
                    return false;
                }

                if (representsEnum(consumerBody) && representsEnum(providerBody)) {
                    return providerBody.getEnums().containsAll(consumerBody.getEnums());
                }

                return true;
            case TOPIC:
                if (representsString(providerBody) && representsEnum(consumerBody)) {
                    return false;
                }

                if (representsEnum(providerBody) && representsEnum(consumerBody)) {
                    return consumerBody.getEnums().containsAll(providerBody.getEnums());
                }

                return true;
            default:
                throw new RuntimeException("Unknown JMS destination type"); // TODO: handle
        }
    }

    private boolean representsEnum(StringSchema body) {
        return body.getEnums().size() > 0;
    }

    private boolean representsString(StringSchema body) {
        return body.getEnums().size() == 0;
    }

    private boolean compareObjectSchema(DestinationType dstType, ObjectSchema consumerBody, ObjectSchema providerBody) {
        return equals(consumerBody.getAdditionalProperties(), providerBody.getAdditionalProperties())
                && equals(consumerBody.getDependencies(), providerBody.getDependencies())
                && equals(consumerBody.getPatternProperties(), providerBody.getPatternProperties())
                && compareObjectProperties(dstType, consumerBody.getProperties(), providerBody.getProperties());
    }

    private boolean compareObjectProperties(DestinationType dstType,
                                            Map<String, JsonSchema> consumerProperties,
                                            Map<String, JsonSchema> providerProperties) {
        return consumerProperties.keySet().equals(providerProperties.keySet())
                && consumerProperties.keySet().stream()
                .allMatch(key -> isSchemaMatching(
                        dstType, consumerProperties.get(key), providerProperties.get(key)));
    }

    private boolean equals(Object object1, Object object2) {
        if (object1 == null) {
            return object2 == null;
        } else {
            return object1.equals(object2);
        }
    }
}
