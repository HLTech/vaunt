package com.hltech.vaunt.generator.domain.representation.model;

import lombok.Data;

@Data
public class Service {

    private final String name;
    private final Capabilities capabilities;
    private final Expectations expectations;
}
