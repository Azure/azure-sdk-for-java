// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.util.IterableStream;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        // Act

        // Act & Assert
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
            final IterableStream<String> response = producer.getPartitionIds();

            Assert.assertNotNull(response);

            final List<String> partitionIds = response.stream().collect(Collectors.toList());
            Assert.assertTrue(partitionIds.size() > 1);
        }
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getMetadata() {
        // Arrange
        final ConnectionStringProperties connectionProperties = getConnectionStringProperties();

        // Act
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
            final EventHubProperties properties = producer.getProperties();

            // Assert
            Assert.assertNotNull(properties);
            Assert.assertEquals(connectionProperties.getEntityPath(), properties.getName());
            Assert.assertTrue(properties.getCreatedAt().isBefore(Instant.now()));

            Assert.assertNotNull(properties.getPartitionIds());
            Assert.assertTrue(properties.getPartitionIds().length > 1);
        }
    }

    /**
     * Verifies we can get partition ids of an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        // Arrange
        final ConnectionStringProperties connectionProperties = getConnectionStringProperties();

        // Act

        // Act
        try (EventHubProducerClient producer = new EventHubProducerClient(asyncProducer, tryTimeout)) {
            final EventHubProperties properties = producer.getProperties();
            final String partitionId = properties.getPartitionIds()[0];
            final PartitionProperties partitionProperties = producer.getPartitionProperties(partitionId);

            // Assert
            Assert.assertNotNull(partitionProperties);

            Assert.assertEquals(connectionProperties.getEntityPath(), partitionProperties.getEventHubName());
            Assert.assertEquals(partitionId, partitionProperties.getId());
        }
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
