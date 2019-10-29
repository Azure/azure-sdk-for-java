// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventHubProducerClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "1";

    private final Duration tryTimeout = Duration.ofSeconds(30);
    private EventHubProducerAsyncClient asyncProducer;

    public EventHubProducerClientIntegrationTest() {
        super(new ClientLogger(EventHubProducerClientIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        asyncProducer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .scheduler(Schedulers.parallel())
            .buildAsyncProducer();
    }

    @Override
    protected void afterTest() {
        dispose(asyncProducer);
    }

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Test
    public void sendMessageToPartition() {
        // Arrange
        final SendOptions sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
            producer.send(events, sendOptions);
        }
    }

    /**
     * Verifies that we can create an {@link EventHubProducerClient} that does not care about partitions and lets the
     * service distribute the events.
     */
    @Test
    public void sendMessage() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
            producer.send(events);
        }
    }

    /**
     * Verifies we can create an {@link EventDataBatch} and send it using our EventHubProducer.
     */
    @Test
    public void sendBatch() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
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
    public void sendBatchWithPartitionKey() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
            final BatchOptions options = new BatchOptions().setPartitionKey("my-partition-key");
            final EventDataBatch batch = producer.createBatch(options);

            events.forEach(event -> {
                Assert.assertTrue(batch.tryAdd(event));
            });

            producer.send(batch);
        }
    }
}
