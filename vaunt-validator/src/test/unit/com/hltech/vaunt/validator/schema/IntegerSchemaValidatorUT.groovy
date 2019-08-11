package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject
import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema
import com.google.common.collect.Sets
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class IntegerSchemaValidatorUT extends Specification {

    @Subject
    def validator = new IntegerSchemaValidator()

    def 'Should return no errors for the same IntegerSchemas'() {
        given:
            IntegerSchema consumerSchema = getSampleSchema()
            IntegerSchema providerSchema = getSampleSchema()

        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0
    }

    @Unroll
    def 'Should return error for consumer IntegerSchema being different in #field from producer IntegerSchema'() {
        when:
            def resultList = validator.validate(consumerSchema, providerSchema)

        then:
            resultList.size() == 1
            resultList.get(0) == error

        where:
            field              | consumerSchema                                                                   | providerSchema                                                                   | error
            'divisibleBy'      | new IntegerSchema(id: 'a', divisibleBy: 1)                                       | new IntegerSchema(id: 'a', divisibleBy: 3)                                       | 'Schema with id a has not matching divisibleBy - consumer: 1, provider: 3'
            'exclusiveMaximum' | new IntegerSchema(id: 'a', exclusiveMaximum: true)                               | new IntegerSchema(id: 'a', exclusiveMaximum: false)                              | 'Schema with id a has not matching exclusiveMaximum - consumer: true, provider: false'
            'exclusiveMinimum' | new IntegerSchema(id: 'a', exclusiveMinimum: true)                               | new IntegerSchema(id: 'a', exclusiveMinimum: false)                              | 'Schema with id a has not matching exclusiveMinimum - consumer: true, provider: false'
            'maximum'          | new IntegerSchema(id: 'a', maximum: 2)                                           | new IntegerSchema(id: 'a', maximum: 4)                                           | 'Schema with id a has not matching maximum - consumer: 2.0, provider: 4.0'
            'minimum'          | new IntegerSchema(id: 'a', minimum: 1)                                           | new IntegerSchema(id: 'a', minimum: 3)                                           | 'Schema with id a has not matching minimum - consumer: 1.0, provider: 3.0'
            'multipleOf'       | new IntegerSchema(id: 'a', multipleOf: 1)                                        | new IntegerSchema(id: 'a', multipleOf: 3)                                        | 'Schema with id a has not matching multipleOf - consumer: 1.0, provider: 3.0'
            'enums'            | new IntegerSchema(id: 'a', enums: ['ab'])                                        | new IntegerSchema(id: 'a', enums: ['cd'])                                        | 'Schema with id a has not matching enums - consumer: [ab], provider: [cd]'
            'format'           | new IntegerSchema(id: 'a', format: JsonValueFormat.DATE)                         | new IntegerSchema(id: 'a', format: JsonValueFormat.DATE_TIME)                    | 'Schema with id a has not matching format - consumer: date, provider: date-time'
            'default'          | new IntegerSchema(id: 'a', defaultdefault: 'ab')                                 | new IntegerSchema(id: 'a', defaultdefault: 'cd')                                 | 'Schema with id a has not matching default - consumer: ab, provider: cd'
            'links'            | new IntegerSchema(id: 'a', links: [new LinkDescriptionObject(href: 'ab')])       | new IntegerSchema(id: 'a', links: [new LinkDescriptionObject(href: 'cd')])       | 'Schema with id a has not matching links'
            'pathStart'        | new IntegerSchema(id: 'a', pathStart: 'ab')                                      | new IntegerSchema(id: 'a', pathStart: 'cd')                                      | 'Schema with id a has not matching pathStart - consumer: ab, provider: cd'
            'title'            | new IntegerSchema(id: 'a', title: 'ab')                                          | new IntegerSchema(id: 'a', title: 'cd')                                          | 'Schema with id a has not matching title - consumer: ab, provider: cd'
            '$ref'             | new IntegerSchema(id: 'a', $ref: 'ab')                                           | new IntegerSchema(id: 'a', $ref: 'cd')                                           | 'Schema with id a has not matching $ref - consumer: ab, provider: cd'
            '$schema'          | new IntegerSchema(id: 'a', $schema: 'ab')                                        | new IntegerSchema(id: 'a', $schema: 'cd')                                        | 'Schema with id a has not matching $schema - consumer: ab, provider: cd'
            'disallow'         | new IntegerSchema(id: 'a', disallow: [new IntegerSchema(id: 'ab', title: 'ab')]) | new IntegerSchema(id: 'a', disallow: [new IntegerSchema(id: 'cd', title: 'cd')]) | 'Schema with id a has not matching disallow - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'extends'          | getSchemaWithExtended('ab')                                                      | getSchemaWithExtended('cd')                                                      | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'required'         | new IntegerSchema(id: 'a', required: true)                                       | new IntegerSchema(id: 'a', required: false)                                      | 'Schema with id a has not matching required - consumer: true, provider: false'
            'readonly'         | new IntegerSchema(id: 'a', readonly: true)                                       | new IntegerSchema(id: 'a', readonly: false)                                      | 'Schema with id a has not matching readonly - consumer: true, provider: false'
            'description'      | new IntegerSchema(id: 'a', description: 'ab')                                    | new IntegerSchema(id: 'a', description: 'cd')                                    | 'Schema with id a has not matching description - consumer: ab, provider: cd'
    }

    @Unroll
    def 'Should return errors #errors when ValueTypeSchema of provider contains #providerEnums enums and consumer #consumerEnums'() {
        given:
            def consumerSchema = new IntegerSchema()
            consumerSchema.setId('a')
            consumerSchema.setEnums(consumerEnums)

            def providerSchema = new IntegerSchema()
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

    def getSampleSchema() {
        def schema = new IntegerSchema(
                divisibleBy: 1,
                exclusiveMaximum: 4,
                exclusiveMinimum: 'ab',
                maximum: 2,
                minimum: 1,
                multipleOf: 1,
                enums: ['a', 'b'],
                format: JsonValueFormat.DATE_TIME,
                defaultdefault: 'ab',
                links: [new LinkDescriptionObject(href: 'abc')],
                pathStart: 'ab',
                title: 'ab',
                $ref: 'ab',
                $schema: 'ab',
                disallow: [new IntegerSchema(id: 'ab')],
                required: true,
                readonly: true,
                description: 'ab'
        )
        schema.setExtends(getSchemaWithExtended('ab'))
        return schema
    }

    def getSchemaWithExtended(String id) {
        def schema = new IntegerSchema(id: 'a')
        JsonSchema[] schemas = [new IntegerSchema(id: id, title: id)]
        schema.setExtends(schemas)
        return schema
    }
}
