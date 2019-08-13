package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.google.common.collect.Sets
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ArraySchemaValidatorUT extends Specification {

    @Subject
    def validator = new ArraySchemaValidator()

    def 'Should return no errors for the same ArraySchemas'() {
        given:
            ArraySchema consumerSchema = getSampleSchema()
            ArraySchema providerSchema = getSampleSchema()

        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0
    }

    @Unroll
    def 'Should return error for consumer ArraySchema being different in #field from producer ArraySchema'() {
        when:
            def resultList = validator.validate(consumerSchema, providerSchema)

        then:
            resultList.size() == 1
            resultList.get(0) == error

        where:
            field                  | consumerSchema                                                                | providerSchema                                                                   | error
            'additionalItems'      | new ArraySchema(id: 'a', additionalItems: getAdditionalItems('id'))           | new ArraySchema(id: 'a', additionalItems: getAdditionalItems('id2'))             | 'Schema with id a has not matching additionalItems'
            'items'                | new ArraySchema(id: 'a', items: getItems('id'))                               | new ArraySchema(id: 'a', items: getItems('id2'))                                 | 'Schema with id a has not matching items'
            'maxItems'             | new ArraySchema(id: 'a', maxItems: 1)                                         | new ArraySchema(id: 'a', maxItems: 2)                                            | 'Schema with id a has not matching maxItems - consumer: 1, provider: 2'
            'minItems'             | new ArraySchema(id: 'a', minItems: 1)                                         | new ArraySchema(id: 'a', minItems: 2)                                            | 'Schema with id a has not matching minItems - consumer: 1, provider: 2'
            'uniqueItems'          | new ArraySchema(id: 'a', uniqueItems: true)                                   | new ArraySchema(id: 'a', uniqueItems: false)                                     | 'Schema with id a has not matching uniqueItems - consumer: true, provider: false'
            'enums'                | new ArraySchema(id: 'a', enums: ['ab'])                                       | new ArraySchema(id: 'a', enums: ['cd'])                                          | 'Schema with id a has not matching enums - consumer: [ab], provider: [cd]'
            'format'               | new ArraySchema(id: 'a', oneOf: ['ab'])                                       | new ArraySchema(id: 'a', oneOf: ['cd'])                                          | 'Schema with id a has not matching oneOf - consumer: [ab], provider: [cd]'
            'default'              | new ArraySchema(id: 'a', defaultdefault: 'ab')                                | new ArraySchema(id: 'a', defaultdefault: 'cd')                                   | 'Schema with id a has not matching default - consumer: ab, provider: cd'
            'links'                | new ArraySchema(id: 'a', links: [new LinkDescriptionObject(href: 'ab')])      | new ArraySchema(id: 'a', links: [new LinkDescriptionObject(href: 'cd')])         | 'Schema with id a has not matching links'
            'pathStart'            | new ArraySchema(id: 'a', pathStart: 'ab')                                     | new ArraySchema(id: 'a', pathStart: 'cd')                                        | 'Schema with id a has not matching pathStart - consumer: ab, provider: cd'
            'title'                | new ArraySchema(id: 'a', title: 'ab')                                         | new ArraySchema(id: 'a', title: 'cd')                                            | 'Schema with id a has not matching title - consumer: ab, provider: cd'
            '$ref'                 | new ArraySchema(id: 'a', $ref: 'ab')                                          | new ArraySchema(id: 'a', $ref: 'cd')                                             | 'Schema with id a has not matching $ref - consumer: ab, provider: cd'
            '$schema'              | new ArraySchema(id: 'a', $schema: 'ab')                                       | new ArraySchema(id: 'a', $schema: 'cd')                                          | 'Schema with id a has not matching $schema - consumer: ab, provider: cd'
            'disallow'             | new ArraySchema(id: 'a', disallow: [new StringSchema(id: 'ab', title: 'ab')]) | new ArraySchema(id: 'a', disallow: [new StringSchema(id: 'cd', title: 'cd')])    | 'Schema with id a has not matching disallow - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'extends'              | getSchemaWithExtended('ab')                                                   | getSchemaWithExtended('cd')                                                      | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'required'             | new ArraySchema(id: 'a', required: true)                                      | new ArraySchema(id: 'a', required: false)                                        | 'Schema with id a has not matching required - consumer: true, provider: false'
            'readonly'             | new ArraySchema(id: 'a', readonly: true)                                      | new ArraySchema(id: 'a', readonly: false)                                        | 'Schema with id a has not matching readonly - consumer: true, provider: false'
            'description'          | new ArraySchema(id: 'a', description: 'ab')                                   | new ArraySchema(id: 'a', description: 'cd')                                      | 'Schema with id a has not matching description - consumer: ab, provider: cd'
    }

    @Unroll
    def 'Should return errors #errors when ContainerTypeSchema of provider contains #providerEnums enums and consumer #consumerEnums'() {
        given:
            def consumerSchema = new ArraySchema()
            consumerSchema.setId('a')
            consumerSchema.setEnums(consumerEnums)

            def providerSchema = new ArraySchema()
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

    def 'Should pass validation when provider schema is required and consumer is not'() {
        given:
            def consumerSchema = new ArraySchema()
            consumerSchema.setRequired(false)

            def providerSchema = new ArraySchema()
            providerSchema.setRequired(true)

        expect:
            validator.validate(consumerSchema, providerSchema) == []
    }

    def getSampleSchema() {
        def schema = new ArraySchema(
                additionalItems: getAdditionalItems('id'),
                items: getItems('id'),
                maxItems: 1,
                minItems: 1,
                uniqueItems: true,
                enums: ['a', 'b'],
                oneOf: ['a', 'b'],
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

    def getAdditionalItems(String id) {
        return new ArraySchema.SchemaAdditionalItems(getSchemaWithExtended(id))
    }

    def getItems(String id) {
        return new ArraySchema.SingleItems(getSchemaWithExtended(id))
    }

    def getSchemaWithExtended(String id) {
        def schema = new ArraySchema(id: 'a')
        JsonSchema[] schemas = [new StringSchema(id: id, title: id)]
        schema.setExtends(schemas)
        return schema
    }
}
