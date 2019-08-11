package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.google.common.collect.Sets
import com.hltech.vaunt.validator.schema.ObjectSchemaValidator
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class ObjectSchemaValidatorUT extends Specification {

    @Subject
    def validator = new ObjectSchemaValidator()

    def 'Should return no errors for the same ObjectSchemas'() {
        given:
            ObjectSchema consumerSchema = getSampleSchema()
            ObjectSchema providerSchema = getSampleSchema()

        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0
    }

    @Unroll
    def 'Should return error for consumer ObjectSchema being different in #field from producer ObjectSchema'() {
        when:
            def resultList = validator.validate(consumerSchema, providerSchema)

        then:
            resultList.size() == 1
            resultList.get(0) == error

        where:
            field                  | consumerSchema                                                                    | providerSchema                                                                    | error
            'additionalProperties' | new ObjectSchema(id: 'a', additionalProperties: getAdditionalProperty('id'))      | new ObjectSchema(id: 'a', additionalProperties: getAdditionalProperty('di'))      | 'Schema with id a has not matching additionalProperties'
            'dependencies'         | new ObjectSchema(id: 'a', dependencies: ['ab': 'ab'])                             | new ObjectSchema(id: 'a', dependencies: ['ab': 'cd'])                             | 'Schema with id a has not matching dependencies - consumer: {ab=ab}, provider: {ab=cd}'
            'patternProperties'    | new ObjectSchema(id: 'a', patternProperties: ['ab': getSchemaWithExtended('id')]) | new ObjectSchema(id: 'a', patternProperties: ['ab': getSchemaWithExtended('di')]) | 'Schema with id a has not matching patternProperties - consumer: {ab=JsonSchema(id=a)}, provider: {ab=JsonSchema(id=a)}'
            'properties'           | new ObjectSchema(id: 'a', properties: ['ab': getSchemaWithExtended('id')])        | new ObjectSchema(id: 'a', properties: ['ab': getSchemaWithExtended('di')])        | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=id)], provider: [JsonSchema(id=di)]'
            'enums'                | new ObjectSchema(id: 'a', enums: ['ab'])                                          | new ObjectSchema(id: 'a', enums: ['cd'])                                          | 'Schema with id a has not matching enums - consumer: [ab], provider: [cd]'
            'format'               | new ObjectSchema(id: 'a', oneOf: ['ab'])                                          | new ObjectSchema(id: 'a', oneOf: ['cd'])                                          | 'Schema with id a has not matching oneOf - consumer: [ab], provider: [cd]'
            'default'              | new ObjectSchema(id: 'a', defaultdefault: 'ab')                                   | new ObjectSchema(id: 'a', defaultdefault: 'cd')                                   | 'Schema with id a has not matching default - consumer: ab, provider: cd'
            'links'                | new ObjectSchema(id: 'a', links: [new LinkDescriptionObject(href: 'ab')])         | new ObjectSchema(id: 'a', links: [new LinkDescriptionObject(href: 'cd')])         | 'Schema with id a has not matching links'
            'pathStart'            | new ObjectSchema(id: 'a', pathStart: 'ab')                                        | new ObjectSchema(id: 'a', pathStart: 'cd')                                        | 'Schema with id a has not matching pathStart - consumer: ab, provider: cd'
            'title'                | new ObjectSchema(id: 'a', title: 'ab')                                            | new ObjectSchema(id: 'a', title: 'cd')                                            | 'Schema with id a has not matching title - consumer: ab, provider: cd'
            '$ref'                 | new ObjectSchema(id: 'a', $ref: 'ab')                                             | new ObjectSchema(id: 'a', $ref: 'cd')                                             | 'Schema with id a has not matching $ref - consumer: ab, provider: cd'
            '$schema'              | new ObjectSchema(id: 'a', $schema: 'ab')                                          | new ObjectSchema(id: 'a', $schema: 'cd')                                          | 'Schema with id a has not matching $schema - consumer: ab, provider: cd'
            'disallow'             | new ObjectSchema(id: 'a', disallow: [new StringSchema(id: 'ab', title: 'ab')])    | new ObjectSchema(id: 'a', disallow: [new StringSchema(id: 'cd', title: 'cd')])    | 'Schema with id a has not matching disallow - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'extends'              | getSchemaWithExtended('ab')                                                       | getSchemaWithExtended('cd')                                                       | 'Schema with id a has not matching extends - consumer: [JsonSchema(id=ab)], provider: [JsonSchema(id=cd)]'
            'required'             | new ObjectSchema(id: 'a', required: true)                                         | new ObjectSchema(id: 'a', required: false)                                        | 'Schema with id a has not matching required - consumer: true, provider: false'
            'readonly'             | new ObjectSchema(id: 'a', readonly: true)                                         | new ObjectSchema(id: 'a', readonly: false)                                        | 'Schema with id a has not matching readonly - consumer: true, provider: false'
            'description'          | new ObjectSchema(id: 'a', description: 'ab')                                      | new ObjectSchema(id: 'a', description: 'cd')                                      | 'Schema with id a has not matching description - consumer: ab, provider: cd'
    }

    @Unroll
    def 'Should return errors #errors when ContainerTypeSchema of provider contains #providerEnums enums and consumer #consumerEnums'() {
        given:
            def consumerSchema = new ObjectSchema()
            consumerSchema.setId('a')
            consumerSchema.setEnums(consumerEnums)

            def providerSchema = new ObjectSchema()
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

    def 'Should return no errors when ObjectSchema of provider contains superset of consumer properties'() {
        given:
            def consumerSchema = new ObjectSchema()
            consumerSchema.setId('a')
            consumerSchema.setProperties(['a': getSchemaWithExtended('ab')])

            def providerSchema = new ObjectSchema()
            providerSchema.setId('a')
            providerSchema.setProperties(['a': getSchemaWithExtended('ab'), 'c': getSchemaWithExtended('cd')])

        expect:
            validator.validate(consumerSchema, providerSchema).size() == 0
    }

    def 'Should return error when ObjectSchema of consumer contains superset of provider properties'() {
        given:
            def consumerSchema = new ObjectSchema()
            consumerSchema.setId('a')
            consumerSchema.setProperties(['a': getSchemaWithExtended('ab'), 'c': getSchemaWithExtended('cd')])

            def providerSchema = new ObjectSchema()
            providerSchema.setId('a')
            providerSchema.setProperties(['a': getSchemaWithExtended('ab')])

        expect:
            validator.validate(consumerSchema, providerSchema) == ['Schema with id a has not matching properties - consumer: ids of properties: [a, c], provider: ids of properties: [a]']
    }

    def 'Should pass validation when provider schema is required and consumer is not'() {
        given:
            def consumerSchema = new ObjectSchema()
            consumerSchema.setRequired(false)

            def providerSchema = new ObjectSchema()
            providerSchema.setRequired(true)

        expect:
            validator.validate(consumerSchema, providerSchema) == []
    }

    def getSampleSchema() {
        def schema = new ObjectSchema(
                additionalProperties: getAdditionalProperty('id'),
                dependencies: ['ab': 'ab'],
                patternProperties: ['ab': getSchemaWithExtended('id')],
                properties: ['ab': getSchemaWithExtended('id')],
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

    def getAdditionalProperty(String id) {
        return new ObjectSchema.SchemaAdditionalProperties(getSchemaWithExtended(id))
    }

    def getSchemaWithExtended(String id) {
        def schema = new ObjectSchema(id: 'a')
        JsonSchema[] schemas = [new StringSchema(id: id, title: id)]
        schema.setExtends(schemas)
        return schema
    }
}
