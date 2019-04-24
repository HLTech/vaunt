package com.hltech.vaunt.generator.domain.representation.message;

import com.hltech.vaunt.generator.domain.representation.annotation.Consumer;
import com.hltech.vaunt.core.domain.model.DestinationType;
import lombok.Data;

@Consumer(providerName = "provider", destinationType = DestinationType.QUEUE, destinationName = "destination")
@Data
public class SampleConsumerMessage {
    private final String value;
}
