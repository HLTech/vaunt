package com.hltech.vaunt.validator.schema

import com.fasterxml.jackson.module.jsonSchema.types.IntegerSchema
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema
import com.hltech.vaunt.validator.VauntValidationException
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
            def response = validator.validate(new StringSchema(), new IntegerSchema())

        then:
            response == null
            def ex = thrown VauntValidationException
            ex.getMessage() == 'Exactly one validator should exist for consumer and provider'
    }
}
