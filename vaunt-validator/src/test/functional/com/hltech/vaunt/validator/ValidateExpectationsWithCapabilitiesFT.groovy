package com.hltech.vaunt.validator

import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.google.common.collect.Lists
import com.hltech.vaunt.core.domain.model.Contract
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static com.hltech.vaunt.core.domain.model.DestinationType.QUEUE
import static com.hltech.vaunt.core.domain.model.DestinationType.TEMPORARY_QUEUE
import static com.hltech.vaunt.core.domain.model.DestinationType.TOPIC
import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.OK
import static com.hltech.vaunt.validator.ValidationResult.ValidationStatus.FAILED

class ValidateExpectationsWithCapabilitiesFT extends Specification {

    @Subject
    VauntValidator validator = new VauntValidator()

    def 'Expectations fulfilled by capabilities should pass validation'() {
        given:
            def consumerContract = new Contract(QUEUE, 'queue', sampleJsonSchema('id1'))
            def providerContract = new Contract(QUEUE, 'queue', sampleJsonSchema('id1'))
            def anotherProviderContract = new Contract(QUEUE, 'queue', sampleJsonSchema('id2', 'abc', 4321, 4321))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract, anotherProviderContract))

        then:
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = QUEUE, destinationName = "queue", messageId = "id1")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Expectations against nonexistent endpoint (different destination type) should fail validation'() {
        given:
            def consumerContract = new Contract(QUEUE, 'queue', sampleJsonSchema('id1'))
            def providerContract = new Contract(TOPIC, 'queue', sampleJsonSchema('id1'))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then:
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = QUEUE, destinationName = "queue", messageId = "id1")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing endpoint required by consumer'
    }

    @Unroll
    def 'Expectations against nonexistent endpoint for #destinationType (different destination name) should fail validation'() {
        given:
            def consumerContract = new Contract(destinationType, 'topic', sampleJsonSchema('id1'))
            def providerContract = new Contract(destinationType, 'queue', sampleJsonSchema('id1'))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then:
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = ' + destinationType + ', destinationName = "topic", messageId = "id1")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing endpoint required by consumer'

        where:
            destinationType << [TOPIC, QUEUE]
    }

    def 'Expectations against existing endpoint with different destination names for temporary queues should pass validation'() {
        given:
            def consumerContract = new Contract(TEMPORARY_QUEUE, 'any name', sampleJsonSchema('id1'))
            def providerContract = new Contract(TEMPORARY_QUEUE, 'any other name', sampleJsonSchema('id1'))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then:
            validationResults[0].name == 'Contract(destinationType = TEMPORARY_QUEUE, destinationName = "any name", messageId = "id1")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Expectations with different id of a message than capabilities should fail validation'() {
        given:
            def consumerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1'))
            def providerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id2'))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then:
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "id1")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'Missing message with given id required by consumer'
    }

    def 'When more than one capabilities of provider fulfill expectations validator should fail validation'() {
        given:
            def consumerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1'))
            def providerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1'))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract, providerContract))

        then:
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "id1")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 1
            validationResults[0].errors[0] == 'More than one message with the same id for given endpoint'
    }

    def 'Expectations with the same JsonSchema of a message as capabilities except inner object ids should pass validation'() {
        given:
            def consumerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1'))
            def providerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1', 'another bSch'))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract))

        then:
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "id1")'
            validationResults[0].result == OK
            validationResults[0].errors != null
            validationResults[0].errors.size() == 0
    }

    def 'Expectations with different JsonSchema of a message than capabilities should fail validation'() {
        given:
            def consumerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1'))
            def providerContract = new Contract(TOPIC, 'topic', sampleJsonSchema('id1', 'bSch', 4321, 4321))
            def anotherProviderContract = new Contract(QUEUE, 'topic', sampleJsonSchema('id2', 'bSch', 4321, 4321))

        when:
            def validationResults = validator.validate(Lists.newArrayList(consumerContract), Lists.newArrayList(providerContract, anotherProviderContract))

        then:
            validationResults.size() == 1
            validationResults[0].name == 'Contract(destinationType = TOPIC, destinationName = "topic", messageId = "id1")'
            validationResults[0].result == FAILED
            validationResults[0].errors
            validationResults[0].errors.size() == 2
            validationResults[0].errors[0] == 'Schema with id nSch has not matching multipleOf - consumer: 1234.0, provider: 4321.0'
            validationResults[0].errors[1] == 'Schema with id sSch has not matching minLength - consumer: 1234, provider: 4321'
    }

    def sampleJsonSchema(def id, def bId = 'bSch', def mOf = 1234, def mLe = 1234) {
        new ObjectSchema(
                'id': id,
                'properties': ['boolean': new BooleanSchema('id': bId),
                               'integer': new IntegerSchema('id': 'iSch'),
                               'number': new NumberSchema('id': 'nSch', 'multipleOf': mOf),
                               'object': new ObjectSchema('id': 'oSch'),
                               'string': new StringSchema('id': 'sSch', minLength: mLe)])
    }
}
