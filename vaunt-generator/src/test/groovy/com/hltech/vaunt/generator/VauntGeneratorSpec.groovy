package com.hltech.vaunt.generator

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.google.common.collect.ArrayListMultimap
import com.hltech.vaunt.generator.domain.representation.RepresentationExtractor
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

class VauntGeneratorSpec extends Specification {

    def representationExtractorMock = Mock(RepresentationExtractor)

    @Shared
    def objectMapper = new ObjectMapper().registerModule(new GuavaModule())

    @Subject
    VauntGenerator generator = new VauntGenerator(representationExtractorMock, objectMapper)

    def 'should write json file with service name, expectations and capabilities to target directory'() {
        given:
            def packageName = 'dev.hltech.transferout.informationrequest'
            def serviceName = 'information-request'
            def targetDir = './build/tmp/test/'

        and:
            def providersToContracts = ArrayListMultimap.create()
            providersToContracts.put('altus-client',
                    queueContract('transfers_out.reject_information_request_queue', RejectMessage))
            providersToContracts.put('altus-client',
                    queueContract('transfers_out.release_information_request_queue', ReleaseMessage))

            def service = new Service(
                    serviceName,
                    new Capabilities([queueContract('transfers_out.create_information_request_queue', CreateMessage)]),
                    new Expectations(providersToContracts)
            )

            1 * representationExtractorMock.extractServiceRepresentation(packageName, serviceName) >> service

        when:
            generator.writeVauntFile(packageName, serviceName, targetDir)

        then:
            def content = Paths.get(targetDir).resolve("${serviceName}.json").toFile().text
            expect content, isJson()
            expect content, hasJsonPath('$.name', equalTo(serviceName))

        and:
            expect content, hasJsonPath('$.capabilities.contracts', hasSize(1))
            expect content, hasJsonPath('$.capabilities.contracts[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.capabilities.contracts[0].destinationName', equalTo('transfers_out.create_information_request_queue'))
            expect content, hasJsonPath('$.capabilities.contracts[0].body.type', equalTo('object'))
            expect content, hasJsonPath('$.capabilities.contracts[0].body.properties.name.type', equalTo('string'))

        and:
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client', hasSize(2))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[0].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[0].destinationName', equalTo('transfers_out.reject_information_request_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[0].body.type', equalTo('object'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[0].body.properties.reason.type', equalTo('string'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[0].body.properties.code.type', equalTo('integer'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[1].destinationType', equalTo('QUEUE'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[1].destinationName', equalTo('transfers_out.release_information_request_queue'))
            expect content, hasJsonPath('$.expectations.providerNameToContracts.altus-client[1].body.properties.id.type', equalTo('integer'))


    }

    private Contract queueContract(String queueName, Class<?> schemaClass) {
        def schemaGenerator = new JsonSchemaGenerator(objectMapper)
        new Contract(DestinationType.QUEUE, queueName, schemaGenerator.generateSchema(schemaClass))
    }

    class CreateMessage {
        String name
    }

    class RejectMessage {
        String reason
        int code
    }

    class ReleaseMessage {
        int id
    }
}
