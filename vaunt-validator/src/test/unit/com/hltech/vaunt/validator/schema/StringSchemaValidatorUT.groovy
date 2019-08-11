package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.google.common.collect.Sets
import com.hltech.vaunt.validator.schema.StringSchemaValidator
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class StringSchemaValidatorUT extends Specification {

    @Subject
    def validator = new StringSchemaValidator()

    def 'Should return no errors for the same StringSchemas'() {
        given:
            StringSchema consumerSchema = getSampleSchema()
            StringSchema providerSchema = getSampleSchema()

        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0
    }

    @Unroll
    def 'Should return error for consumer StringSchema being different in #field from producer StringSchema'() {
        when:
            def resultList = validator.validate(consumerSchema, providerSchema)

        then:
            resultList.size() == 1
            resultList.get(0) == error

        where:
            field         | consumerSchema                                                                 | providerSchema                                                                 | error
            'minLength'   | new StringSchema(id: 'a', minLength: 1)                                        | new StringSchema(id: 'a', minLength: 2)                                        | 'Schema with id a has not matching minLength - consumer: 1, provider: 2'
            'maxLength'   | new StringSchema(id: 'a', maxLength: 1)                                        | new StringSchema(id: 'a', maxLength: 2)                                        | 'Schema with id a has not matching maxLength - consumer: 1, provider: 2'
            'pattern'     | new StringSchema(id: 'a', pattern: 'ab')                                       | new StringSchema(id: 'a', pattern: 'cd')                                       | 'Schema with id a has not matching pattern - consumer: ab, provider: cd'
            'enums'       | new StringSchema(id: 'a', enums: ['ab'])                                       | new StringSchema(id: 'a', enums: ['cd'])                                       | 'Schema with id a has not matching enums - consumer: [ab], provider: [cd]'
            'format'      | new StringSchema(id: 'a', format: JsonValueFormat.DATE)                        | new StringSchema(id: 'a', format: JsonValueFormat.DATE_TIME)                   | 'Schema with id a has not matching format - consumer: date, provider: date-time'
            'default'     | new StringSchema(id: 'a', defaultdefault: 'ab')                                | new StringSchema(id: 'a', defaultdefault: 'cd')                                | 'Schema with id a has not matching default - consumer: ab, provider: cd'
            'links'       | new StringSchema(id: 'a', links: [new LinkDescriptionObject(href: 'ab')])      | new StringSchema(id: 'a', links: [new LinkDescriptionObject(href: 'cd')])      | 'Schema with id a has not matching links'
            'pathStart'   | new StringSchema(id: 'a', pathStart: 'ab')                                     | new StringSchema(id: 'a', pathStart: 'cd')                                     | 'Schema with id a has not matching pathStart - consumer: ab, provider: cd'
            'title'       | new StringSchema(id: 'a', title: 'ab')                                         | new StringSchema(id: 'a', title: 'cd')                                         | 'Schema with id a has not matching title - consumer: ab, provider: cd'
            '$ref'        | new StringSchema(id: 'a', $ref: 'ab')                                          | new StringSchema(id: 'a', $ref: 'cd')                                          | 'Schema with id a has not matching $ref - consumer: ab, provider: cd'
            '$schema'     | new StringSchema(id: 'a', $schema: 'ab')                                       | new StringSchema(id: 'a', $schema: 'cd')                                       | 'Schema with id a has not matching $schema - consumer: ab, provider: cd'
            'disallow'    | new StringSchema(id: 'a', disallow: [new StringSchema(id: 'ab', title: 'ab')]) | new StringSchema(id: 'a', disallow: [new StringSchema(id: 'cd', title: 'cd')]) | 'Schema with id a has not matching disallow - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'extends'     | getSchemaWithExtended('ab')                                                    | getSchemaWithExtended('cd')                                                    | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'required'    | new StringSchema(id: 'a', required: true)                                      | new StringSchema(id: 'a', required: false)                                     | 'Schema with id a has not matching required - consumer: true, provider: false'
            'readonly'    | new StringSchema(id: 'a', readonly: true)                                      | new StringSchema(id: 'a', readonly: false)                                     | 'Schema with id a has not matching readonly - consumer: true, provider: false'
            'description' | new StringSchema(id: 'a', description: 'ab')                                   | new StringSchema(id: 'a', description: 'cd')                                   | 'Schema with id a has not matching description - consumer: ab, provider: cd'
    }

    @Unroll
    def 'Should return errors #errors when ValueTypeSchema of provider contains #providerEnums enums and consumer #consumerEnums'() {
        given:
            def consumerSchema = new StringSchema()
            consumerSchema.setId('a')
            consumerSchema.setEnums(consumerEnums)

            def providerSchema = new StringSchema()
            providerSchema.setId('a')
            providerSchema.setEnums(providerEnums)

        expect:
            validator.validate(consumerSchema, providerSchema) == errors

        where:
            consumerEnums                               | providerEnums                                | errors
            Sets.newHashSet('abc', 'def', 'geh')        | Sets.newHashSet('abc', 'def', 'geh', 'ijk')  | []
            Sets.newHashSet('abc', 'def', 'geh', 'ijk') | Sets.newHashSet('abc', 'def', 'geh')         | ['Schema with id a has not matching enums - consumer: ' + consumerEnums + ', provider: ' + providerEnums]
            Sets.newHashSet('abc', 'def', 'geh')        | new HashSet<String>()                        | []
            new HashSet<String>()                       | Sets.newHashSet('abc', 'def', 'geh')         | ['Schema with id a has not matching enums - consumer: ' + consumerEnums + ', provider: ' + providerEnums]
    }

    def getSampleSchema() {
        def schema = new StringSchema(
                minLength: 1,
                maxLength: 4,
                pattern: 'ab',
                enums: ['a', 'b'],
                format: JsonValueFormat.DATE_TIME,
                defaultdefault: 'ab',
                links: [new LinkDescriptionObject(href: 'abc')],
                pathStart: 'ab',
                title: 'ab',
                $ref: 'ab',
                $schema: 'ab',
                disallow: [new StringSchema(id: 'ab')],
                required: true,
                readonly: true,
                description: 'ab'
        )
        schema.setExtends(getSchemaWithExtended('ab'))
        return schema
    }

    def getSchemaWithExtended(String id) {
        def schema = new StringSchema(id: 'a')
        JsonSchema[] schemas = [new StringSchema(id: id, title: id)]
        schema.setExtends(schemas)
        return schema
    }
}
