// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = EventHubIT.TestConfig.class)
@ActiveProfiles(profiles = {"event-hub","service-bus-jms"})
public class EventHubIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubIT.class);
    private final String data = "eventhub test";

    @Autowired
    private EventHubProducerClient producerClient;

    @Autowired
    private EventHubConsumerClient consumerClient;

    @Autowired
    private EventProcessorClient processorClient;

    @Autowired
    private BlobCheckpointStore checkpointStore;

    @EnableAutoConfiguration
    static class TestConfig {
        @Bean
        EventHubsRecordMessageListener messageListener() {
            return message -> {};
        }
        @Bean
        EventHubsErrorHandler errorHandler() {
            return errorContext -> {};
        }
    }
    @Test
    public void testEventHubOperation() throws InterruptedException {
        LOGGER.info("EventHubIT begin.");
        producerClient.send(Arrays.asList(new EventData(data)));
        producerClient.close();
        IterableStream<PartitionEvent> events = consumerClient.receiveFromPartition("0", 1, EventPosition.earliest());
        for (PartitionEvent event : events) {
            Assertions.assertEquals(data, event.getData().getBodyAsString());
        }
        processorClient.start();
        Assertions.assertTrue(processorClient.isRunning());
        processorClient.stop();
        Assertions.assertFalse(processorClient.isRunning());
        LOGGER.info("EventHubIT end.");
    }

}
