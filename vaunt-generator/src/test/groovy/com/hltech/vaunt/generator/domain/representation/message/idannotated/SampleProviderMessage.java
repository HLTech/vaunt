package com.hltech.vaunt.generator.domain.representation.message.idannotated;

import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.generator.domain.representation.annotation.Provider;
import lombok.Data;

@Provider(destinationType = DestinationType.QUEUE, destinationName = "destination", messageId = "testId")
@Data
public class SampleProviderMessage {
    private final long value;
}
