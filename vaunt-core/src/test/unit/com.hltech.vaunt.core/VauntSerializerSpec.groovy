package com.hltech.vaunt.core

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.hltech.vaunt.core.domain.model.Contract
import com.hltech.vaunt.core.domain.model.DestinationType
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Subject

import javax.validation.constraints.Size
import java.time.ZonedDateTime

class VauntSerializerSpec extends Specification {

    @Subject
    def serializer = new VauntSerializer()

    def 'Should correctly serialize schema'() {
        expect:
            new JsonSlurper().parseText(serializer.serialize(schema())) == new JsonSlurper().parseText(expectedResponse())
    }

    def 'Should correctly generate schema (and should not use ref unless object is of JsonSchema type (threat of StackOverflow)'() {
        expect:
            serializer.generateSchema(Message) == schema()
    }

    def 'Should correctly generate schema without redundant $ref element'() {
        given: 'seenSchemas in VisitorContext contain reference to object X'
            serializer.serialize(serializer.generateSchema(Message))

        expect: 'seenSchemas in VisitorContext should be cleaned and thus no $ref element for JSON for another message referring to object X'
            new JsonSlurper().parseText(serializer.serialize(serializer.generateSchema(AnotherMessage))) == new JsonSlurper().parseText(expectedAnotherResponse())
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
                            "message": {
                                "type":"string"
                            }
                        }
                    ]
                """

        expect:
            serializer.parseContracts(contract) == [new Contract(dstType, dstName, new StringSchema())]
    }

    def schema() {
        def schema = new ObjectSchema()
        schema.setId(Message.getSimpleName())
        schema.putOptionalProperty("string", stringPart())
        schema.putOptionalProperty("zonedDateTime", new NumberSchema())
        schema.putOptionalProperty("mapped", mappedPart())
        schema.putOptionalProperty("listed", listedPart())
        schema.putOptionalProperty("inner", innerPart())
        schema.putOptionalProperty("subinner", subinnerPart())
        return schema
    }

    def stringPart() {
        def schema = new StringSchema()
        schema.setMinLength(1)
        schema.setMaxLength(3)
        return schema
    }

    def subinnerPart() {
        def schema = new ObjectSchema()
        schema.setId(InnerMessage.class.getSimpleName())
        schema.putOptionalProperty("string", new StringSchema())
        schema.putOptionalProperty("longg", new IntegerSchema())
        return schema
    }

    def listedPart() {
        def schema = new ArraySchema()
        schema.setItems(new ArraySchema.SingleItems(new StringSchema()))
        return schema
    }

    def innerPart() {
        def schema = new ObjectSchema()
        schema.setId(InnerMessage.class.getSimpleName())
        schema.putOptionalProperty("string", new StringSchema())
        schema.putOptionalProperty("longg", new IntegerSchema())
        return schema
    }

    def mappedPart() {
        def schema = new ObjectSchema()
        schema.setAdditionalProperties(new ObjectSchema.SchemaAdditionalProperties(innerMappedPart()))
        return schema
    }

    def innerMappedPart() {
        def schema = new ArraySchema()
        schema.setItems(new ArraySchema.SingleItems(innerItemsPart()))
        return schema
    }

    def innerItemsPart() {
        def schema = new ObjectSchema()
        schema.setId(Contract.class.getSimpleName())
        schema.putOptionalProperty("destinationType", destinationTypePart())
        schema.putOptionalProperty("destinationName", new StringSchema())
        schema.putOptionalProperty("message", bodyPart())
        return schema
    }

    def destinationTypePart() {
        def schema = new StringSchema()
        schema.setEnums(Sets.newHashSet("QUEUE", "TOPIC", "TEMPORARY_QUEUE"))
        return schema
    }

    def bodyPart() {
        def schema = new ObjectSchema()
        schema.setId(JsonSchema.class.getSimpleName())
        schema.putOptionalProperty("id", new StringSchema())
        schema.putOptionalProperty("\$ref", new StringSchema())
        schema.putOptionalProperty("\$schema", new StringSchema())
        schema.putOptionalProperty("disallow", disallowPart())
        schema.putOptionalProperty("required", new BooleanSchema())
        schema.putOptionalProperty("readonly", new BooleanSchema())
        schema.putOptionalProperty("description", new StringSchema())
        schema.putOptionalProperty("extends", extendsPart())
        return schema
    }

    def disallowPart() {
        def schema = new ArraySchema()
        schema.setItems(new ArraySchema.SingleItems(new ReferenceSchema("JsonSchema")))
        return schema
    }

    def extendsPart() {
        def schema = new ArraySchema()
        schema.setItems(new ArraySchema.SingleItems(new ReferenceSchema("JsonSchema")))
        return schema
    }


    def expectedResponse() {
        """
        {
            "type":"object",
            "id":"Message",
            "properties":{
                "string":{
                    "type":"string",
                    "maxLength":3,
                    "minLength":1
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
                                    "enum":["TOPIC","TEMPORARY_QUEUE","QUEUE"]
                                },
                                "destinationName":{
                                    "type":"string"
                                },
                                "message":{
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
                },
                "inner":{                
                    "type":"object",
                    "id":"InnerMessage",
                    "properties":{
                        "string":{
                            "type":"string"
                        },
                        "longg":{
                            "type":"integer"
                        }
                    }                
                },
                "subinner":{                
                    "type":"object",
                    "id":"InnerMessage",
                    "properties":{
                        "string":{
                            "type":"string"
                        },
                        "longg":{
                            "type":"integer"
                        }
                    }                
                }
            }
        }       
        """
    }

    def expectedAnotherResponse() {
        """
        {
        
            "type":"object",
            "id":"AnotherMessage",
            "properties":{
                "inner":{
                    "type":"object",
                    "id":"InnerMessage",
                    "properties":{
                        "string":{
                            "type":"string"
                        },
                        "longg":{
                            "type":"integer"
                        }
                    }
                }
            }
        
        } 
        """
    }

    class Message {
        @Size(min = 1, max = 3)
        String string
        ZonedDateTime zonedDateTime
        Multimap<String, Contract> mapped
        List<String> listed
        InnerMessage inner
        InnerMessage subinner
    }

    class AnotherMessage {
        InnerMessage inner
    }

    class InnerMessage {
        String string
        Long longg
    }
}
