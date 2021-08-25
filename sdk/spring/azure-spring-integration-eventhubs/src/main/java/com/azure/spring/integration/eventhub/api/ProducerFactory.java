package com.azure.spring.integration.eventhub.api;

import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;

public interface ProducerFactory {

    EventHubProducerAsyncClient getOrCreateProducerClient(String eventHubName);
}
