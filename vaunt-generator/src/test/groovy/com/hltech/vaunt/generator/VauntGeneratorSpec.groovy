package com.hltech.vaunt.generator

import com.hltech.vaunt.core.domain.model.Service
import com.hltech.vaunt.generator.domain.representation.RepresentationExtractor
import com.hltech.vaunt.generator.domain.representation.RepresentationWriter
import spock.lang.Specification
import spock.lang.Subject

class VauntGeneratorSpec extends Specification {

    def extractorMock = Mock(RepresentationExtractor)
    def writerMock = Mock(RepresentationWriter)

    @Subject
    VauntGenerator generator = new VauntGenerator(extractorMock, writerMock)

    def "should write extracted service representation to target directory"() {
        given:
            def packageRoot = 'com.example.package'
            def serviceName = 'service-name'
            def targetDir = './build/tmp/test/'

        and:
            def service = Mock(Service)
            extractorMock.extractServiceRepresentation(packageRoot, serviceName, new Properties()) >> service

        when:
            generator.writeVauntFile(packageRoot, serviceName, targetDir)

        then:
            1 * writerMock.writeServiceRepresentation(service, targetDir)
    }
}
