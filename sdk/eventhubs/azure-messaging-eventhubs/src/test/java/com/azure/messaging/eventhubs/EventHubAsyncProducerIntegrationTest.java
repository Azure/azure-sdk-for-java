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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventHubAsyncProducerIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";

    private EventHubAsyncClient client;

    public EventHubAsyncProducerIntegrationTest() {
        super(new ClientLogger(EventHubAsyncProducerIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .scheduler(Schedulers.parallel())
            .buildAsyncClient();
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
        try (EventHubAsyncProducer producer = client.createProducer(producerOptions)) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }

    /**
     * Verifies that we can create an {@link EventHubAsyncProducer} that does not care about partitions and lets the service
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
        try (EventHubAsyncProducer producer = client.createProducer()) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
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

        try (EventHubAsyncProducer producer = client.createProducer()) {
            final Mono<EventDataBatch> createBatch = producer.createBatch().map(batch -> {
                events.forEach(event -> {
                    Assert.assertTrue(batch.tryAdd(event));
                });

                return batch;
            });

            // Act & Assert
            StepVerifier.create(createBatch.flatMap(batch -> producer.send(batch)))
                .verifyComplete();
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

        try (EventHubAsyncProducer producer = client.createProducer()) {
            final BatchOptions options = new BatchOptions().partitionKey("my-partition-key");
            final Mono<EventDataBatch> createBatch = producer.createBatch(options)
                .map(batch -> {
                    Assert.assertEquals(options.partitionKey(), batch.getPartitionKey());

                    events.forEach(event -> {
                        Assert.assertTrue(batch.tryAdd(event));
                    });

                    return batch;
                });

            // Act & Assert
            StepVerifier.create(createBatch.flatMap(batch -> producer.send(batch)))
                .verifyComplete();
        }
    }
}
