package com.hltech.vaunt.validator

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.hltech.vaunt.core.VauntSerializer
import com.hltech.vaunt.core.domain.model.Capabilities
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.DestinationType
import com.hltech.vaunt.core.domain.model.Expectations
import com.hltech.vaunt.core.domain.model.Service
import com.hltech.vaunt.validator.projectA.messages.EnumMessage
import com.hltech.vaunt.validator.projectA.messages.EnumStringMessage
import com.hltech.vaunt.validator.projectB.messages.AnotherSampleMessage
import com.hltech.vaunt.validator.projectB.messages.SameMessageAsSample
import com.hltech.vaunt.validator.projectB.messages.SampleMessage
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll


class VauntValidatorSpec extends Specification {

    @Subject
    VauntValidator validator = new VauntValidator()

    @Shared
    def serializer = new VauntSerializer()

    def 'Consumer and provider having matching contract should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.QUEUE, 'queue', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be no validation errors'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Matching expectations and capabilities should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.QUEUE, 'queue', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation errors'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Consumer expecting nonexistent endpoint (different destination type) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.MISSING_ENDPOINT
    }

    def 'Expectations against nonexistent endpoint (different destination type) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.MISSING_ENDPOINT
    }

    def 'Consumer expecting nonexistent endpoint for types other than temporary queues (different destination name) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.MISSING_ENDPOINT
    }

    def 'Consumer expecting existing endpoint with different destination names for temporary queues should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.TEMPORARY_QUEUE, 'a name', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TEMPORARY_QUEUE, 'another name', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be no validation errors'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Expectations against nonexistent endpoint for types other than temporary queues (different destination name) should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.MISSING_ENDPOINT
    }

    def 'Expectations against existing endpoint with different destination names for temporary queues should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.TEMPORARY_QUEUE, 'any name', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TEMPORARY_QUEUE, 'any other name', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be no validation errors'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Consumer expecting different JsonSchema of a message than provider should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(AnotherSampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        and: 'consumer and provider representation'
            ArrayListMultimap<String, Contract> providerNameToContracts = ArrayListMultimap.create()
            providerNameToContracts.put('provider', consumerContract)

            def consumer = new Service('consumer', null, new Expectations(providerNameToContracts))
            def provider = new Service('provider', new Capabilities(Lists.newArrayList(providerContract)), null)

        when: 'contracts are validated'
            def validationResults = validator.validate(consumer, provider)

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA
    }

    def 'Expectations with different JsonSchema of a message than capabilities should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(AnotherSampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA
    }

    def 'Expectations with the same JsonSchema of a message as capabilities except message ids should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(SameMessageAsSample)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA
    }

    def 'Expectations with the same JsonSchema of a message as capabilities except inner object ids should pass validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(SampleMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectA.messages.SampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should not be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].valid
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
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0

        where:
            dstType << [DestinationType.QUEUE, DestinationType.TEMPORARY_QUEUE]
    }

    def 'Expectations with JsonSchema of a message containing enum having values being subset of what is in Capabilities should not pass validation - dstType = topic'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(EnumMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA
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
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA

        where:
            dstType << [DestinationType.QUEUE, DestinationType.TEMPORARY_QUEUE]
    }

    def 'Capabilities with JsonSchema of a message containing enum having values being subset of what is in Expectations should pass validation - dstType = topic'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumMessage)
            def providerJsonSchema = serializer.generateSchema(EnumMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should not be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
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
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA

        where:
            dstType << [DestinationType.QUEUE, DestinationType.TEMPORARY_QUEUE]
    }

    def 'Capabilities with JsonSchema of a message containing enum and Expectations containing string in corresponding place should pass validation - dstType = topic'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumStringMessage)
            def providerJsonSchema = serializer.generateSchema(EnumStringMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should not be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
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

        then: 'there should be wrong schema validation error'
            validationResults.size() == 1
            validationResults[0].valid
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0

        where:
            dstType << [DestinationType.QUEUE, DestinationType.TEMPORARY_QUEUE]
    }

    def 'Expectations with JsonSchema of a message containing enum and Capabilities containing string in corresponding place should not pass validation - dstType = topic'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = serializer.generateSchema(EnumStringMessage)
            def providerJsonSchema = serializer.generateSchema(com.hltech.vaunt.validator.projectB.messages.EnumStringMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', consumerJsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'topic', providerJsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should not be wrong schema validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.WRONG_SCHEMA
    }
}
