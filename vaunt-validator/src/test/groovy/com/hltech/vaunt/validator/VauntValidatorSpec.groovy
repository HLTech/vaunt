package com.hltech.vaunt.validator

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.hltech.vaunt.core.VauntSerializer
import com.hltech.vaunt.core.domain.model.Capabilities
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.Expectations
import com.hltech.vaunt.core.domain.model.Service
import com.hltech.vaunt.validator.projectA.messages.EnumMessage
import com.hltech.vaunt.validator.projectA.messages.EnumStringMessage
import com.hltech.vaunt.validator.projectA.messages.RequiredMessage
import com.hltech.vaunt.validator.projectA.messages.SmallMessage
import com.hltech.vaunt.validator.projectB.messages.AnotherSampleMessage
import com.hltech.vaunt.validator.projectB.messages.SampleMessage
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static com.hltech.vaunt.core.domain.model.DestinationType.QUEUE
import static com.hltech.vaunt.core.domain.model.DestinationType.TEMPORARY_QUEUE
import static com.hltech.vaunt.core.domain.model.DestinationType.TOPIC
import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.OK
import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.FAILED

class VauntValidatorSpec extends Specification {

    @Subject
    VauntValidator validator = new VauntValidator()

    @Shared
    def serializer = new VauntSerializer()

    def 'Consumer and provider having matching contract should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(QUEUE, 'queue', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be no validation errors'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = QUEUE, destinationName = "queue", messageId = "SampleMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Matching expectations and capabilities should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(QUEUE, 'queue', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation errors'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = QUEUE, destinationName = "queue", messageId = "SampleMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Consumer expecting nonexistent endpoint (different destination type) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'queue', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = QUEUE, destinationName = "queue", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing endpoint required by consumer'
    }

    def 'Expectations against nonexistent endpoint (different destination type) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'queue', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = QUEUE, destinationName = "queue", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing endpoint required by consumer'
    }

    def 'Consumer expecting nonexistent endpoint for types other than temporary queues (different destination name) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'queue', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing endpoint required by consumer'
    }

    def 'Consumer expecting existing endpoint with different destination names for temporary queues should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(TEMPORARY_QUEUE, 'a name', consumerJsonSchema)
            def providerContract = new Contract(TEMPORARY_QUEUE, 'another name', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be no validation errors'
            validationResults[0].name == 'Contract(destinationType = TEMPORARY_QUEUE, destinationName = "a name", messageId = "SampleMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Expectations against nonexistent endpoint for types other than temporary queues (different destination name) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'queue', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing endpoint required by consumer'
    }

    def 'Expectations against existing endpoint with different destination names for temporary queues should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(TEMPORARY_QUEUE, 'any name', consumerJsonSchema)
            def providerContract = new Contract(TEMPORARY_QUEUE, 'any other name', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation errors'
            validationResults[0].name == 'Contract(destinationType = TEMPORARY_QUEUE, destinationName = "any name", messageId = "SampleMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Consumer expecting different id of a message than provider should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(AnotherSampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be missing message with given id validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing message with given id required by consumer'
    }

    def 'Expectations with different id of a message than capabilities should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(AnotherSampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing message with given id validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing message with given id required by consumer'
    }

    def 'When more than one capabilities of provider fulfill expectations validator should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectA.messages.SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract, providerContract))

        then: 'there should be more than one message with the same id for given endpoint validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'More than one message with the same id for given endpoint'
    }

    def 'Expectations with different JsonSchema of a message than capabilities should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectA.messages.AnotherSampleMessage)
            def providerJsonSchema = serializer.generateSchema(AnotherSampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "AnotherSampleMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Consumer schema with id null and type StringSchema does not match provider schema with id null and type IntegerSchema'
    }

    def 'Expectations with the same JsonSchema of a message as capabilities except inner object ids should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectA.messages.SampleMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should not be wrong schema validation error'
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SampleMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    @Unroll
    def 'Expectations with JsonSchema of a message containing enum having values being subset of what is in Capabilities should pass validation - dstType = #dstType'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(EnumMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumMessage)
            def consumerContract = new Contract(dstType, dstType.toString(), consumerJsonSchema)
            def providerContract = new Contract(dstType, dstType.toString(), providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should not be wrong schema validation error'
            validationResults[0].name == 'Contract(destinationType = ' + dstType.toString() + ', destinationName = "'+dstType.toString() + '", messageId = "EnumMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0

        where:
            dstType << [QUEUE, TEMPORARY_QUEUE, TOPIC]
    }

    @Unroll
    def 'Capabilities with JsonSchema of a message containing enum having values being subset of what is in Expectations should not pass validation - dstType = #dstType'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumMessage)
            def providerJsonSchema = serializer.generateSchema(EnumMessage)
            def consumerContract = new Contract(dstType, dstType.toString(), consumerJsonSchema)
            def providerContract = new Contract(dstType, dstType.toString(), providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = ' + dstType.toString() + ', destinationName = "' + dstType.toString() + '", messageId = "EnumMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Schema with id null has not matching enums - consumer: [A, B, C, D], provider: [A, B, C]'

        where:
            dstType << [QUEUE, TEMPORARY_QUEUE, TOPIC]
    }

    @Unroll
    def 'Capabilities with JsonSchema of a message containing enum and Expectations containing string in corresponding place should not pass validation - dstType = #dstType'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumStringMessage)
            def providerJsonSchema = serializer.generateSchema(EnumStringMessage)
            def consumerContract = new Contract(dstType, dstType.toString(), consumerJsonSchema)
            def providerContract = new Contract(dstType, dstType.toString(), providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = ' + dstType.toString() + ', destinationName = "' + dstType.toString() + '", messageId = "EnumStringMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Schema with id null has not matching enums - consumer: [], provider: [A, B, C]'

        where:
            dstType << [QUEUE, TEMPORARY_QUEUE, TOPIC]
    }

    @Unroll
    def 'Expectations with JsonSchema of a message containing enum and Capabilities containing string in corresponding place should pass validation - dstType = #dstType'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(EnumStringMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumStringMessage)
            def consumerContract = new Contract(dstType, dstType.toString(), consumerJsonSchema)
            def providerContract = new Contract(dstType, dstType.toString(), providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation error'
            validationResults[0].name == 'Contract(destinationType = ' + dstType.toString() + ', destinationName = "'+dstType.toString() + '", messageId = "EnumStringMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0

        where:
            dstType << [QUEUE, TEMPORARY_QUEUE, TOPIC]
    }

    def 'Provider JsonSchema should be able to contain superset of consumer JsonSchema fields'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SmallMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.SmallMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation error'
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SmallMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Consumer JsonSchema should not be able to contain superset of provider JsonSchema fields'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.SmallMessage)
            def providerJsonSchema = serializer.generateSchema(SmallMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "SmallMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors != null
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Schema with id SmallMessage has not matching properties - consumer: ids of properties: [ab, defg, abc], provider: ids of properties: [ab, defg]'
    }

    def 'Provider JsonSchema should be able to contain required field when consumer JsonSchema does not contain similar not required field'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.RequiredMessage)
            def providerJsonSchema = serializer.generateSchema(RequiredMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation error'
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "RequiredMessage")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Consumer JsonSchema should not be able to contain required field when provider JsonSchema does not contain similar not required field'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(RequiredMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.RequiredMessage)
            def consumerContract = new Contract(TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "RequiredMessage")'
            validationResults[0].result == FAILED
            validationResults[0].errors != null
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Schema with id null has not matching required - consumer: true, provider: null'
    }
}
