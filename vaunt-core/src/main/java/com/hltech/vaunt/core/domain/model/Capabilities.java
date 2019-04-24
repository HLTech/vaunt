package com.hltech.vaunt.core.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class Capabilities {
    private final List<Contract> contracts;
}
