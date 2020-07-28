/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.api;


import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.microsoft.azure.spring.integration.eventhub.impl.EventHubProcessor;

import java.util.Optional;

/**
 * @author Warren Zhu
 * @author Domenico Sibilio
 * @author Xiaolu Dai
 */
public interface EventHubClientFactory {

    EventHubConsumerAsyncClient getOrCreateConsumerClient(String eventHubName, String consumerGroup);

    EventHubProducerAsyncClient getOrCreateProducerClient(String eventHubName);

    EventProcessorClient createEventProcessorClient(String eventHubName, String consumerGroup,
                                                    EventHubProcessor eventHubProcessor);

    Optional<EventProcessorClient> getEventProcessorClient(String eventHubName, String consumerGroup);

    EventProcessorClient removeEventProcessorClient(String eventHubName, String consumerGroup);

}
