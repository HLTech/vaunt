package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.NumberSchema
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject
import com.google.common.collect.Sets
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class NumberSchemaValidatorUT extends Specification {

    @Subject
    def validator = new NumberSchemaValidator()

    def 'Should return no errors for the same NumberSchemas'() {
        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0

        where:
            consumerSchema     | providerSchema
            new NumberSchema() | getSampleSchema()
            getSampleSchema()  | getSampleSchema()
    }

    @Unroll
    def 'Should return error for consumer NumberSchema being different in #field from producer NumberSchema'() {
        when:
            def resultList = validator.validate(consumerSchema, providerSchema)

        then:
            resultList.size() == 1
            resultList.get(0) == error

        where:
            field              | consumerSchema                                                             | providerSchema                                                             | error
            'exclusiveMaximum' | new NumberSchema(id: 'a', exclusiveMaximum: true)                               | new NumberSchema(id: 'a', exclusiveMaximum: false)                              | 'Schema with id a has not matching exclusiveMaximum - consumer: true, provider: false'
            'exclusiveMinimum' | new NumberSchema(id: 'a', exclusiveMinimum: true)                               | new NumberSchema(id: 'a', exclusiveMinimum: false)                              | 'Schema with id a has not matching exclusiveMinimum - consumer: true, provider: false'
            'maximum'          | new NumberSchema(id: 'a', maximum: 2)                                           | new NumberSchema(id: 'a', maximum: 4)                                           | 'Schema with id a has not matching maximum - consumer: 2.0, provider: 4.0'
            'minimum'          | new NumberSchema(id: 'a', minimum: 1)                                           | new NumberSchema(id: 'a', minimum: 3)                                           | 'Schema with id a has not matching minimum - consumer: 1.0, provider: 3.0'
            'multipleOf'       | new NumberSchema(id: 'a', multipleOf: 1)                                  | new NumberSchema(id: 'a', multipleOf: 3)                                  | 'Schema with id a has not matching multipleOf - consumer: 1.0, provider: 3.0'
            'enums'            | new NumberSchema(id: 'a', enums: ['ab'])                                  | new NumberSchema(id: 'a', enums: ['cd'])                                  | 'Schema with id a has not matching enums - consumer: [ab], provider: [cd]'
            'format'           | new NumberSchema(id: 'a', format: JsonValueFormat.DATE)                   | new NumberSchema(id: 'a', format: JsonValueFormat.DATE_TIME)              | 'Schema with id a has not matching format - consumer: date, provider: date-time'
            'default'          | new NumberSchema(id: 'a', defaultdefault: 'ab')                           | new NumberSchema(id: 'a', defaultdefault: 'cd')                           | 'Schema with id a has not matching default - consumer: ab, provider: cd'
            'links'            | new NumberSchema(id: 'a', links: [new LinkDescriptionObject(href: 'ab')]) | new NumberSchema(id: 'a', links: [new LinkDescriptionObject(href: 'cd')]) | 'Schema with id a has not matching links'
            'pathStart'        | new NumberSchema(id: 'a', pathStart: 'ab')                                | new NumberSchema(id: 'a', pathStart: 'cd')                                | 'Schema with id a has not matching pathStart - consumer: ab, provider: cd'
            'title'            | new NumberSchema(id: 'a', title: 'ab')                                    | new NumberSchema(id: 'a', title: 'cd')                                    | 'Schema with id a has not matching title - consumer: ab, provider: cd'
            '$ref'             | new NumberSchema(id: 'a', $ref: 'ab')                                           | new NumberSchema(id: 'a', $ref: 'cd')                                           | 'Schema with id a has not matching $ref - consumer: ab, provider: cd'
            '$schema'          | new NumberSchema(id: 'a', $schema: 'ab')                                        | new NumberSchema(id: 'a', $schema: 'cd')                                        | 'Schema with id a has not matching $schema - consumer: ab, provider: cd'
            'disallow'         | new NumberSchema(id: 'a', disallow: [new NumberSchema(id: 'ab', title: 'ab')]) | new NumberSchema(id: 'a', disallow: [new NumberSchema(id: 'cd', title: 'cd')]) | 'Schema with id a has not matching disallow - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'extends'          | getSchemaWithExtended('ab')                                                      | getSchemaWithExtended('cd')                                                      | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'required'         | new NumberSchema(id: 'a', required: true)                                       | new NumberSchema(id: 'a', required: false)                                      | 'Schema with id a has not matching required - consumer: true, provider: false'
            'readonly'         | new NumberSchema(id: 'a', readonly: true)                                       | new NumberSchema(id: 'a', readonly: false)                                      | 'Schema with id a has not matching readonly - consumer: true, provider: false'
            'description'      | new NumberSchema(id: 'a', description: 'ab')                                    | new NumberSchema(id: 'a', description: 'cd')                                    | 'Schema with id a has not matching description - consumer: ab, provider: cd'
    }

    @Unroll
    def 'Should return errors #errors when ValueTypeSchema of provider contains #providerEnums enums and consumer #consumerEnums'() {
        given:
            def consumerSchema = new NumberSchema()
            consumerSchema.setId('a')
            consumerSchema.setEnums(consumerEnums)

            def providerSchema = new NumberSchema()
            providerSchema.setId('a')
            providerSchema.setEnums(providerEnums)

        expect:
            validator.validate(consumerSchema, providerSchema) == errors

        where:
            consumerEnums                               | providerEnums                               | errors
            Sets.newHashSet('abc', 'def', 'geh')        | Sets.newHashSet('abc', 'def', 'geh', 'ijk') | []
            Sets.newHashSet('abc', 'def', 'geh', 'ijk') | Sets.newHashSet('abc', 'def', 'geh')        | ['Schema with id a has not matching enums - consumer: ' + consumerEnums + ', provider: ' + providerEnums]
            Sets.newHashSet('abc', 'def', 'geh')        | new HashSet<String>()                       | []
            new HashSet<String>()                       | Sets.newHashSet('abc', 'def', 'geh')        | ['Schema with id a has not matching enums - consumer: ' + consumerEnums + ', provider: ' + providerEnums]
    }

    def 'Should pass validation when provider schema is required and consumer is not'() {
        given:
            def consumerSchema = new NumberSchema()
            consumerSchema.setRequired(false)

            def providerSchema = new NumberSchema()
            providerSchema.setRequired(true)

        expect:
            validator.validate(consumerSchema, providerSchema) == []
    }

    def getSampleSchema() {
        def schema = new NumberSchema(
                exclusiveMaximum: 4,
                exclusiveMinimum: 'ab',
                maximum: 2,
                minimum: 1,
                multipleOf: 1,
                format: JsonValueFormat.DATE_TIME,
                defaultdefault: 'ab',
                links: [new LinkDescriptionObject(href: 'abc')],
                pathStart: 'ab',
                title: 'ab',
                $ref: 'ab',
                $schema: 'ab',
                disallow: [new NumberSchema(id: 'ab')],
                required: true,
                readonly: true,
                description: 'ab'
        )
        schema.setExtends(getSchemaWithExtended('ab'))
        return schema
    }

    def getSchemaWithExtended(String id) {
        def schema = new NumberSchema(id: 'a')
        JsonSchema[] schemas = [new NumberSchema(id: id, title: id)]
        schema.setExtends(schemas)
        return schema
    }
}
