package com.hltech.vaunt.core

import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.google.common.collect.Multimap
import com.hltech.vaunt.core.domain.model.Contract
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import java.time.ZonedDateTime

class VauntSerializerSpec extends Specification {

    @Shared
    def mapper = new VauntSerializer().mapper

    @Subject
    def serializer = new VauntSerializer()

    def 'Should correctly serialize schema'() {
        given:
            def schema = new JsonSchemaGenerator(mapper).generateSchema(Message)

        expect:
            new JsonSlurper().parseText(serializer.serializeSchema(schema)) == new JsonSlurper().parseText(expectedResponse())
    }

    def 'Should correctly generate schema'() {
        expect:
            new JsonSlurper().parseText(serializer.serializeSchema(serializer.generateSchema(Message))) == new JsonSlurper().parseText(expectedResponse())
    }

    def expectedResponse() {
        """
        {
            "type":"object",
            "id":"urn:jsonschema:com:hltech:vaunt:core:VauntSerializerSpec:Message",
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
                            "id":"urn:jsonschema:com:hltech:vaunt:core:domain:model:Contract",
                            "properties":{
                                "destinationType":{
                                    "type":"string",
                                    "enum":["QUEUE","TOPIC"]
                                },
                                "destinationName":{
                                    "type":"string"
                                },
                                "body":{
                                    "type":"object",
                                    "id":"urn:jsonschema:com:fasterxml:jackson:module:jsonSchema:JsonSchema",
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
                                                "\$ref":"urn:jsonschema:com:fasterxml:jackson:module:jsonSchema:JsonSchema"
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
                                                "\$ref":"urn:jsonschema:com:fasterxml:jackson:module:jsonSchema:JsonSchema"
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

    class Message {
        String string
        ZonedDateTime zonedDateTime
        Multimap<String, Contract> mapped
        List<String> listed
    }
}
