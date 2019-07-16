package com.hltech.vaunt.validator.projectA.messages

class SampleMessage {
    String value
    int amount
    InnerMessage innerMessage
}

class InnerMessage {
    String value2
    BigDecimal amount
}

class EnumMessage {

    InnerEnum en

    enum InnerEnum {
        A,
        B,
        C
    }
}
