// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.core.amqp.exception.ErrorCondition.RESOURCE_LIMIT_EXCEEDED;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;

/**
 * Integration tests with Azure Event Hubs service. There are other tests that also test {@link
 * EventHubConsumerAsyncClient} in other scenarios.
 *
 * @see SetPrefetchCountTest
 * @see EventPositionIntegrationTest
 */
public class EventHubConsumerAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";
    private final String[] expectedPartitionIds = new String[]{"0", "1"};

    // The maximum number of receivers on a partition + consumer group is 5.
    private static final int MAX_NUMBER_OF_CONSUMERS = 5;
    private static final String MESSAGE_TRACKING_ID = UUID.randomUUID().toString();

    private EventHubAsyncClient client;

    public EventHubConsumerAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubConsumerAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        client = createBuilder()
            .scheduler(Schedulers.single())
            .buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Tests that the same EventHubAsyncClient can create multiple EventHubConsumers listening to different partitions.
     */
    @Test
    public void parallelCreationOfReceivers() {
        // Arrange
        final int numberOfEvents = 10;
        final List<String> partitionIds = client.getPartitionIds().collectList().block(TIMEOUT);
        if (partitionIds == null || partitionIds.isEmpty()) {
            Assertions.fail("Should have partitions");
        }

        final CountDownLatch countDownLatch = new CountDownLatch(partitionIds.size());
        final EventHubConsumerAsyncClient[] consumers = new EventHubConsumerAsyncClient[partitionIds.size()];
        final EventHubProducerAsyncClient[] producers = new EventHubProducerAsyncClient[partitionIds.size()];
        final Disposable.Composite subscriptions = Disposables.composite();
        try {
            for (int i = 0; i < partitionIds.size(); i++) {
                final String partitionId = partitionIds.get(i);
                final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
                    EventPosition.fromEnqueuedTime(Instant.now()));
                consumers[i] = consumer;

                final Disposable subscription = consumer.receive(partitionId).take(numberOfEvents)
                    .subscribe(
                        event -> logger.info("Event[{}] received. partition: {}", event.getEventData().getSequenceNumber(), partitionId),
                        error -> Assertions.fail("An error should not have occurred:" + error.toString()),
                        () -> {
                            logger.info("Disposing of consumer now that the receive is complete.");
                            countDownLatch.countDown();
                        });

                subscriptions.add(subscription);

                producers[i] = client.createProducer();
            }

            // Act
            for (int i = 0; i < partitionIds.size(); i++) {
                final String partitionId = partitionIds.get(i);
                final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
                final EventHubProducerAsyncClient producer = producers[i];

                producer.send(TestUtils.getEvents(numberOfEvents, MESSAGE_TRACKING_ID), sendOptions).block(TIMEOUT);
            }

            // Assert
            // Wait for all the events we sent to be received.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Assertions.assertEquals(0, countDownLatch.getCount());
        } catch (InterruptedException e) {
            Assertions.fail("Countdown latch was interrupted:" + e);
        } finally {
            logger.info("Disposing of subscriptions, consumers, producers.");

            subscriptions.dispose();
            dispose(consumers);
            dispose(producers);
        }
    }

    /**
     * Verify if we don't set {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()}, then it is always
     * null as we are consuming events.
     */
    @Test
    public void lastEnqueuedInformationIsNotUpdated() {
        // Arrange
        final String secondPartitionId = "1";
        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(1)
            .setTrackLastEnqueuedEventProperties(false);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, position, options);

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final int expectedNumber = 5;
        final EventHubProducerAsyncClient producer = client.createProducer();
        final Disposable producerEvents = getEvents(isActive).flatMap(event -> producer.send(event)).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event", error));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(secondPartitionId).take(expectedNumber))
                .assertNext(event -> {
                    Assertions.assertNull(event.getPartitionContext().getLastEnqueuedEventProperties(), "'lastEnqueuedEventProperties' should be null.");
                })
                .expectNextCount(expectedNumber - 1)
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }
    }

    /**
     * Verify that each time we receive an event, the data, {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()},
     * null as we are consuming events.
     */
    @Test
    public void lastEnqueuedInformationIsUpdated() {
        // Arrange
        final String secondPartitionId = "1";
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = client.createProducer();
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(secondPartitionId)))
            .subscribe(
                sent -> {
                },
                error -> logger.error("Error sending event", error),
                () -> logger.info("Event sent."));

        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(1)
            .setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
            EventPosition.latest(), options);
        final AtomicReference<LastEnqueuedEventProperties> lastViewed = new AtomicReference<>(
            new LastEnqueuedEventProperties(null, null, null, null));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(secondPartitionId).take(10))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), true))
                .expectNextCount(5)
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }
    }

    private static void verifyLastRetrieved(AtomicReference<LastEnqueuedEventProperties> atomicReference,
        LastEnqueuedEventProperties current, boolean isFirst) {
        Assertions.assertNotNull(current);
        final LastEnqueuedEventProperties previous = atomicReference.get();

        // Update the atomic reference to the new one now.
        atomicReference.set(current);

        // The first time we step through this, the retrieval time will not be set for the previous event.
        if (isFirst) {
            return;
        }

        Assertions.assertNotNull(previous.getRetrievalTime(), "This is not the first event, should have a retrieval time.");

        final int compared = previous.getRetrievalTime().compareTo(current.getRetrievalTime());
        final int comparedSequenceNumber = previous.getOffset().compareTo(current.getOffset());
        Assertions.assertTrue(compared <= 0, String.format("Expected retrieval time previous '%s' to be before or equal to current '%s'",
            previous.getRetrievalTime(), current.getRetrievalTime()));

        Assertions.assertTrue(comparedSequenceNumber <= 0, String.format("Expected offset previous '%s' to be before or equal to current '%s'",
            previous.getRetrievalTime(), current.getRetrievalTime()));
    }

    /**
     * Verify that if we set the identifier in the consumer, it shows up in the quota error.
     */
    @Disabled("Investigate. The sixth receiver is not causing an exception to be thrown.")
    @Test
    public void consumerIdentifierShowsUpInQuotaErrors() {
        // Arrange
        final String prefix = UUID.randomUUID().toString();
        final Consumer<AmqpException> validateException = error -> {
            Assertions.assertEquals(RESOURCE_LIMIT_EXCEEDED, error.getErrorCondition());

            final String errorMsg = error.getMessage();
            for (int i = 0; i < MAX_NUMBER_OF_CONSUMERS; i++) {
                Assertions.assertTrue(errorMsg.contains(prefix + ":" + i));
            }
        };

        final List<EventHubConsumerAsyncClient> consumers = new ArrayList<>();
        final Disposable.Composite subscriptions = Disposables.composite();
        EventHubConsumerAsyncClient exceededConsumer = null;
        try {
            for (int i = 0; i < MAX_NUMBER_OF_CONSUMERS; i++) {
                final EventHubConsumerOptions options = new EventHubConsumerOptions().setIdentifier(prefix + ":" + i);
                final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, EventPosition.earliest(), options);
                consumers.add(consumer);
                subscriptions.add(consumer.receive(PARTITION_ID).take(TIMEOUT).subscribe(eventData -> {
                    // Received an event. We don't need to log it though.
                }));
            }

            // Act & Verify
            exceededConsumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, EventPosition.earliest());
            StepVerifier.create(exceededConsumer.receive(PARTITION_ID))
                .expectErrorSatisfies(exception -> {
                    Assertions.assertTrue(exception instanceof AmqpException);
                    validateException.accept((AmqpException) exception);
                })
                .verify();
        } catch (AmqpException e) {
            validateException.accept(e);
        } finally {
            subscriptions.dispose();
            dispose(exceededConsumer);
            dispose(consumers.toArray(new EventHubConsumerAsyncClient[0]));
        }
    }

    /**
     * Verifies when a consumer with the same owner level takes over the consumption of events, the first consumer is
     * closed.
     */
    @Test
    public void sameOwnerLevelClosesFirstConsumer() throws InterruptedException {
        // Arrange
        final Semaphore semaphore = new Semaphore(1);
        final String secondPartitionId = "1";
        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setOwnerLevel(1L);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
            position, options);

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final Disposable.Composite subscriptions = Disposables.composite();

        final EventHubProducerAsyncClient producer = client.createProducer();
        subscriptions.add(getEvents(isActive).flatMap(event -> producer.send(event)).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event", error)));

        // Act
        logger.info("STARTED CONSUMING FROM PARTITION 1");
        semaphore.acquire();

        subscriptions.add(consumer.receive(secondPartitionId)
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C1:\tReceived event sequence: {}", event.getEventData().getSequenceNumber()),
                ex -> logger.error("C1:\tERROR", ex),
                () -> {
                    logger.info("C1:\tCompleted.");
                    semaphore.release();
                }));

        Thread.sleep(2000);

        logger.info("STARTED CONSUMING FROM PARTITION 1 with C3");
        final EventHubConsumerAsyncClient consumer2 = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
            position, options);
        subscriptions.add(consumer2.receive(secondPartitionId)
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C3:\tReceived event sequence: {}", event.getEventData().getSequenceNumber()),
                ex -> logger.error("C3:\tERROR", ex),
                () -> logger.info("C3:\tCompleted.")));

        // Assert
        try {
            Assertions.assertTrue(semaphore.tryAcquire(60, TimeUnit.SECONDS), "The EventHubConsumer was not closed after one with a higher epoch number started.");
        } finally {
            subscriptions.dispose();
            isActive.set(false);
            dispose(producer, consumer, consumer2);
        }
    }

    /**
     * Verifies that we can get the metadata about an Event Hub
     */
    @Test
    public void getEventHubProperties() {
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.earliest())
            .buildAsyncConsumer();

        // Act & Assert
        try {
            StepVerifier.create(consumer.getProperties())
                .assertNext(properties -> {
                    Assertions.assertNotNull(properties);
                    Assertions.assertEquals(consumer.getEventHubName(), properties.getName());
                    Assertions.assertEquals(2, properties.getPartitionIds().length);
                }).verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Verifies that we can get the partition identifiers of an Event Hub.
     */
    @Test
    public void getPartitionIds() {
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.earliest())
            .buildAsyncConsumer();

        // Act & Assert
        try {
            StepVerifier.create(consumer.getPartitionIds())
                .expectNextCount(expectedPartitionIds.length)
                .verifyComplete();
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Verifies that we can get partition information for each of the partitions in an Event Hub.
     */
    @Test
    public void getPartitionProperties() {
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.earliest())
            .buildAsyncConsumer();

        // Act & Assert
        try {
            for (String partitionId : expectedPartitionIds) {
                StepVerifier.create(consumer.getPartitionProperties(partitionId))
                    .assertNext(properties -> {
                        Assertions.assertEquals(consumer.getEventHubName(), properties.getEventHubName());
                        Assertions.assertEquals(partitionId, properties.getId());
                    })
                    .verifyComplete();
            }
        } finally {
            dispose(consumer);
        }
    }

    /**
     * Verify that each time we receive an event, the data, {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()},
     * null as we are consuming events.
     */
    @Test
    public void canReceiveMultipleTimes() {
        // Arrange
        final String secondPartitionId = "1";
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = client.createProducer();
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(secondPartitionId)))
            .subscribe(
                sent -> {
                },
                error -> logger.error("Error sending event", error),
                () -> logger.info("Event sent."));

        final EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(1)
            .setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME,
            EventPosition.latest(), options);
        final AtomicReference<LastEnqueuedEventProperties> lastViewed = new AtomicReference<>(
            new LastEnqueuedEventProperties(null, null, null, null));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(secondPartitionId).take(10))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), true))
                .expectNextCount(5)
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getPartitionContext().getLastEnqueuedEventProperties(), false))
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }
    }

    private Flux<EventData> getEvents(AtomicBoolean isActive) {
        return Flux.interval(Duration.ofMillis(500))
            .takeWhile(count -> isActive.get())
            .map(position -> TestUtils.getEvent("Event: " + position, MESSAGE_TRACKING_ID, position.intValue()));
    }
}
