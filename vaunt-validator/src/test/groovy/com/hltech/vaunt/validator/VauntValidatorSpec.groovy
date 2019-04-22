package com.hltech.vaunt.validator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.hltech.vaunt.validator.domain.representation.model.Capabilities
import com.hltech.vaunt.validator.domain.representation.model.Contract
import com.hltech.vaunt.validator.domain.representation.model.DestinationType
import com.hltech.vaunt.validator.domain.representation.model.Expectations
import com.hltech.vaunt.validator.domain.representation.model.Service
import spock.lang.Specification
import spock.lang.Subject


class VauntValidatorSpec extends Specification {

    @Subject
    VauntValidator validator = new VauntValidator()

    def jsonSchemaGenerator = new JsonSchemaGenerator(new ObjectMapper())

    def 'Consumer and provider having matching contract should pass validation'() {
        given: 'consumer and provider contracts'
            def jsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', jsonSchema)
            def providerContract = new Contract(DestinationType.QUEUE, 'queue', jsonSchema)

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
            def jsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', jsonSchema)
            def providerContract = new Contract(DestinationType.QUEUE, 'queue', jsonSchema)

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
            def jsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', jsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', jsonSchema)

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

    def 'expectations against nonexistent endpoint (different destination type) should fail validation'() {
        given: 'consumer and provider contracts'
            def jsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.QUEUE, 'queue', jsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', jsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.MISSING_ENDPOINT
    }

    def 'Consumer expecting nonexistent endpoint (different destination name) should fail validation'() {
        given: 'consumer and provider contracts'
            def jsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', jsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', jsonSchema)

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

    def 'Expectations against nonexistent endpoint (different destination name) should fail validation'() {
        given: 'consumer and provider contracts'
            def jsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def consumerContract = new Contract(DestinationType.TOPIC, 'topic', jsonSchema)
            def providerContract = new Contract(DestinationType.TOPIC, 'queue', jsonSchema)

        when: 'expectations and capabilities are validated'
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then: 'there should be missing endpoint validation error'
            validationResults.size() == 1
            !validationResults[0].valid
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == ValidationError.MISSING_ENDPOINT
    }

    def 'Consumer expecting different JsonSchema of a message than provider should fail validation'() {
        given: 'consumer and provider contracts'
            def consumerJsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def providerJsonSchema = jsonSchemaGenerator.generateSchema(AnotherSampleMessage)
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
            def consumerJsonSchema = jsonSchemaGenerator.generateSchema(SampleMessage)
            def providerJsonSchema = jsonSchemaGenerator.generateSchema(AnotherSampleMessage)
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
}
