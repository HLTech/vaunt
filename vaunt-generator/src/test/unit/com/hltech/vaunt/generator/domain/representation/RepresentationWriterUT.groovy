package com.hltech.vaunt.generator.domain.representation

import com.google.common.collect.ArrayListMultimap
import com.hltech.vaunt.core.VauntSerializer
import com.hltech.vaunt.core.domain.model.Capabilities
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.DestinationType
import com.hltech.vaunt.core.domain.model.Expectations
import com.hltech.vaunt.core.domain.model.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Paths

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static spock.util.matcher.HamcrestSupport.expect

class RepresentationWriterUT extends Specification {

    @Shared
    def serializer = new VauntSerializer()

    @Subject
    RepresentationWriter writer = new RepresentationWriter(serializer)

    def 'should write service representation as json file to target directory'() {
        given:
            def serviceName = 'information-service'
            def targetDir = './build/tmp/test/fake/dir'

        and:
            def providersToContracts = ArrayListMultimap.create()
            providersToContracts.put('remote-client', queue('reject_information_queue', RejectMessage))
            providersToContracts.put('remote-client', queue('accept_information_queue', AcceptMessage))
            providersToContracts.put('audit-service', queue('audit_queue', AuditMessage))

            def service = new Service(
                    serviceName,
                    new Capabilities(
                            [
                                    queue('request_for_information_queue', RequestMessage),
                                    topic('something_changed_topic', ChangedEvent)
                            ]
                    ),
                    new Expectations(providersToContracts)
            )

        when:
            writer.writeServiceRepresentation(service, targetDir)

        then:
            def content = Paths.get(targetDir).resolve("${serviceName}.json").toFile().text
            expect content, isJson()
            expect content, hasJsonPath('$.name', equalTo(serviceName))

        and:
            expect content, hasJsonPath('$.capabilities.contracts', hasSize(2))
            expect content, hasJsonPath('$.capabilities.contracts[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.capabilities.contracts[0].destinationName', equalTo('request_for_information_queue'))
            expect content, hasJsonPath('$.capabilities.contracts[0].message.type', equalTo('object'))
            expect content, hasJsonPath('$.capabilities.contracts[0].message.properties.name.type', equalTo('string'))
            expect content, hasJsonPath('$.capabilities.contracts[1].destinationType', equalTo('TOPIC'))
            expect content, hasJsonPath('$.capabilities.contracts[1].destinationName', equalTo('something_changed_topic'))
            expect content, hasJsonPath('$.capabilities.contracts[1].message.type', equalTo('object'))
            expect content, hasJsonPath('$.capabilities.contracts[1].message.properties.timestamp.type', equalTo('integer'))

        and:
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client', hasSize(2))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].destinationName', equalTo('reject_information_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].message.type', equalTo('object'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].message.properties.reason.type', equalTo('string'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].message.properties.code.type', equalTo('integer'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[1].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[1].destinationName', equalTo('accept_information_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[1].message.properties.id.type', equalTo('integer'))

        and:
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service', hasSize(1))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].destinationName', equalTo('audit_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].message.type', equalTo('object'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].message.properties.payload.type', equalTo('string'))


    }

    private Contract topic(String topicName, Class<?> schemaClass) {
        contract(DestinationType.TOPIC, topicName, schemaClass)
    }

    private Contract queue(String queueName, Class<?> schemaClass) {
        contract(DestinationType.QUEUE, queueName, schemaClass)
    }

    private Contract contract(DestinationType type, String name, Class<?> schemaClass) {
        new Contract(type, name, serializer.generateSchema(schemaClass))
    }

    class RequestMessage {
        String name
    }

    class RejectMessage {
        String reason
        int code
    }

    class AcceptMessage {
        int id
    }

    class AuditMessage {
        String payload
    }

    class ChangedEvent {
        long timestamp
    }
}
