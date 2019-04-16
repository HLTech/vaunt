package com.hltech.vaunt.generator.domain.representation.model;

import com.google.common.collect.Multimap;
import lombok.Data;

@Data
public class Expectations {
    private final Multimap<String, Contract> providerNameToContracts;
}
