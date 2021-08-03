// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests for synchronous {@link EventHubProducerClient}.
 */
@Tag(TestUtils.INTEGRATION)
class EventHubProducerClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "2";
    private EventHubProducerClient producer;

    EventHubProducerClientIntegrationTest() {
        super(new ClientLogger(EventHubProducerClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildProducerClient();
    }

    @Override
    protected void afterTest() {
        dispose(producer);
    }

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Test
    void sendMessageToPartition() {
        // Arrange
        final SendOptions sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        producer.send(events, sendOptions);
    }

    /**
     * Verifies that we can create an {@link EventHubProducerClient} that does not care about partitions and lets the
     * service distribute the events.
     */
    @Test
    void sendMessage() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        producer.send(events);
    }

    /**
     * Verifies we can create an {@link EventDataBatch} and send it using our EventHubProducer.
     */
    @Test
    void sendBatch() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        EventDataBatch batch = producer.createBatch();
        events.forEach(event -> Assertions.assertTrue(batch.tryAdd(event)));

        producer.send(batch);
    }

    /**
     * Verifies we can create an {@link EventDataBatch} with a partition key and send it using our EventHubProducer.
     */
    @Test
    void sendBatchWithPartitionKey() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        final CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("my-partition-key");
        final EventDataBatch batch = producer.createBatch(options);

        events.forEach(event -> Assertions.assertTrue(batch.tryAdd(event)));

        producer.send(batch);
    }

    /**
     * Verify that we can send to multiple partitions, round-robin, and with a partition key, using the same producer.
     */
    @Test
    void sendEventsWithKeyAndPartition() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act
        producer.send(events);
        producer.send(events.get(0));
        producer.send(events.get(0), new SendOptions().setPartitionId("2"));
        producer.send(events, new SendOptions().setPartitionId("3"));
        producer.send(events, new SendOptions().setPartitionKey("sandwiches"));
    }

    @Test
    void sendAllPartitions() {
        for (String partitionId : producer.getPartitionIds()) {
            final EventDataBatch batch = producer.createBatch(new CreateBatchOptions().setPartitionId(partitionId));
            Assertions.assertNotNull(batch);

            Assertions.assertTrue(batch.tryAdd(TestUtils.getEvent("event", "test guid", Integer.parseInt(partitionId))));

            // Act & Assert
            producer.send(batch);
        }
    }

    /**
     * Sending with credentials.
     */
    @Test
    void sendWithCredentials() {
        // Arrange
        final EventData event = new EventData("body");
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
        final EventHubProducerClient client = createBuilder(true)
            .buildProducerClient();

        // Act & Assert
        try {
            client.send(event, options);
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dispose(client);
        }
    }
}
