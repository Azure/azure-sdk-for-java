// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests scenarios on {@link EventHubAsyncClient}.
 */
@RunWith(Parameterized.class)
public class EventHubClientIntegrationTest extends ApiTestBase {
    private static final int NUMBER_OF_EVENTS = 5;

    @Parameterized.Parameters(name = "{index}: transportType={0}")
    public static Iterable<Object> getTransportTypes() {
        return Arrays.asList(TransportType.AMQP, TransportType.AMQP_WEB_SOCKETS);
    }

    private static final String PARTITION_ID = "1";
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static final AtomicReference<Instant> MESSAGES_PUSHED_INSTANT = new AtomicReference<>();
    private static final String MESSAGE_TRACKING_VALUE = UUID.randomUUID().toString();

    private EventHubAsyncClient client;

    @Rule
    public TestName testName = new TestName();

    public EventHubClientIntegrationTest(TransportType transportType) {
        super(new ClientLogger(EventHubClientIntegrationTest.class));

        setTransportType(transportType);
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        skipIfNotRecordMode();

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        final ConnectionOptions connectionOptions = getConnectionOptions();

        client = new EventHubAsyncClient(connectionOptions, getReactorProvider(), handlerProvider);

        setupEventTestData(client);
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    @Test(expected = NullPointerException.class)
    public void nullConstructor() throws NullPointerException {
        new EventHubAsyncClient(null, null, null);
    }

    /**
     * Verifies that we can receive messages, and that the receiver continues to fetch messages when the prefetch queue
     * is exhausted.
     */
    @Test
    public void receiveMessage() {
        // Arrange
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .prefetchCount(2);
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.fromEnqueuedTime(MESSAGES_PUSHED_INSTANT.get()), options);

        // Act & Assert
        StepVerifier.create(consumer.receive().filter(x -> isMatchingEvent(x, MESSAGE_TRACKING_VALUE)).take(NUMBER_OF_EVENTS))
            .expectNextCount(NUMBER_OF_EVENTS)
            .verifyComplete();
    }

    /**
     * Verifies that we can have multiple consumers listening to the same partition + consumer group at the same time.
     */
    @Ignore("Investigate. Only 2 of the 4 consumers get the events. The other two consumers do not.")
    @Test
    public void parallelEventHubClients() throws InterruptedException {
        skipIfNotRecordMode();

        // Arrange
        final int numberOfClients = 4;
        final int numberOfEvents = 10;
        final String messageTrackingId = "message-tracking-id";
        final String messageTrackingValue = UUID.randomUUID().toString();
        final Flux<EventData> events = Flux.range(0, numberOfEvents).map(number -> {
            final EventData eventData = new EventData("testString".getBytes(UTF_8));
            eventData.addProperty(messageTrackingId, messageTrackingValue);
            return eventData;
        });

        final CountDownLatch countDownLatch = new CountDownLatch(numberOfClients);
        final EventHubAsyncClient[] clients = new EventHubAsyncClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = new EventHubAsyncClient(getConnectionOptions(), getReactorProvider(), new ReactorHandlerProvider(getReactorProvider()));
        }

        final EventHubProducer producer = clients[0].createProducer(new EventHubProducerOptions().partitionId(PARTITION_ID));
        final List<EventHubConsumer> consumers = new ArrayList<>();
        final Disposable.Composite subscriptions = Disposables.composite();

        try {
            for (final EventHubAsyncClient hubClient : clients) {
                final EventHubConsumer consumer = hubClient.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());
                consumers.add(consumer);

                final Disposable subscription = consumer.receive().filter(event -> {
                    return event.properties() != null
                        && event.properties().containsKey(messageTrackingId)
                        && messageTrackingValue.equals(event.properties().get(messageTrackingId));
                }).take(numberOfEvents).subscribe(event -> {
                    logger.info("Event[{}] matched.", event.sequenceNumber());
                }, error -> Assert.fail("An error should not have occurred:" + error.toString()), () -> {
                        long count = countDownLatch.getCount();
                        logger.info("Finished consuming events. Counting down: {}", count);
                        countDownLatch.countDown();
                    });

                subscriptions.add(subscription);
            }

            // Act
            producer.send(events).block(TIMEOUT);

            // Assert
            // Wait for all the events we sent to be received by each of the consumers.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Assert.assertEquals(0, countDownLatch.getCount());

            logger.info("Completed successfully.");
        } finally {
            logger.info("Disposing of subscriptions, consumers and clients.");
            subscriptions.dispose();

            dispose(producer);
            dispose(consumers.toArray(new EventHubConsumer[0]));
            dispose(clients);
        }
    }

    /**
     * When we run this test, we check if there have been events already pushed to the partition, if not, we push some
     * events there.
     */
    private void setupEventTestData(EventHubAsyncClient client) {
        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
            return;
        }

        logger.info("Pushing events to partition. Message tracking value: {}", MESSAGE_TRACKING_VALUE);

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final EventHubProducer producer = client.createProducer(producerOptions);
        final Flux<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, MESSAGE_TRACKING_VALUE);

        try {
            MESSAGES_PUSHED_INSTANT.set(Instant.now());
            producer.send(events).block(TIMEOUT);
        } finally {
            dispose(producer);
        }
    }
}
