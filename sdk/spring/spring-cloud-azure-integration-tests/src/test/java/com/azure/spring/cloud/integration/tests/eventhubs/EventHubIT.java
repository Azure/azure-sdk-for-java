// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("event-hub")
public class EventHubIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubIT.class);
    private final String data = "eventhub test";

    @Autowired
    private EventHubProducerClient producerClient;

    @Autowired
    private EventHubConsumerClient consumerClient;

    @Test
    public void testEventHubOperation() throws InterruptedException {
        LOGGER.info("EventHubIT begin.");
        producerClient.send(Arrays.asList(new EventData(data)));
        producerClient.close();
        IterableStream<PartitionEvent> events = consumerClient.receiveFromPartition("0", 1, EventPosition.earliest());
        for (PartitionEvent event : events) {
            Assertions.assertEquals(data, event.getData().getBodyAsString());
        }
    }

}
