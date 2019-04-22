package com.hltech.vaunt.validator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidationError {
    MISSING_ENDPOINT("Missing endpoint required by consumer"),
    WRONG_SCHEMA("Wrong schema of the message");

    private final String description;
}
