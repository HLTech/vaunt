package com.hltech.vaunt.generator.domain.representation.message;

import com.google.common.collect.Multimap;
import com.hltech.vaunt.generator.domain.representation.annotation.Consumer;
import com.hltech.vaunt.core.domain.model.DestinationType;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Consumer(providerName = "provider", destinationType = DestinationType.QUEUE, destinationName = "destination")
@Data
public class SampleConsumerMessage {
    private final String value;
    private final ZonedDateTime time;
    private final Multimap<String, String> mappedData;
    private final List<String> listedData;
}
