package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject
import com.fasterxml.jackson.module.jsonSchema.types.BooleanSchema
import com.google.common.collect.Sets
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class BooleanSchemaValidatorUT extends Specification {

    @Subject
    def validator = new BooleanSchemaValidator()

    def 'Should return no errors for the same BooleanSchemas'() {
        given:
            BooleanSchema consumerSchema = getSampleSchema()
            BooleanSchema providerSchema = getSampleSchema()

        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0
    }

    @Unroll
    def 'Should return error for consumer BooleanSchema being different in #field from producer BooleanSchema'() {
        when:
            def resultList = validator.validate(consumerSchema, providerSchema)

        then:
            resultList.size() == 1
            resultList.get(0) == error

        where:
            field         | consumerSchema                                                                   | providerSchema                                                                   | error
            'enums'       | new BooleanSchema(id: 'a', enums: ['ab'])                                        | new BooleanSchema(id: 'a', enums: ['cd'])                                        | 'Schema with id a has not matching enums - consumer: [ab], provider: [cd]'
            'format'      | new BooleanSchema(id: 'a', format: JsonValueFormat.DATE)                         | new BooleanSchema(id: 'a', format: JsonValueFormat.DATE_TIME)                    | 'Schema with id a has not matching format - consumer: date, provider: date-time'
            'default'     | new BooleanSchema(id: 'a', defaultdefault: 'ab')                                 | new BooleanSchema(id: 'a', defaultdefault: 'cd')                                 | 'Schema with id a has not matching default - consumer: ab, provider: cd'
            'links'       | new BooleanSchema(id: 'a', links: [new LinkDescriptionObject(href: 'ab')])       | new BooleanSchema(id: 'a', links: [new LinkDescriptionObject(href: 'cd')])       | 'Schema with id a has not matching links'
            'pathStart'   | new BooleanSchema(id: 'a', pathStart: 'ab')                                      | new BooleanSchema(id: 'a', pathStart: 'cd')                                      | 'Schema with id a has not matching pathStart - consumer: ab, provider: cd'
            'title'       | new BooleanSchema(id: 'a', title: 'ab')                                          | new BooleanSchema(id: 'a', title: 'cd')                                          | 'Schema with id a has not matching title - consumer: ab, provider: cd'
            '$ref'        | new BooleanSchema(id: 'a', $ref: 'ab')                                           | new BooleanSchema(id: 'a', $ref: 'cd')                                           | 'Schema with id a has not matching $ref - consumer: ab, provider: cd'
            '$schema'     | new BooleanSchema(id: 'a', $schema: 'ab')                                        | new BooleanSchema(id: 'a', $schema: 'cd')                                        | 'Schema with id a has not matching $schema - consumer: ab, provider: cd'
            'disallow'    | new BooleanSchema(id: 'a', disallow: [new BooleanSchema(id: 'ab', title: 'ab')]) | new BooleanSchema(id: 'a', disallow: [new BooleanSchema(id: 'cd', title: 'cd')]) | 'Schema with id a has not matching disallow - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'extends'     | getSchemaWithExtended('ab')                                                      | getSchemaWithExtended('cd')                                                      | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'required'    | new BooleanSchema(id: 'a', required: true)                                       | new BooleanSchema(id: 'a', required: false)                                      | 'Schema with id a has not matching required - consumer: true, provider: false'
            'readonly'    | new BooleanSchema(id: 'a', readonly: true)                                       | new BooleanSchema(id: 'a', readonly: false)                                      | 'Schema with id a has not matching readonly - consumer: true, provider: false'
            'description' | new BooleanSchema(id: 'a', description: 'ab')                                    | new BooleanSchema(id: 'a', description: 'cd')                                    | 'Schema with id a has not matching description - consumer: ab, provider: cd'
    }

    @Unroll
    def 'Should return errors #errors when ValueTypeSchema of provider contains #providerEnums enums and consumer #consumerEnums'() {
        given:
            def consumerSchema = new BooleanSchema()
            consumerSchema.setId('a')
            consumerSchema.setEnums(consumerEnums)

            def providerSchema = new BooleanSchema()
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
            def consumerSchema = new BooleanSchema()
            consumerSchema.setRequired(false)

            def providerSchema = new BooleanSchema()
            providerSchema.setRequired(true)

        expect:
            validator.validate(consumerSchema, providerSchema) == []
    }

    def getSampleSchema() {
        def schema = new BooleanSchema(
                enums: ['a', 'b'],
                format: JsonValueFormat.DATE_TIME,
                defaultdefault: 'ab',
                links: [new LinkDescriptionObject(href: 'abc')],
                pathStart: 'ab',
                title: 'ab',
                $ref: 'ab',
                $schema: 'ab',
                disallow: [new BooleanSchema(id: 'ab')],
                required: true,
                readonly: true,
                description: 'ab'
        )
        schema.setExtends(getSchemaWithExtended('ab'))
        return schema
    }

    def getSchemaWithExtended(String id) {
        def schema = new BooleanSchema(id: 'a')
        JsonSchema[] schemas = [new BooleanSchema(id: id, title: id)]
        schema.setExtends(schemas)
        return schema
    }
}
