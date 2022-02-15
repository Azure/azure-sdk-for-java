// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.factory;

import com.azure.messaging.eventhubs.*;
import com.azure.spring.integration.core.api.BatchConsumerConfig;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class DefaultEventHubClientFactoryTest {
    @Mock
    EventHubConsumerAsyncClient eventHubConsumerClient;

    @Mock
    EventHubProducerAsyncClient eventHubProducerClient;

    @Mock
    BlobContainerAsyncClient blobContainerClient;

    @Mock
    EventProcessorClient eventProcessorClient;

    @Mock
    EventHubProcessor eventHubProcessor;

    @Mock
    EventHubConnectionStringProvider connectionStringProvider;

    BatchConsumerConfig batchConsumerConfig = BatchConsumerConfig.builder().batchSize(10).build();

    private EventHubClientFactory clientFactory;
    private final String eventHubName = "eventHub";
    private final String eventHubNameWithBatch = "eventHubBatch";
    private final String consumerGroup = "group";
    private final String connectionString = "conStr";
    private final String container = "container";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(blobContainerClient.exists()).thenReturn(Mono.just(true));

        this.clientFactory = new MockedDefaultEventHubClientFactory(connectionString, connectionString, container);
    }

    class MockedDefaultEventHubClientFactory extends DefaultEventHubClientFactory {

        MockedDefaultEventHubClientFactory(String eventHubConnectionString, String checkpointConnectionString,
            String checkpointStorageContainer) {
            super(eventHubConnectionString, checkpointConnectionString, checkpointStorageContainer);
        }

        Map<Integer, Integer> createEventProcessorClientInternalTracker = new HashMap<>();

        @Override
        EventProcessorClient createEventProcessorClientInternal(String eventHubName, String consumerGroup,
            EventHubProcessor eventHubProcessor, BatchConsumerConfig batchConsumerConfig) {
            createEventProcessorClientInternalTracker.compute(Objects.hash(eventHubName, consumerGroup,
                eventHubProcessor, batchConsumerConfig), (key, value) -> (value == null) ? 1 : value + 1);

            return eventProcessorClient;
        }

        @Override
        EventHubConsumerAsyncClient createEventHubClient(String eventHubName, String consumerGroup) {
            return eventHubConsumerClient;
        }

        @Override
        EventHubProducerAsyncClient createProducerClient(String eventHubName) {
            return eventHubProducerClient;
        }

        @Override
        BlobContainerAsyncClient createBlobClient(String containerName) {
            return blobContainerClient;
        }
    }

    @Test
    public void testGetEventHubConsumerClient() {
        EventHubConsumerAsyncClient client = clientFactory.getOrCreateConsumerClient(eventHubName, consumerGroup);
        assertNotNull(client);
        EventHubConsumerAsyncClient another = clientFactory.getOrCreateConsumerClient(eventHubName, consumerGroup);
        assertEquals(client, another);
    }

    @Test
    public void testGetEventHubProducerClient() {
        EventHubProducerAsyncClient sender = clientFactory.getOrCreateProducerClient(eventHubName);
        assertNotNull(sender);
        EventHubProducerAsyncClient another = clientFactory.getOrCreateProducerClient(eventHubName);
        assertEquals(sender, another);
    }

    @Test
    public void testGetEventProcessorClient() {
        clientFactory.createEventProcessorClient(eventHubNameWithBatch, consumerGroup, eventHubProcessor,
            batchConsumerConfig);
        Optional<EventProcessorClient> optionalEph = clientFactory.getEventProcessorClient(eventHubNameWithBatch, consumerGroup);

        assertTrue(optionalEph.isPresent());

        clientFactory.createEventProcessorClient(eventHubName, consumerGroup, eventHubProcessor, null);
        optionalEph = clientFactory.getEventProcessorClient(eventHubName, consumerGroup);

        assertTrue(optionalEph.isPresent());
    }

    @Test
    public void testGetNullEventProcessorClient() {
        Optional<EventProcessorClient> optionalEph = clientFactory.getEventProcessorClient(eventHubName, consumerGroup);
        assertFalse(optionalEph.isPresent());
    }

    @Test
    public void testRemoveEventProcessorClient() {
        EventProcessorClient client = clientFactory.createEventProcessorClient(eventHubName, consumerGroup,
            eventHubProcessor, batchConsumerConfig);
        EventProcessorClient another = clientFactory.removeEventProcessorClient(eventHubName, consumerGroup);

        assertSame(client, another);

        client = clientFactory.createEventProcessorClient(eventHubNameWithBatch, consumerGroup,
            eventHubProcessor, batchConsumerConfig);
        another = clientFactory.removeEventProcessorClient(eventHubNameWithBatch, consumerGroup);

        assertSame(client, another);
    }

    @Test
    public void testRemoveAbsentEventProcessorClient() {
        EventProcessorClient client = clientFactory.removeEventProcessorClient(eventHubName, consumerGroup);
        assertNull(client);
    }

    @Test
    public void testGetOrCreateEventProcessorClient() {
        EventProcessorClient client = clientFactory.createEventProcessorClient(eventHubNameWithBatch, consumerGroup,
            eventHubProcessor, batchConsumerConfig);
        assertNotNull(client);
        clientFactory.createEventProcessorClient(eventHubNameWithBatch, consumerGroup, eventHubProcessor, batchConsumerConfig);

        int hashCode = Objects.hash(eventHubNameWithBatch, consumerGroup, eventHubProcessor, batchConsumerConfig);
        MockedDefaultEventHubClientFactory mockedClientFactory = (MockedDefaultEventHubClientFactory) clientFactory;
        assertEquals(1, mockedClientFactory.createEventProcessorClientInternalTracker.get(hashCode));

        client = clientFactory.createEventProcessorClient(eventHubName, consumerGroup,
            eventHubProcessor, batchConsumerConfig);
        assertNotNull(client);
        clientFactory.createEventProcessorClient(eventHubName, consumerGroup, eventHubProcessor, batchConsumerConfig);

        hashCode = Objects.hash(eventHubName, consumerGroup, eventHubProcessor, batchConsumerConfig);
        assertEquals(1, mockedClientFactory.createEventProcessorClientInternalTracker.get(hashCode));
    }

    @Test
    public void testRecreateEventProcessorClient() {
        EventProcessorClient client = clientFactory.createEventProcessorClient(eventHubNameWithBatch, consumerGroup,
            eventHubProcessor, batchConsumerConfig);
        assertNotNull(client);
        clientFactory.removeEventProcessorClient(eventHubNameWithBatch, consumerGroup);
        clientFactory.createEventProcessorClient(eventHubNameWithBatch, consumerGroup, eventHubProcessor, batchConsumerConfig);

        int hashCode = Objects.hash(eventHubNameWithBatch, consumerGroup, eventHubProcessor, batchConsumerConfig);
        MockedDefaultEventHubClientFactory mockedClientFactory = (MockedDefaultEventHubClientFactory) clientFactory;
        assertEquals(2, mockedClientFactory.createEventProcessorClientInternalTracker.get(hashCode));

        client = clientFactory.createEventProcessorClient(eventHubName, consumerGroup,
            eventHubProcessor, batchConsumerConfig);
        assertNotNull(client);
        clientFactory.removeEventProcessorClient(eventHubName, consumerGroup);
        clientFactory.createEventProcessorClient(eventHubName, consumerGroup, eventHubProcessor, batchConsumerConfig);

        hashCode = Objects.hash(eventHubName, consumerGroup, eventHubProcessor, batchConsumerConfig);
        assertEquals(2, mockedClientFactory.createEventProcessorClientInternalTracker.get(hashCode));

    }
}
