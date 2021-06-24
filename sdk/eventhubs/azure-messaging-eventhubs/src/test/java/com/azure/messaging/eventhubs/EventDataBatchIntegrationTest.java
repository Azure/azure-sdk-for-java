// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_ID;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

@Tag(TestUtils.INTEGRATION)
public class EventDataBatchIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_KEY = "PartitionIDCopyFromProducerOption";

    private final TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
    private EventHubProducerAsyncClient producer;
    private EventHubClientBuilder builder;

    @Mock
    private ErrorContextProvider contextProvider;

    public EventDataBatchIntegrationTest() {
        super(new ClientLogger(EventDataBatchIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        builder = createBuilder()
            .shareConnection()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(EventHubClientBuilder.DEFAULT_PREFETCH_COUNT);
        producer = builder.buildAsyncProducerClient();
    }

    @Override
    protected void afterTest() {
        dispose(producer);
    }

    /**
     * Test for sending full batch without partition key
     */
    @Test
    public void sendSmallEventsFullBatch() {
        // Arrange
        final EventDataBatch batch = new EventDataBatch(ClientConstants.MAX_MESSAGE_LENGTH_BYTES, null, null, contextProvider,
            new TracerProvider(Collections.emptyList()), getFullyQualifiedDomainName(), getEventHubName());
        int count = 0;
        while (batch.tryAdd(createData())) {
            // We only print every 100th item or it'll be really spammy.
            if (count % 100 == 0) {
                logger.verbose("Batch size: {}", batch.getCount());
            }

            count++;
        }

        // Act & Assert
        StepVerifier.create(producer.send(batch.getEvents()))
            .verifyComplete();
    }

    /**
     * Test for sending a message batch that is {@link ClientConstants#MAX_MESSAGE_LENGTH_BYTES} with partition key.
     */
    @Test
    public void sendSmallEventsFullBatchPartitionKey() {
        // Arrange
        final EventDataBatch batch = new EventDataBatch(ClientConstants.MAX_MESSAGE_LENGTH_BYTES, null,
            PARTITION_KEY, contextProvider, tracerProvider, getFullyQualifiedDomainName(), getEventHubName());
        int count = 0;
        while (batch.tryAdd(createData())) {
            // We only print every 100th item or it'll be really spammy.
            if (count % 100 == 0) {
                logger.verbose("Batch size: {}", batch.getCount());
            }
            count++;
        }

        // Act & Assert
        StepVerifier.create(producer.send(batch.getEvents()))
            .verifyComplete();
    }

    /**
     * Verifies that when we send 10 messages with the same partition key and some application properties, the received
     * EventData also contains the {@link EventData#getPartitionKey()} property set.
     */
    @Test
    public void sendBatchPartitionKeyValidate() throws InterruptedException {
        // Arrange
        final String messageValue = UUID.randomUUID().toString();

        final SendOptions sendOptions = new SendOptions().setPartitionKey(PARTITION_KEY);
        final EventDataBatch batch = new EventDataBatch(ClientConstants.MAX_MESSAGE_LENGTH_BYTES, null,
            PARTITION_KEY, contextProvider, tracerProvider, getFullyQualifiedDomainName(), getEventHubName());
        int count = 0;
        while (count < 10) {
            final EventData data = createData();
            data.getProperties().put(MESSAGE_ID, messageValue);

            if (!batch.tryAdd(data)) {
                break;
            }
            count++;
        }

        final CountDownLatch countDownLatch = new CountDownLatch(batch.getCount());
        final List<EventHubConsumerAsyncClient> consumers = new ArrayList<>();
        final Instant now = Instant.now();
        try {
            // Creating consumers on all the partitions and subscribing to the receive event.
            final List<String> partitionIds = producer.getPartitionIds().collectList().block(TIMEOUT);
            Assertions.assertNotNull(partitionIds);

            for (String id : partitionIds) {
                final EventHubConsumerAsyncClient consumer = builder.buildAsyncConsumerClient();

                consumers.add(consumer);
                consumer.receiveFromPartition(id, EventPosition.fromEnqueuedTime(now)).subscribe(partitionEvent -> {
                    EventData event = partitionEvent.getData();
                    if (event.getPartitionKey() == null || !PARTITION_KEY.equals(event.getPartitionKey())) {
                        return;
                    }

                    if (isMatchingEvent(event, messageValue)) {
                        logger.info("Event[{}] matched. Countdown: {}", event.getSequenceNumber(), countDownLatch.getCount());
                        countDownLatch.countDown();
                    } else {
                        logger.warning("Event[{}] matched partition key, but not GUID. Expected: {}. Actual: {}",
                            event.getSequenceNumber(), messageValue, event.getProperties().get(MESSAGE_ID));
                    }
                }, error -> {
                        Assertions.fail("An error should not have occurred:" + error.toString());
                    }, () -> {
                        logger.info("Disposing of consumer now that the receive is complete.");
                        dispose(consumer);
                    });
            }

            // Act
            producer.send(batch.getEvents(), sendOptions).block();

            // Assert
            // Wait for all the events we sent to be received.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } finally {
            logger.info("Disposing of subscriptions.");
            dispose(consumers.toArray(new EventHubConsumerAsyncClient[0]));
        }

        Assertions.assertEquals(0, countDownLatch.getCount());
    }

    /**
     * Verify we can send a batch by specifying the {@code maxMessageSize} and partition key.
     */
    @Test
    public void sendEventsFullBatchWithPartitionKey() {
        // Arrange
        final int maxMessageSize = 1024;
        final EventDataBatch batch = new EventDataBatch(maxMessageSize, null, PARTITION_KEY, contextProvider,
            tracerProvider, getFullyQualifiedDomainName(), getEventHubName());
        final Random random = new Random();
        final SendOptions sendOptions = new SendOptions().setPartitionKey(PARTITION_KEY);
        int count = 0;

        while (true) {
            final EventData eventData = new EventData("a".getBytes());
            for (int i = 0; i < random.nextInt(20); i++) {
                eventData.getProperties().put("key" + i, "value");
            }

            if (batch.tryAdd(eventData)) {
                count++;
            } else {
                break;
            }
        }

        // Act & Assert
        Assertions.assertEquals(count, batch.getCount());
        StepVerifier.create(producer.send(batch.getEvents(), sendOptions))
            .verifyComplete();
    }

    private static EventData createData() {
        return new EventData("a".getBytes(StandardCharsets.UTF_8));
    }
}
