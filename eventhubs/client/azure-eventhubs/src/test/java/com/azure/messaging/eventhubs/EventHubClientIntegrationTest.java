// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.azure.messaging.eventhubs.EventHubClient.DEFAULT_CONSUMER_GROUP_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests scenarios on {@link EventHubClient}.
 */
public class EventHubClientIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";

    private EventHubClient client;
    private ReactorHandlerProvider handlerProvider;

    @Rule
    public TestName testName = new TestName();

    public EventHubClientIntegrationTest() {
        super(new ClientLogger(EventHubClientIntegrationTest.class));
    }

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        client = new EventHubClient(getConnectionOptions(), getReactorProvider(), handlerProvider);
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    @Ignore("client not closed properly")
    @Test(expected = NullPointerException.class)
    public void nullConstructor() throws NullPointerException {
        client = new EventHubClient(null, null, null);
    }

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Ignore("java.util.concurrent.CancellationException: Disposed")
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
    @Ignore("java.util.concurrent.CancellationException: Disposed")
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
     * Verifies that we can receive messages, and that the receiver continues to fetch messages when the prefetch queue
     * is exhausted.
     */
    @Test
    public void receiveMessage() {
        skipIfNotRecordMode();

        // Arrange
        final int numberOfEvents = 10;
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .prefetchCount(2);
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID,
            EventPosition.earliest(), options);

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();
    }

    /**
     * Verifies that we can have multiple consumers listening to the same partition + consumer group at the same time.
     */
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
        final EventHubClient[] clients = new EventHubClient[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            clients[i] = new EventHubClient(getConnectionOptions(), getReactorProvider(), new ReactorHandlerProvider(getReactorProvider()));
        }

        final EventHubProducer producer = client.createProducer(new EventHubProducerOptions().partitionId(PARTITION_ID));
        final List<EventHubConsumer> consumers = new ArrayList<>();
        final Disposable.Composite subscriptions = Disposables.composite();

        try {
            for (EventHubClient client : clients) {
                final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.latest());
                consumers.add(consumer);

                final Disposable subscription = consumer.receive().filter(event -> {
                    return event.properties() != null
                        && event.properties().containsKey(messageTrackingId)
                        && messageTrackingValue.equals(event.properties().get(messageTrackingId));
                }).take(numberOfEvents).subscribe(event -> {
                    logger.asInfo().log("Event[{}] matched.", event.sequenceNumber());
                }, error -> Assert.fail("An error should not have occurred:" + error.toString()), () -> {
                    logger.asInfo().log("Finished consuming events. Counting down: %s", countDownLatch.getCount());
                    countDownLatch.countDown();
                });

                subscriptions.add(subscription);
            }

            // Act
            producer.send(events).block(TIMEOUT);

            // Assert
            // Wait for all the events we sent to be received by each of the consumers.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } finally {
            logger.asInfo().log("Disposing of subscriptions, consumers and clients.");
            subscriptions.dispose();

            dispose(producer);
            dispose(consumers.toArray(new EventHubConsumer[0]));
            dispose(clients);
        }
    }
}
