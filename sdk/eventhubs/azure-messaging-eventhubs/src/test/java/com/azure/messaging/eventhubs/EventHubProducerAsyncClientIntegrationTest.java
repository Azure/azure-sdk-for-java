// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests for asynchronous {@link EventHubProducerAsyncClient}.
 */
@Tag(TestUtils.INTEGRATION)
class EventHubProducerAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "2";

    private EventHubProducerAsyncClient producer;

    EventHubProducerAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubProducerAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        producer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(RETRY_OPTIONS)
            .buildAsyncProducerClient();
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
        StepVerifier.create(producer.send(events, sendOptions))
            .verifyComplete();
    }

    /**
     * Verifies that we can create an {@link EventHubProducerAsyncClient} that does not care about partitions and lets
     * the service distribute the events.
     */
    @Test
    void sendMessage() {
        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        StepVerifier.create(producer.send(events))
            .verifyComplete();
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

        final Mono<EventDataBatch> createBatch = producer.createBatch().map(batch -> {
            events.forEach(event -> Assertions.assertTrue(batch.tryAdd(event)));

            return batch;
        });

        // Act & Assert
        StepVerifier.create(createBatch.flatMap(batch -> producer.send(batch)))
            .verifyComplete();
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

        final CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("my-partition-key");
        final Mono<EventDataBatch> createBatch = producer.createBatch(options)
            .map(batch -> {
                Assertions.assertEquals(options.getPartitionKey(), batch.getPartitionKey());

                events.forEach(event -> Assertions.assertTrue(batch.tryAdd(event)));

                return batch;
            });

        // Act & Assert
        StepVerifier.create(createBatch.flatMap(batch -> producer.send(batch)))
            .verifyComplete();
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
        final Mono<Void> onComplete = Mono.when(producer.send(events),
            producer.send(Flux.just(events.get(0))),
            producer.send(Flux.fromIterable(events), new SendOptions().setPartitionId("3")),
            producer.send(Flux.fromIterable(events), new SendOptions().setPartitionId("4")),
            producer.send(Flux.fromIterable(events), new SendOptions().setPartitionKey("sandwiches")));

        // Assert
        StepVerifier.create(onComplete)
            .verifyComplete();
    }

    @Test
    void sendAllPartitions() {
        final List<String> partitionIds = producer.getPartitionIds().collectList().block(TIMEOUT);

        Assertions.assertNotNull(partitionIds);

        for (String partitionId : partitionIds) {
            final EventDataBatch batch =
                producer.createBatch(new CreateBatchOptions().setPartitionId(partitionId)).block(TIMEOUT);
            Assertions.assertNotNull(batch);

            Assertions.assertTrue(batch.tryAdd(TestUtils.getEvent("event", "test guid",
                Integer.parseInt(partitionId))));

            // Act & Assert
            StepVerifier.create(producer.send(batch)).expectComplete().verify(TIMEOUT);
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
        final EventHubProducerAsyncClient client = createBuilder(true)
            .buildAsyncProducerClient();

        // Act & Assert
        try {
            StepVerifier.create(client.getEventHubProperties())
                .expectNextMatches(properties -> properties != null && getEventHubName().equals(properties.getName()))
                .expectComplete()
                .verify(TIMEOUT);

            StepVerifier.create(client.send(event, options))
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(client);
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_EVENTHUBS_CONNECTION_STRING_WITH_SAS", matches =
        ".*ShadAccessSignature .*")
    void sendWithSasConnectionString() {
        final EventData event = new EventData("body");
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
        EventHubProducerAsyncClient eventHubAsyncClient = new EventHubClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retry(RETRY_OPTIONS)
            .transportType(AmqpTransportType.AMQP)
            .connectionString(getConnectionString(true))
            .buildAsyncProducerClient();

        try {
            StepVerifier.create(eventHubAsyncClient.getEventHubProperties())
                .assertNext(properties -> {
                    Assertions.assertEquals(getEventHubName(), properties.getName());
                    Assertions.assertEquals(NUMBER_OF_PARTITIONS, properties.getPartitionIds().stream().count());
                })
                .expectComplete()
                .verify(TIMEOUT);

            StepVerifier.create(eventHubAsyncClient.send(event, options))
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(eventHubAsyncClient);
        }

    }
}
