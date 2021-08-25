package com.azure.spring.integration.eventhub.api;

import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;

public interface ConsumerFactory {

    EventHubConsumerAsyncClient getOrCreateConsumerClient(String eventHubName, String consumerGroup);

}
