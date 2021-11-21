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
import org.junit.jupiter.api.Tag;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_PREFETCH_COUNT;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests with Azure Event Hubs service. There are other tests that also test {@link
 * EventHubConsumerAsyncClient} in other scenarios.
 *
 * @see SetPrefetchCountTest
 * @see EventPositionIntegrationTest
 */
@Tag(TestUtils.INTEGRATION)
public class EventHubConsumerAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID_HEADER = "SENT_PARTITION_ID";
    private static final String MESSAGE_TRACKING_ID = UUID.randomUUID().toString();

    private EventHubClientBuilder builder;
    private List<String> partitionIds;

    public EventHubConsumerAsyncClientIntegrationTest() {
        super(new ClientLogger(EventHubConsumerAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        builder = createBuilder()
            .shareConnection()
            .consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .prefetchCount(DEFAULT_PREFETCH_COUNT);
        partitionIds = EXPECTED_PARTITION_IDS;
    }

    /**
     * Tests that the same EventHubAsyncClient can create multiple EventHubConsumers listening to different partitions.
     */
    @Test
    public void parallelCreationOfReceivers() {
        // Arrange
        final Map<String, IntegrationTestEventData> testData = getTestData();
        final CountDownLatch countDownLatch = new CountDownLatch(partitionIds.size());
        final EventHubConsumerAsyncClient[] consumers = new EventHubConsumerAsyncClient[partitionIds.size()];
        final Disposable.Composite subscriptions = Disposables.composite();

        // Act
        try {
            for (int i = 0; i < partitionIds.size(); i++) {
                final String partitionId = partitionIds.get(i);
                final IntegrationTestEventData matchingTestData = testData.get(partitionId);

                assertNotNull(matchingTestData,
                    "Did not find matching integration test data for partition: " + partitionId);

                final Instant lastEnqueuedTime = matchingTestData.getPartitionProperties().getLastEnqueuedTime();
                final EventHubConsumerAsyncClient consumer = builder.buildAsyncConsumerClient();
                consumers[i] = consumer;
                final Disposable subscription = consumer.receiveFromPartition(partitionId,
                    EventPosition.fromEnqueuedTime(lastEnqueuedTime))
                    .take(matchingTestData.getEvents().size())
                    .subscribe(
                        event -> logger.info("Event[{}] received. partition: {}",
                            event.getData().getSequenceNumber(), partitionId),
                        error -> Assertions.fail("An error should not have occurred:" + error.toString()),
                        () -> {
                            logger.info("Disposing of consumer now that the receive is complete.");
                            countDownLatch.countDown();
                        });

                subscriptions.add(subscription);
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
        }
    }

    /**
     * Verify if we don't set {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()}, then it is always null as we
     * are consuming events.
     */
    @Test
    public void lastEnqueuedInformationIsNotUpdated() {
        // Arrange
        final String firstPartition = "4";
        final EventHubConsumerAsyncClient consumer = builder.prefetchCount(1).buildAsyncConsumerClient();
        final PartitionProperties properties = consumer.getPartitionProperties(firstPartition).block(TIMEOUT);
        assertNotNull(properties);

        final EventPosition position = EventPosition.fromSequenceNumber(properties.getLastEnqueuedSequenceNumber());
        final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(false);

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final int expectedNumber = 5;
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final SendOptions sendOptions = new SendOptions().setPartitionId(firstPartition);
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, sendOptions))
            .subscribe(sent -> logger.info("Event sent."), error -> logger.error("Error sending event", error));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(firstPartition, position, options)
                .take(expectedNumber))
                .assertNext(event -> Assertions.assertNull(event.getLastEnqueuedEventProperties(),
                    "'lastEnqueuedEventProperties' should be null."))
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
        final String partitionId = "3";
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(partitionId)))
            .subscribe(
                sent -> {
                },
                error -> logger.error("Error sending event", error),
                () -> logger.info("Event sent."));

        final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);

        // Act & Assert
        try (EventHubConsumerAsyncClient consumer = builder.buildAsyncConsumerClient()) {
            final AtomicReference<LastEnqueuedEventProperties> lastViewed = new AtomicReference<>(
                new LastEnqueuedEventProperties(null, null, null, null));

            StepVerifier.create(consumer.receiveFromPartition(partitionId, EventPosition.latest(), options).take(10))
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
        }
    }

    private static void verifyLastRetrieved(AtomicReference<LastEnqueuedEventProperties> atomicReference,
        LastEnqueuedEventProperties current, boolean isFirst) {
        assertNotNull(current);
        final LastEnqueuedEventProperties previous = atomicReference.get();

        // Update the atomic reference to the new one now.
        atomicReference.set(current);

        // The first time we step through this, the retrieval time will not be set for the previous event.
        if (isFirst) {
            return;
        }

        assertNotNull(previous.getRetrievalTime(), "This is not the first event, should have a retrieval "
            + "time.");

        final int compared = previous.getRetrievalTime().compareTo(current.getRetrievalTime());
        final int comparedSequenceNumber = previous.getOffset().compareTo(current.getOffset());
        Assertions.assertTrue(compared <= 0, String.format("Expected retrieval time previous '%s' to be before or "
                + "equal to current '%s'",
            previous.getRetrievalTime(), current.getRetrievalTime()));

        Assertions.assertTrue(comparedSequenceNumber <= 0, String.format("Expected offset previous '%s' to be before "
                + "or equal to current '%s'",
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

        final String lastPartition = "2";
        final EventPosition position = EventPosition.fromEnqueuedTime(Instant.now());
        final ReceiveOptions firstReceive = new ReceiveOptions().setOwnerLevel(1L);
        final ReceiveOptions secondReceive = new ReceiveOptions().setOwnerLevel(2L);
        final EventHubConsumerAsyncClient consumer = builder.prefetchCount(1).buildAsyncConsumerClient();

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final Disposable.Composite subscriptions = Disposables.composite();

        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        subscriptions.add(getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(lastPartition)))
            .subscribe(sent -> logger.info("Event sent."),
                error -> {
                    logger.error("Error sending event", error);
                    Assertions.fail("Should not have failed to publish event.");
                }));

        // Act
        logger.info("STARTED CONSUMING FROM PARTITION 1");
        semaphore.acquire();

        subscriptions.add(consumer.receiveFromPartition(lastPartition, position, firstReceive)
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C1:\tReceived event sequence: {}", event.getData().getSequenceNumber()),
                ex -> {
                    logger.error("C1:\tERROR", ex);
                    semaphore.release();
                }, () -> {
                    logger.info("C1:\tCompleted.");
                    Assertions.fail("Should not be hitting this. An error should occur instead.");
                }));

        Thread.sleep(2000);

        logger.info("STARTED CONSUMING FROM PARTITION 1 with C3");
        final EventHubConsumerAsyncClient consumer2 = builder.buildAsyncConsumerClient();
        subscriptions.add(consumer2.receiveFromPartition(lastPartition, position, secondReceive)
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C3:\tReceived event sequence: {}", event.getData().getSequenceNumber()),
                ex -> {
                    logger.error("C3:\tERROR", ex);
                    Assertions.fail("Should not error here");
                },
                () -> logger.info("C3:\tCompleted.")));

        // Assert
        try {
            Assertions.assertTrue(semaphore.tryAcquire(15, TimeUnit.SECONDS),
                "The EventHubConsumer was not closed after one with a higher epoch number started.");
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
                    assertNotNull(properties);
                    Assertions.assertEquals(consumer.getEventHubName(), properties.getName());
                    Assertions.assertEquals(NUMBER_OF_PARTITIONS, properties.getPartitionIds().stream().count());
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
                .expectNextCount(NUMBER_OF_PARTITIONS)
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
            for (String partitionId : EXPECTED_PARTITION_IDS) {
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
     * Verify that each time we receive an event, the data, and {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()}
     * as we are consuming events.
     */
    @Test
    public void canReceive() {
        // Arrange
        final String secondPartitionId = "1";
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(secondPartitionId)))
            .subscribe(
                sent -> {
                },
                error -> logger.error("Error sending event", error),
                () -> logger.info("Event sent."));

        final ReceiveOptions options = new ReceiveOptions()
            .setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient consumer = builder.prefetchCount(1).buildAsyncConsumerClient();
        final AtomicReference<LastEnqueuedEventProperties> lastViewed = new AtomicReference<>(
            new LastEnqueuedEventProperties(null, null, null, null));

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(secondPartitionId, EventPosition.latest(), options).take(10))
                .assertNext(event -> {
                    final EventData eventData = event.getData();
                    assertNotNull(eventData.getOffset(), "'getOffset' cannot be null.");
                    assertNotNull(eventData.getSequenceNumber(), "'getSequenceNumber' cannot be null.");
                    assertNotNull(eventData.getEnqueuedTime(), "'getEnqueuedTime' cannot be null.");

                    assertNotNull(eventData.getSystemProperties().get(OFFSET_ANNOTATION_NAME.getValue()));
                    assertNotNull(eventData.getSystemProperties().get(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()));
                    assertNotNull(eventData.getSystemProperties().get(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()));

                    verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), true);
                })
                .expectNextCount(5)
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .assertNext(event -> verifyLastRetrieved(lastViewed, event.getLastEnqueuedEventProperties(), false))
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            dispose(producer, consumer);
        }
    }

    @Test
    public void receivesMultiplePartitions() {
        // Arrange
        final EventHubConsumerAsyncClient consumer = builder.prefetchCount(1).buildAsyncConsumerClient();

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final AtomicInteger counter = new AtomicInteger();
        final Set<Integer> allPartitions = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(
            consumer.getPartitionIds().map(Integer::valueOf).collectList().block(TIMEOUT))));

        // This is the one we'll mutate.
        final Set<Integer> expectedPartitions = new HashSet<>(allPartitions);
        final int expectedNumber = 6;

        Assumptions.assumeTrue(expectedPartitions.size() <= expectedNumber,
            "Cannot run this test if there are more partitions than expected.");

        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
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
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions,
                    expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions,
                    expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions,
                    expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions,
                    expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions,
                    expectedPartitions))
                .assertNext(event -> assertPartitionEvent(event, producer.getEventHubName(), allPartitions,
                    expectedPartitions))
                .verifyComplete();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }

        Assertions.assertTrue(expectedPartitions.isEmpty(), "Expected messages to be received from all partitions. "
            + "There are: " + expectedPartitions.size());
    }

    /**
     * Verifies we can receive from the same partition concurrently.
     */
    @Test
    public void multipleReceiversSamePartition() throws InterruptedException {
        // Arrange
        final EventHubConsumerAsyncClient consumer = builder.prefetchCount(1).buildAsyncConsumerClient();
        final EventHubConsumerAsyncClient consumer2 = builder.buildAsyncConsumerClient();
        final String partitionId = "1";
        final PartitionProperties properties = consumer.getPartitionProperties(partitionId).block(TIMEOUT);
        assertNotNull(properties, "Should have been able to get partition properties.");

        final int numberToTake = 10;
        final CountDownLatch countdown1 = new CountDownLatch(numberToTake);
        final CountDownLatch countdown2 = new CountDownLatch(numberToTake);
        final EventPosition position = EventPosition.fromSequenceNumber(properties.getLastEnqueuedSequenceNumber());

        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final Disposable producerEvents = getEvents(isActive).flatMap(event -> {
            event.getProperties().put(PARTITION_ID_HEADER, partitionId);
            return producer.send(event, new SendOptions().setPartitionId(partitionId));
        }).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event. Exception:" + error, error),
            () -> logger.info("Completed"));

        consumer.receiveFromPartition(partitionId, position)
            .filter(x -> TestUtils.isMatchingEvent(x.getData(), MESSAGE_TRACKING_ID))
            .take(numberToTake)
            .subscribe(event -> {
                logger.info("Consumer1: Event received");
                countdown1.countDown();
            });

        consumer2.receiveFromPartition(partitionId, position)
            .filter(x -> TestUtils.isMatchingEvent(x.getData(), MESSAGE_TRACKING_ID))
            .take(numberToTake)
            .subscribe(event -> {
                logger.info("Consumer2: Event received");
                countdown2.countDown();
            });

        // Assert
        try {
            boolean successful = countdown1.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            boolean successful2 = countdown2.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);

            Assertions.assertTrue(successful,
                String.format("Expected to get %s events. Got: %s", numberToTake, countdown1.getCount()));
            Assertions.assertTrue(successful2,
                String.format("Expected to get %s events. Got: %s", numberToTake, countdown2.getCount()));
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
            consumer2.close();
        }
    }

    /**
     * Verifies that we are properly closing the receiver after each receive operation that terminates the upstream
     * flux.
     */
    @Test
    void closesReceiver() throws InterruptedException {
        // Arrange
        final String partitionId = "1";
        final SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
        final EventHubConsumerAsyncClient consumer = builder.prefetchCount(1).buildAsyncConsumerClient();
        final int numberOfEvents = 5;
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final PartitionProperties properties = producer.getPartitionProperties(partitionId).block(TIMEOUT);

        assertNotNull(properties);

        final AtomicReference<EventPosition> startingPosition = new AtomicReference<>(
            EventPosition.fromSequenceNumber(properties.getLastEnqueuedSequenceNumber()));
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, sendOptions).thenReturn(Instant.now()))
            .subscribe(time -> logger.verbose("Sent event at: {}", time),
                error -> logger.error("Error sending event.", error),
                () -> logger.info("Completed"));

        // Act & Assert
        try {
            for (int i = 0; i < 7; i++) {
                logger.info("[{}]: Starting iteration", i);

                final List<PartitionEvent> events = consumer.receiveFromPartition(partitionId, startingPosition.get())
                    .take(numberOfEvents)
                    .collectList()
                    .block(Duration.ofSeconds(15));

                Thread.sleep(700);
                assertNotNull(events);
                Assertions.assertEquals(numberOfEvents, events.size());
            }
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            consumer.close();
        }
    }

    /**
     * Verify that when we specify backpressure, events are no longer fetched after we've reached the subscribed
     * amount.
     */
    @Test
    void canReceiveWithBackpressure() {
        // Arrange
        final int backpressure = 15;
        final String secondPartitionId = "2";
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(secondPartitionId)))
            .subscribe(
                sent -> {
                },
                error -> logger.error("Error sending event", error),
                () -> logger.info("Event sent."));

        final ReceiveOptions options = new ReceiveOptions()
            .setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient consumer = builder
            .prefetchCount(2)
            .buildAsyncConsumerClient();

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(secondPartitionId, EventPosition.latest(), options), backpressure)
                .expectNextCount(backpressure)
                .thenAwait(Duration.ofSeconds(5))
                .thenCancel()
                .verify();
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            dispose(producer, consumer);
        }
    }

    /**
     * Verify that when we specify a small prefetch, it continues to fetch items.
     */
    @Test
    void receivesWithSmallPrefetch() {
        // Arrange
        final String secondPartitionId = "2";
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final Disposable producerEvents = getEvents(isActive)
            .flatMap(event -> producer.send(event, new SendOptions().setPartitionId(secondPartitionId)))
            .subscribe(
                sent -> {
                },
                error -> logger.error("Error sending event", error),
                () -> logger.info("Event sent."));

        final int prefetch = 5;
        final int backpressure = 3;
        final int batchSize = 10;
        final EventHubConsumerAsyncClient consumer = builder
            .prefetchCount(prefetch)
            .buildAsyncConsumerClient();

        // Act & Assert
        try {
            StepVerifier.create(consumer.receiveFromPartition(secondPartitionId, EventPosition.latest()), prefetch)
                .expectNextCount(prefetch)
                .thenRequest(backpressure)
                .expectNextCount(backpressure)
                .thenRequest(batchSize)
                .expectNextCount(batchSize)
                .thenRequest(batchSize)
                .expectNextCount(batchSize)
                .thenAwait(Duration.ofSeconds(1))
                .thenCancel()
                .verify(TIMEOUT);
        } finally {
            isActive.set(false);
            producerEvents.dispose();
            dispose(producer, consumer);
        }
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
