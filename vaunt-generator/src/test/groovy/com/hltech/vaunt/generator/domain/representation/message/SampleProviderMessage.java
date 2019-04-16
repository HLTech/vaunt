package com.hltech.vaunt.generator.domain.representation.message;

import com.hltech.vaunt.generator.domain.representation.annotation.Provider;
import com.hltech.vaunt.generator.domain.representation.model.DestinationType;
import lombok.Data;

@Provider(destinationType = DestinationType.QUEUE, destinationName = "destination")
@Data
public class SampleProviderMessage {
    private final long value;
}
