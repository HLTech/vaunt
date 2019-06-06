package com.hltech.vaunt.core

import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.google.common.collect.Multimap
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.DestinationType
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import java.time.ZonedDateTime

class VauntSerializerSpec extends Specification {

    @Shared
    def generator = new VauntSerializer().generator

    @Subject
    def serializer = new VauntSerializer()

    def 'Should correctly serialize schema'() {
        given:
            def schema = generator.generateSchema(Message)

        expect:
            new JsonSlurper().parseText(serializer.serializeSchema(schema)) == new JsonSlurper().parseText(expectedResponse())
    }

    def 'Should correctly generate schema'() {
        expect:
            new JsonSlurper().parseText(serializer.serializeSchema(serializer.generateSchema(Message))) == new JsonSlurper().parseText(expectedResponse())
    }

    def 'Should correctly parse contracts'() {
        given:
            def dstType = DestinationType.QUEUE
            def dstName = 'dst'
            def contract = """
                    [
                        {
                            "destinationType":"${dstType}",
                            "destinationName":"${dstName}",
                            "body": {
                                "type":"string"
                            }
                        }
                    ]
                """

        expect:
            serializer.parseContracts(contract) == [new Contract(dstType, dstName, new StringSchema())]
    }

    def expectedResponse() {
        """
        {
            "type":"object",
            "id":"Message",
            "properties":{
                "string":{
                    "type":"string"
                },
                "zonedDateTime":{
                    "type":"number"
                },"mapped":{
                    "type":"object",
                    "additionalProperties":{
                        "type":"array",
                        "items":{
                            "type":"object",
                            "id":"Contract",
                            "properties":{
                                "destinationType":{
                                    "type":"string",
                                    "enum":["QUEUE","TOPIC","TEMPORARY_QUEUE"]
                                },
                                "destinationName":{
                                    "type":"string"
                                },
                                "body":{
                                    "type":"object",
                                    "id":"JsonSchema",
                                    "properties":{
                                        "id":{
                                            "type":"string"
                                        },
                                        "\$ref":{
                                            "type":"string"
                                        },
                                        "\$schema":{
                                            "type":"string"
                                        },
                                        "disallow":{
                                            "type":"array",
                                            "items":{
                                                "type":"object",
                                                "\$ref":"JsonSchema"
                                            }
                                        },
                                        "required":{
                                            "type":"boolean"
                                        },
                                        "readonly":{
                                            "type":"boolean"
                                        },
                                        "description":{
                                            "type":"string"
                                        },
                                        "extends":{
                                            "type":"array",
                                            "items":{
                                                "type":"object",
                                                "\$ref":"JsonSchema"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                "listed":{
                    "type":"array",
                    "items":{
                        "type":"string"
                    }
                }
            }
        }       
        """
    }

    class Message extends VisitorContext {
        String string
        ZonedDateTime zonedDateTime
        Multimap<String, Contract> mapped
        List<String> listed
    }
}
