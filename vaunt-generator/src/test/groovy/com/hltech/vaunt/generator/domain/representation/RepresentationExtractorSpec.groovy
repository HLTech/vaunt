package com.hltech.vaunt.generator.domain.representation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.hltech.vaunt.generator.domain.representation.message.SampleConsumerMessage
import com.hltech.vaunt.generator.domain.representation.message.SampleProviderMessage
import com.hltech.vaunt.generator.domain.representation.model.DestinationType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class RepresentationExtractorSpec extends Specification {

    @Shared
    def jsonSchemaGenerator = new JsonSchemaGenerator(new ObjectMapper())

    @Subject
    RepresentationExtractor representationExtractor = new RepresentationExtractor(jsonSchemaGenerator)

    def 'Should extract service representation'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName)

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Capabilities are as expected'
            service.capabilities
            service.capabilities.contracts
            service.capabilities.contracts[0].destinationName == 'destination'
            service.capabilities.contracts[0].destinationType == DestinationType.QUEUE
            service.capabilities.contracts[0].body == jsonSchemaGenerator.generateSchema(SampleProviderMessage)

        and: 'Expectations are as expected'
            service.expectations
            service.expectations.providerNameToContracts
            service.expectations.providerNameToContracts.size() == 1
            service.expectations.providerNameToContracts.get('provider')
            service.expectations.providerNameToContracts.get('provider').size() == 1
            def providerContract = service.expectations.providerNameToContracts.get('provider')[0]
            providerContract.destinationType == DestinationType.QUEUE
            providerContract.destinationName == 'destination'
            providerContract.body == jsonSchemaGenerator.generateSchema(SampleConsumerMessage)
    }
}
