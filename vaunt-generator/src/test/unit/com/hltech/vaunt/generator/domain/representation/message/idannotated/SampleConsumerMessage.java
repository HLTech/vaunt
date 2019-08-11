package com.hltech.vaunt.generator.domain.representation.message.idannotated;

import com.google.common.collect.Multimap;
import com.hltech.vaunt.core.domain.model.DestinationType;
import com.hltech.vaunt.generator.domain.representation.annotation.Consumer;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Consumer(providerName = "provider",
        destinationType = DestinationType.QUEUE,
        destinationName = "destination",
        messageId = "randomId")
@Data
public class SampleConsumerMessage {
    private final String value;
    private final ZonedDateTime time;
    private final Multimap<String, String> mappedData;
    private final List<String> listedData;
}
