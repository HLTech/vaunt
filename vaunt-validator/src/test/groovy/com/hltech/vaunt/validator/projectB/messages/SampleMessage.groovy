package com.hltech.vaunt.validator.projectB.messages

class SampleMessage {
    String value
    int amount
    InnerMessage innerMessage
}

class SameMessageAsSample {
    String value
    int amount
    InnerMessage innerMessage
}

class InnerMessage {
    String value2
    BigDecimal amount
}

class AnotherSampleMessage {
    String value
    int amount
}

class EnumMessage {
    InnerEnum en

    enum InnerEnum {
        A,
        B,
        C,
        D
    }
}

class EnumStringMessage {
    String en
}

class SmallMessage {
    String ab
    int defg
    long abc
}

class RequiredMessage {
    String ab
}
