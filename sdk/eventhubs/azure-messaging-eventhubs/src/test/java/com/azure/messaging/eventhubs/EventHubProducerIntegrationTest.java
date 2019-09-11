// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventHubProducerIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";
    private EventHubClient client;

    public EventHubProducerIntegrationTest() {
        super(new ClientLogger(EventHubProducerIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .scheduler(Schedulers.parallel())
            .buildClient();
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Test
    public void sendMessageToPartition() throws IOException {
        // Arrange
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer(producerOptions)) {
            producer.send(events);
        }
    }

    /**
     * Verifies that we can create an {@link EventHubProducer} that does not care about partitions and lets the service
     * distribute the events.
     */
    @Test
    public void sendMessage() throws IOException {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer()) {
            producer.send(events);
        }
    }

    /**
     * Verifies we can create an {@link EventDataBatch} and send it using our EventHubProducer.
     */
    @Test
    public void sendBatch() throws IOException {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer()) {
            EventDataBatch batch = producer.createBatch();
            events.forEach(event -> {
                Assert.assertTrue(batch.tryAdd(event));
            });

            producer.send(batch);
        }
    }

    /**
     * Verifies we can create an {@link EventDataBatch} with a partition key and send it using our EventHubProducer.
     */
    @Test
    public void sendBatchWithPartitionKey() throws IOException {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer()) {
            final BatchOptions options = new BatchOptions().partitionKey("my-partition-key");
            final EventDataBatch batch = producer.createBatch(options);

            events.forEach(event -> {
                Assert.assertTrue(batch.tryAdd(event));
            });

            producer.send(batch);
        }
    }
}
