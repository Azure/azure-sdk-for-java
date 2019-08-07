// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
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

public class EventHubProducerIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "1";

    private EventHubAsyncClient client;

    public EventHubProducerIntegrationTest() {
        super(new ClientLogger(EventHubProducerIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        final ConnectionStringProperties properties = new ConnectionStringProperties(getConnectionString());
        final ConnectionOptions connectionOptions = new ConnectionOptions(properties.endpoint().getHost(),
            properties.eventHubName(), getTokenCredential(), getAuthorizationType(), TransportType.AMQP, RETRY_OPTIONS,
            ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.parallel());

        client = new EventHubAsyncClient(connectionOptions, getReactorProvider(), handlerProvider);
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
        skipIfNotRecordMode();

        // Arrange
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer(producerOptions)) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }

    /**
     * Verifies that we can create an {@link EventHubProducer} that does not care about partitions and lets the service
     * distribute the events.
     */
    @Test
    public void sendMessage() throws IOException {
        skipIfNotRecordMode();

        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer()) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }

    /**
     * Verifies we can create an {@link EventDataBatch} and send it using our EventHubProducer.
     */
    @Test
    public void sendBatch() throws IOException {
        skipIfNotRecordMode();

        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        try (EventHubProducer producer = client.createProducer()) {
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
        skipIfNotRecordMode();

        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        try (EventHubProducer producer = client.createProducer()) {
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
