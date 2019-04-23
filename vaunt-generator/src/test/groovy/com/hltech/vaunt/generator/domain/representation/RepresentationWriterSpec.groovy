package com.hltech.vaunt.generator.domain.representation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.google.common.collect.ArrayListMultimap
import com.hltech.vaunt.generator.domain.representation.model.Capabilities
import com.hltech.vaunt.generator.domain.representation.model.Contract
import com.hltech.vaunt.generator.domain.representation.model.DestinationType
import com.hltech.vaunt.generator.domain.representation.model.Expectations
import com.hltech.vaunt.generator.domain.representation.model.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Paths

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static spock.util.matcher.HamcrestSupport.expect

class RepresentationWriterSpec extends Specification {

    @Shared
    def mapper = new ObjectMapper().registerModule(new GuavaModule())

    @Subject
    RepresentationWriter writer = new RepresentationWriter(mapper)

    def 'should write service representation as json file to target directory'() {
        given:
            def serviceName = 'information-service'
            def targetDir = './build/tmp/test/'

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
            expect content, hasJsonPath('$.capabilities.contracts[0].body.type', equalTo('object'))
            expect content, hasJsonPath('$.capabilities.contracts[0].body.properties.name.type', equalTo('string'))
            expect content, hasJsonPath('$.capabilities.contracts[1].destinationType', equalTo('TOPIC'))
            expect content, hasJsonPath('$.capabilities.contracts[1].destinationName', equalTo('something_changed_topic'))
            expect content, hasJsonPath('$.capabilities.contracts[1].body.type', equalTo('object'))
            expect content, hasJsonPath('$.capabilities.contracts[1].body.properties.timestamp.type', equalTo('integer'))

        and:
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client', hasSize(2))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].destinationName', equalTo('reject_information_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].body.type', equalTo('object'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].body.properties.reason.type', equalTo('string'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[0].body.properties.code.type', equalTo('integer'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[1].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[1].destinationName', equalTo('accept_information_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.remote-client[1].body.properties.id.type', equalTo('integer'))

        and:
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service', hasSize(1))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].destinationName', equalTo('audit_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].body.type', equalTo('object'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.audit-service[0].body.properties.payload.type', equalTo('string'))


    }

    private Contract topic(String topicName, Class<?> schemaClass) {
        contract(DestinationType.TOPIC, topicName, schemaClass)
    }

    private Contract queue(String queueName, Class<?> schemaClass) {
        contract(DestinationType.QUEUE, queueName, schemaClass)
    }

    private Contract contract(DestinationType type, String name, Class<?> schemaClass) {
        def schemaGenerator = new JsonSchemaGenerator(mapper)
        new Contract(type, name, schemaGenerator.generateSchema(schemaClass))
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
