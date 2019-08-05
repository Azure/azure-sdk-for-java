// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ErrorContextProvider;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;

public class EventDataBatchIntegrationTest extends ApiTestBase {
    private static final String PARTITION_KEY = "PartitionIDCopyFromProducerOption";

    private EventHubAsyncClient client;
    private EventHubProducer producer;

    @Mock
    private ErrorContextProvider contextProvider;

    @Rule
    public TestName testName = new TestName();

    public EventDataBatchIntegrationTest() {
        super(new ClientLogger(EventDataBatchIntegrationTest.class));
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        MockitoAnnotations.initMocks(this);

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());

        client = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
        producer = client.createProducer();
    }

    @Override
    protected void afterTest() {
        dispose(producer, client);
    }

    /**
     * Test for sending full batch without partition key
     */
    @Test
    public void sendSmallEventsFullBatch() {
        skipIfNotRecordMode();

        // Arrange
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, null, contextProvider);
        int count = 0;
        while (batch.tryAdd(createData())) {
            // We only print every 100th item or it'll be really spammy.
            if (count % 100 == 0) {
                logger.verbose("Batch size: {}", batch.getSize());
            }

            count++;
        }

        // Act & Assert
        StepVerifier.create(producer.send(batch.getEvents()))
            .verifyComplete();
    }

    /**
     * Test for sending a message batch that is {@link EventHubProducer#MAX_MESSAGE_LENGTH_BYTES} with partition key.
     */
    @Test
    public void sendSmallEventsFullBatchPartitionKey() {
        skipIfNotRecordMode();

        // Arrange
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY, contextProvider);
        int count = 0;
        while (batch.tryAdd(createData())) {
            // We only print every 100th item or it'll be really spammy.
            if (count % 100 == 0) {
                logger.verbose("Batch size: {}", batch.getSize());
            }
            count++;
        }

        // Act & Assert
        StepVerifier.create(producer.send(batch.getEvents()))
            .verifyComplete();
    }

    /**
     * Verifies that when we send 10 messages with the same partition key and some application properties, the received
     * EventData also contains the {@link EventData#partitionKey()} property set.
     */
    @Test
    public void sendBatchPartitionKeyValidate() throws InterruptedException {
        skipIfNotRecordMode();

        // Arrange
        final String messageValue = UUID.randomUUID().toString();

        final SendOptions sendOptions = new SendOptions().partitionKey(PARTITION_KEY);
        final EventDataBatch batch = new EventDataBatch(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES, PARTITION_KEY, contextProvider);
        int count = 0;
        while (count < 10) {
            final EventData data = createData();
            data.properties().put(MESSAGE_TRACKING_ID, messageValue);

            if (!batch.tryAdd(data)) {
                break;
            }
            count++;
        }

        final CountDownLatch countDownLatch = new CountDownLatch(batch.getSize());

        Flux<EventHubConsumer> consumers;
        Disposable.Composite subscriptions = Disposables.composite();
        try {

            // Creating consumers on all the partitions and subscribing to the receive event.
            consumers = client.getPartitionIds()
                .map(id -> client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME, id, EventPosition.latest()));

            final List<Disposable> consumerSubscriptions = consumers.map(consumer -> {
                return consumer.receive().subscribe(event -> {
                    if (event.partitionKey() == null || !PARTITION_KEY.equals(event.partitionKey())) {
                        return;
                    }

                    if (isMatchingEvent(event, messageValue)) {
                        logger.info("Event[{}] matched. Countdown: {}", event.sequenceNumber(), countDownLatch.getCount());
                        countDownLatch.countDown();
                    } else {
                        logger.warning(String.format("Event[%s] matched partition key, but not GUID. Expected: %s. Actual: %s",
                            event.sequenceNumber(), messageValue, event.properties().get(MESSAGE_TRACKING_ID)));
                    }
                }, error -> {
                        Assert.fail("An error should not have occurred:" + error.toString());
                    }, () -> {
                        logger.info("Disposing of consumer now that the receive is complete.");
                        dispose(consumer);
                    });
            }).collectList().block(TIMEOUT);

            Assert.assertNotNull(consumerSubscriptions);
            Assert.assertFalse(consumerSubscriptions.isEmpty());

            subscriptions.addAll(consumerSubscriptions);

            // Act
            producer.send(batch.getEvents(), sendOptions).block(TIMEOUT);

            // Assert
            // Wait for all the events we sent to be received.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } finally {
            logger.info("Disposing of subscriptions.");
            subscriptions.dispose();
        }

        Assert.assertEquals(0, countDownLatch.getCount());
    }

    /**
     * Verify we can send a batch by specifying the {@code maxMessageSize} and partition key.
     */
    @Test
    public void sendEventsFullBatchWithPartitionKey() {
        skipIfNotRecordMode();

        // Arrange
        final int maxMessageSize = 1024;
        final EventDataBatch batch = new EventDataBatch(maxMessageSize, PARTITION_KEY, contextProvider);
        final Random random = new Random();
        final SendOptions sendOptions = new SendOptions();
        int count = 0;

        while (true) {
            final EventData eventData = new EventData("a".getBytes());
            for (int i = 0; i < random.nextInt(20); i++) {
                eventData.properties().put("key" + i, "value");
            }

            if (batch.tryAdd(eventData)) {
                count++;
            } else {
                break;
            }
        }

        // Act & Assert
        Assert.assertEquals(count, batch.getSize());
        StepVerifier.create(producer.send(batch.getEvents(), sendOptions))
            .verifyComplete();
    }

    private static EventData createData() {
        return new EventData("a".getBytes(StandardCharsets.UTF_8));
    }
}
