package com.azure.spring.integration.eventhub.api;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;

import java.util.Optional;

public interface ProcessorConsumerFactory {

    EventProcessorClient createEventProcessorClient(String eventHubName, String consumerGroup,
                                                    EventHubProcessor eventHubProcessor);

    Optional<EventProcessorClient> getEventProcessorClient(String eventHubName, String consumerGroup);

    EventProcessorClient removeEventProcessorClient(String eventHubName, String consumerGroup);
}
