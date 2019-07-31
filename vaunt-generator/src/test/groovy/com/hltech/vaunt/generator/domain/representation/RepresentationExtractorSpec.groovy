package com.hltech.vaunt.generator.domain.representation

import com.hltech.vaunt.core.VauntSerializer
import com.hltech.vaunt.generator.domain.representation.message.poliannotated.SampleConsumerMessage
import com.hltech.vaunt.generator.domain.representation.message.poliannotated.SampleProviderMessage
import com.hltech.vaunt.core.domain.model.DestinationType
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class RepresentationExtractorSpec extends Specification {

    @Shared
    def serializer = new VauntSerializer()

    @Subject
    RepresentationExtractor representationExtractor = new RepresentationExtractor(serializer)

    def 'Should extract service representation for provider messages with single annotation'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.monoannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, new Properties())

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Capabilities are as expected'
            def expectedBody = serializer.generateSchema(com.hltech.vaunt.generator.domain.representation.message.monoannotated.SampleProviderMessage)

            service.capabilities
            service.capabilities.contracts
            service.capabilities.contracts.size() == 1
            service.capabilities.contracts[0].destinationName == 'destination'
            service.capabilities.contracts[0].destinationType == DestinationType.QUEUE
            service.capabilities.contracts[0].body.getId() == expectedBody.getId()
            service.capabilities.contracts[0].body == expectedBody
    }

    def 'Should extract service representation for provider messages with single annotation and using properties'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.monoannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package with given properties'
            def props = new Properties()
            props.setProperty('destination', 'epicQueue')
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, props)

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Capabilities are as expected'
            def expectedBody = serializer.generateSchema(com.hltech.vaunt.generator.domain.representation.message.monoannotated.SampleProviderMessage)

            service.capabilities
            service.capabilities.contracts
            service.capabilities.contracts.size() == 1
            service.capabilities.contracts[0].destinationName == 'epicQueue'
            service.capabilities.contracts[0].destinationType == DestinationType.QUEUE
            service.capabilities.contracts[0].body.getId() == expectedBody.getId()
            service.capabilities.contracts[0].body == expectedBody
    }

    def 'Should extract service representation for consumer messages with single annotation'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.monoannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, new Properties())

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Expectations are as expected'
            service.expectations
            service.expectations.providerNameToContracts
            service.expectations.providerNameToContracts.size() == 1
            service.expectations.providerNameToContracts.get('provider')
            service.expectations.providerNameToContracts.get('provider').size() == 1

            def providerContract = service.expectations.providerNameToContracts.get('provider')[0]
            def expectedBody = serializer.generateSchema(com.hltech.vaunt.generator.domain.representation.message.monoannotated.SampleConsumerMessage)
            providerContract.destinationType == DestinationType.QUEUE
            providerContract.destinationName == 'destination'
            providerContract.body.getId() == expectedBody.getId()
            providerContract.body == expectedBody
    }

    def 'Should extract service representation for consumer messages with single annotation and using properties'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.monoannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package with given properties'
            def props = new Properties()
            props.setProperty('destination', 'epicQueue')
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, props)

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Expectations are as expected'
            service.expectations
            service.expectations.providerNameToContracts
            service.expectations.providerNameToContracts.size() == 1
            service.expectations.providerNameToContracts.get('provider')
            service.expectations.providerNameToContracts.get('provider').size() == 1

            def providerContract = service.expectations.providerNameToContracts.get('provider')[0]
            def expectedBody = serializer.generateSchema(com.hltech.vaunt.generator.domain.representation.message.monoannotated.SampleConsumerMessage)
            providerContract.destinationType == DestinationType.QUEUE
            providerContract.destinationName == 'epicQueue'
            providerContract.body.getId() == expectedBody.getId()
            providerContract.body == expectedBody
    }

    def 'Should extract service representation for provider messages with multiple annotations'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.poliannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, new Properties())

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Capabilities are as expected'
            def expectedBody = serializer.generateSchema(SampleProviderMessage)

            service.capabilities
            service.capabilities.contracts
            service.capabilities.contracts.size() == 2
            service.capabilities.contracts[0].destinationName == 'destination'
            service.capabilities.contracts[0].destinationType == DestinationType.QUEUE
            service.capabilities.contracts[0].body == serializer.generateSchema(SampleProviderMessage)
            service.capabilities.contracts[1].destinationName == 'destination2'
            service.capabilities.contracts[1].destinationType == DestinationType.QUEUE
            service.capabilities.contracts[1].body.getId() == expectedBody.getId()
            service.capabilities.contracts[1].body == expectedBody
    }

    def 'Should extract service representation for consumer messages with multiple annotations'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.poliannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, new Properties())

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Expectations are as expected'
            service.expectations
            service.expectations.providerNameToContracts
            service.expectations.providerNameToContracts.size() == 2
            service.expectations.providerNameToContracts.get('provider')
            service.expectations.providerNameToContracts.get('provider').size() == 2

            def expectedBody = serializer.generateSchema(SampleConsumerMessage)

            def providerContract = service.expectations.providerNameToContracts.get('provider')[0]
            providerContract.destinationType == DestinationType.QUEUE
            providerContract.destinationName == 'destination'
            providerContract.body.getId() == expectedBody.getId()
            providerContract.body == expectedBody

            def providerContract2 = service.expectations.providerNameToContracts.get('provider')[1]
            providerContract2.destinationType == DestinationType.QUEUE
            providerContract2.destinationName == 'destination2'
            providerContract2.body.getId() == expectedBody.getId()
            providerContract2.body == expectedBody
    }

    def 'Should extract service representation for provider messages with single annotation with given message id'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.idannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, new Properties())

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Capabilities are as expected'
            def expectedBody = serializer.generateSchema(com.hltech.vaunt.generator.domain.representation.message.idannotated.SampleProviderMessage)

            service.capabilities
            service.capabilities.contracts
            service.capabilities.contracts.size() == 1
            service.capabilities.contracts[0].destinationName == 'destination'
            service.capabilities.contracts[0].destinationType == DestinationType.QUEUE
            service.capabilities.contracts[0].body.getId() == 'testId'
            service.capabilities.contracts[0].body == expectedBody
    }

    def 'Should extract service representation for consumer messages with single annotation with given message id'() {
        given: 'package and service name'
            def packageName = 'com.hltech.vaunt.generator.domain.representation.message.idannotated'
            def serviceName = 'service'

        when: 'Service representation is extracted from given package'
            def service = representationExtractor.extractServiceRepresentation(packageName, serviceName, new Properties())

        then: 'Service name is as expected'
            service.name == 'service'

        and: 'Expectations are as expected'
            service.expectations
            service.expectations.providerNameToContracts
            service.expectations.providerNameToContracts.size() == 1
            service.expectations.providerNameToContracts.get('provider')
            service.expectations.providerNameToContracts.get('provider').size() == 1

            def expectedBody = serializer.generateSchema(com.hltech.vaunt.generator.domain.representation.message.idannotated.SampleConsumerMessage)
            def providerContract = service.expectations.providerNameToContracts.get('provider')[0]
            providerContract.destinationType == DestinationType.QUEUE
            providerContract.destinationName == 'destination'
            providerContract.body.getId() == 'randomId'
            providerContract.body == expectedBody
    }
}
