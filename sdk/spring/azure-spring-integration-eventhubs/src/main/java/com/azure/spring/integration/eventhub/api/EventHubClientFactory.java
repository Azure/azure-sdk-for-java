// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.api;


import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.integration.core.api.BatchConsumerConfig;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author Warren Zhu
 * @author Domenico Sibilio
 * @author Xiaolu Dai
 */
public interface EventHubClientFactory {

    /**
     *
     * @param eventHubName The event hub name.
     * @param consumerGroup The consumer group.
     * @return The EventHubConsumerAsyncClient.
     */
    EventHubConsumerAsyncClient getOrCreateConsumerClient(String eventHubName, String consumerGroup);

    /**
     *
     * @param eventHubName The event hub name.
     * @return The EventHubProducerAsyncClient.
     */
    EventHubProducerAsyncClient getOrCreateProducerClient(String eventHubName);

    /**
     *
     * @param eventHubName The event hub name.
     * @param consumerGroup The consumer group.
     * @param eventHubProcessor The event hub processor.
     * @param batchConsumerConfig The batch consumer config.
     * @return The EventProcessorClient.
     */
    EventProcessorClient createEventProcessorClient(String eventHubName, String consumerGroup,
                                                    EventHubProcessor eventHubProcessor, @Nullable BatchConsumerConfig batchConsumerConfig);

    /**
     *
     * @param eventHubName The event hub name.
     * @param consumerGroup The consumer group.
     * @return The Optional of EventProcessorClient.
     */
    Optional<EventProcessorClient> getEventProcessorClient(String eventHubName, String consumerGroup);

    /**
     *
     * @param eventHubName The event hub name.
     * @param consumerGroup The consumer group.
     * @return The EventProcessorClient.
     */
    EventProcessorClient removeEventProcessorClient(String eventHubName, String consumerGroup);

}
