package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import spock.lang.Specification
import spock.lang.Subject

class SchemaValidatorUT extends Specification {

    @Subject
    def validator = new SchemaValidator()

    def 'Should contain all required validators'() {
        expect:
            validator.schemaValidators.size() == 5
            validator.schemaValidators.any({it -> it.class == StringSchemaValidator})
            validator.schemaValidators.any({it -> it.class == ObjectSchemaValidator})
            validator.schemaValidators.any({it -> it.class == BooleanSchemaValidator})
            validator.schemaValidators.any({it -> it.class == IntegerSchemaValidator})
            validator.schemaValidators.any({it -> it.class == NumberSchemaValidator})
    }

    def 'Should return a value when a validator is found for given consumer and provider schemas'() {
        expect:
            validator.validate(new StringSchema(), new StringSchema()) != null
    }

    def 'Should throw exception when no validator is found for given consumer and provider schemas'() {
        when:
            def errors = validator.validate(new StringSchema(id: 'a'), new IntegerSchema(id: 'b'))

        then:
            errors
            errors.size() == 1
            errors[0] == 'Consumer schema with id a and type StringSchema does not match provider schema with id b and type IntegerSchema'
    }
}
