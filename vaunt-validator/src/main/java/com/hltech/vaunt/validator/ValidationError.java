package com.hltech.vaunt.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidationError {
    MISSING_ENDPOINT("Missing endpoint required by consumer"),
    MISSING_MESSAGE_WITH_NAME("Missing message name required by consumer"),
    DUPLICATE_MATCH("More than one message with the same name for given endpoint"),
    WRONG_SCHEMA("Wrong schema of the message");

    private final String description;
}
