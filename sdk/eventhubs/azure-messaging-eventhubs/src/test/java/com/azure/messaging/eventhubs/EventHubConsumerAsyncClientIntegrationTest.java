// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;

/**
 * Integration tests with Azure Event Hubs service. There are other tests that also test {@link
 * EventHubConsumerAsyncClient} in other scenarios.
 *
 * @see SetPrefetchCountTest
 * @see EventPositionIntegrationTest
 */
public class EventHubConsumerAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID_HEADER = "SENT_PARTITION_ID";

    private final String[] expectedPartitionIds = new String[]{"0", "1"};

    private static final String MESSAGE_TRACKING_ID = UUID.randomUUID().toString();

    private EventHubAsyncClient client;

    public EventHubConsumerAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubConsumerAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        client = createBuilder()
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
                final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, DEFAULT_PREFETCH_COUNT);
                consumers[i] = consumer;

                final Disposable subscription = consumer.receiveFromPartition(partitionId, EventPosition.fromEnqueuedTime(Instant.now()))
                    .take(numberOfEvents)
                    .subscribe(
                        event -> logger.info("Event[{}] received. partition: {}", event.getData().getSequenceNumber(), partitionId),
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
     * Verify if we don't set {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()}, then it is always
     * null as we are consuming events.
     */
    @Test
    public void lastEnqueuedInformationIsNotUpdated() {
        // Arrange
        final String secondPartitionId = "1";
        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 1);
        final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(false);

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final int expectedNumber = 5;
        final EventHubProducerAsyncClient producer = client.createProducer();
        final Disposable producerEvents = getEvents(isActive).flatMap(event -> producer.send(event)).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event", error));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(secondPartitionId, position, options)
                .take(expectedNumber))
                .assertNext(event -> {
                    Assertions.assertNull(event.getLastEnqueuedEventProperties(), "'lastEnqueuedEventProperties' should be null.");
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
     * Verify that each time we receive an event, the data, {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()},
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

        final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 1);
        final AtomicReference<LastEnqueuedEventProperties> lastViewed = new AtomicReference<>(
            new LastEnqueuedEventProperties(null, null, null, null));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(secondPartitionId, EventPosition.latest(), options).take(10))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), true))
                .expectNextCount(5)
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
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
     * Verifies when a consumer with the same owner level takes over the consumption of events, the first consumer is
     * closed.
     */
    @Test
    public void sameOwnerLevelClosesFirstConsumer() throws InterruptedException {
        // Arrange
        final Semaphore semaphore = new Semaphore(1);
        final String secondPartitionId = "1";
        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final ReceiveOptions options = new ReceiveOptions()
            .setOwnerLevel(1L);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 1);

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final Disposable.Composite subscriptions = Disposables.composite();

        final EventHubProducerAsyncClient producer = client.createProducer();
        subscriptions.add(getEvents(isActive).flatMap(event -> producer.send(event)).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event", error)));

        // Act
        logger.info("STARTED CONSUMING FROM PARTITION 1");
        semaphore.acquire();

        subscriptions.add(consumer.receiveFromPartition(secondPartitionId, position)
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C1:\tReceived event sequence: {}", event.getData().getSequenceNumber()),
                ex -> logger.error("C1:\tERROR", ex),
                () -> {
                    logger.info("C1:\tCompleted.");
                    semaphore.release();
                }));

        Thread.sleep(2000);

        logger.info("STARTED CONSUMING FROM PARTITION 1 with C3");
        final EventHubConsumerAsyncClient consumer2 = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 1);
        subscriptions.add(consumer2.receiveFromPartition(secondPartitionId, position, options)
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C3:\tReceived event sequence: {}", event.getData().getSequenceNumber()),
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
            .buildAsyncConsumerClient();

        // Act & Assert
        try {
            StepVerifier.create(consumer.getEventHubProperties())
                .assertNext(properties -> {
                    Assertions.assertNotNull(properties);
                    Assertions.assertEquals(consumer.getEventHubName(), properties.getName());
                    Assertions.assertEquals(2, properties.getPartitionIds().stream().count());
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
            .buildAsyncConsumerClient();

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
            .buildAsyncConsumerClient();

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
     * Verify that each time we receive an event, the data, and
     * {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()} as we are consuming events.
     */
    @Test
    public void canReceive() {
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

        final ReceiveOptions options = new ReceiveOptions()
            .setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 1);
        final AtomicReference<LastEnqueuedEventProperties> lastViewed = new AtomicReference<>(
            new LastEnqueuedEventProperties(null, null, null, null));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(secondPartitionId, EventPosition.latest(), options).take(10))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), true))
                .expectNextCount(5)
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }
    }

    @Test
    public void receivesMultiplePartitions() {
        // Arrange
        final EventHubConsumerAsyncClient consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, 1);

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final AtomicInteger counter = new AtomicInteger();
        final Set<Integer> allPartitions = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(
            consumer.getPartitionIds().map(Integer::valueOf).collectList().block(TIMEOUT))));

        // This is the one we'll mutate.
        final Set<Integer> expectedPartitions = new HashSet<>(allPartitions);
        final int expectedNumber = 6;

        Assumptions.assumeTrue(expectedPartitions.size() <= expectedNumber,
            "Cannot run this test if there are more partitions than expected.");

        final EventHubProducerAsyncClient producer = client.createProducer();
        final Disposable producerEvents = getEvents(isActive).flatMap(event -> {
            final int partition = counter.getAndIncrement() % allPartitions.size();
            event.getProperties().put(PARTITION_ID_HEADER, partition);
            return producer.send(event, new SendOptions().setPartitionId(String.valueOf(partition)));
        }).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event. Exception:" + error, error),
            () -> logger.info("Completed"));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receive(false)
                .filter(x -> TestUtils.isMatchingEvent(x.getData(), MESSAGE_TRACKING_ID))
                .take(expectedNumber))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions, expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions, expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions, expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions, expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions, expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions, expectedPartitions))
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }

        Assertions.assertTrue(expectedPartitions.isEmpty(), "Expected messages to be received from all partitions. There are: " + expectedPartitions.size());
    }

    private static void assertPartitionEvent(PartitionEvent event, String eventHubName, Set<Integer> allPartitions,
        Set<Integer> expectedPartitions) {
        final PartitionContext context = event.getPartitionContext();
        Assertions.assertEquals(eventHubName, context.getEventHubName());

        final EventData eventData = event.getData();
        final Integer partitionId = Integer.valueOf(context.getPartitionId());

        Assertions.assertTrue(eventData.getProperties().containsKey(PARTITION_ID_HEADER));

        final Object eventPartitionObject = eventData.getProperties().get(PARTITION_ID_HEADER);
        Assertions.assertTrue(eventPartitionObject instanceof Integer);
        final Integer eventPartition = (Integer) eventPartitionObject;

        Assertions.assertEquals(partitionId, eventPartition);
        Assertions.assertTrue(allPartitions.contains(partitionId));

        expectedPartitions.remove(partitionId);
    }

    private Flux<EventData> getEvents(AtomicBoolean isActive) {
        return Flux.interval(Duration.ofMillis(500))
            .takeWhile(count -> isActive.get())
            .map(position -> TestUtils.getEvent("Event: " + position, MESSAGE_TRACKING_ID, position.intValue()));
    }
}
