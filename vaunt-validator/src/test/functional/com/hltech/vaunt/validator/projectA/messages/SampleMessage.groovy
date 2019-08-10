package com.hltech.vaunt.validator.projectA.messages

import com.fasterxml.jackson.annotation.JsonProperty

class SampleMessage {
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
    String amount
}

class EnumMessage {
    InnerEnum en

    enum InnerEnum {
        A,
        B,
        C
    }
}

class EnumStringMessage {
    InnerEnum en

    enum InnerEnum {
        A,
        B,
        C
    }
}

class SmallMessage {
    String ab
    int defg
}

class RequiredMessage {
    @JsonProperty(required = true)
    String ab
}
