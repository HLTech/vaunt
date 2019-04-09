package com.hltech.vaunt.validator

import spock.lang.Specification

class AppSpec extends Specification {

    def 'sample test'() {
        expect:
        App.main("123")
    }
}
