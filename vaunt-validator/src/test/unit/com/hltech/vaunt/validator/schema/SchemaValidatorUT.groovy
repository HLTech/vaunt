package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema
import com.fasterxml.jackson.module.jsonSchema.types.NullSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.hltech.vaunt.validator.VauntValidationException
import spock.lang.Specification
import spock.lang.Subject

class SchemaValidatorUT extends Specification {

    @Subject
    def validator = new SchemaValidator()

    def 'Should contain all required validators'() {
        expect:
            validator.schemaValidators.size() == 6
            validator.schemaValidators.any({it -> it.class == StringSchemaValidator})
            validator.schemaValidators.any({it -> it.class == ObjectSchemaValidator})
            validator.schemaValidators.any({it -> it.class == BooleanSchemaValidator})
            validator.schemaValidators.any({it -> it.class == IntegerSchemaValidator})
            validator.schemaValidators.any({it -> it.class == NumberSchemaValidator})
            validator.schemaValidators.any({it -> it.class == ArraySchemaValidator})
    }

    def 'Should return a value when a validator is found for given consumer and provider schemas'() {
        expect:
            validator.validate(new StringSchema(), new StringSchema()) != null
    }

    def 'Should return error when consumer has different schema than provider'() {
        when:
            def errors = validator.validate(new StringSchema(id: 'a'), new IntegerSchema(id: 'b'))

        then:
            errors
            errors.size() == 1
            errors[0] == 'Consumer schema with id a and type StringSchema does not match provider schema with id b and type IntegerSchema'
    }

    def 'Should throw exception when no validator found for given schema'() {
        when:
            def errors = validator.validate(new NullSchema(id: 'a'), new NullSchema(id: 'b'))

        then:
            errors == null
            def ex = thrown VauntValidationException
            ex.getMessage() == 'Exactly one validator should exist for consumer and provider of type NullSchema'
    }
}
