package com.hltech.vaunt.validator

class SampleMessage {
    String value
    int amount
    InnerMessage innerMessage
}

class InnerMessage {
    String value2
    BigDecimal amount
}