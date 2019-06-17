package com.hltech.vaunt.generator.domain.representation.message.poliannotated;

import com.hltech.vaunt.generator.domain.representation.annotation.Provider;
import com.hltech.vaunt.core.domain.model.DestinationType;
import lombok.Data;

@Provider(destinationType = DestinationType.QUEUE, destinationName = "destination")
@Provider(destinationType = DestinationType.QUEUE, destinationName = "destination2")
@Data
public class SampleProviderMessage {
    private final long value;
}
