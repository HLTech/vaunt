package com.hltech.vaunt.generator

import spock.lang.Specification

class AppSpec extends Specification {

    def 'sample test'() {
        expect:
        App.main("123")
    }

}
