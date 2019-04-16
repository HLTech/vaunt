package com.hltech.vaunt.generator.domain.representation.model;

import lombok.Data;

import java.util.List;

@Data
public class Capabilities {
    private final List<Contract> contracts;
}
