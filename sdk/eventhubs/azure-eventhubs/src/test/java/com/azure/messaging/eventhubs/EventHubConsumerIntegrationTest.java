// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
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
import java.util.function.Consumer;

import static com.azure.core.amqp.exception.ErrorCondition.RESOURCE_LIMIT_EXCEEDED;
import static com.azure.messaging.eventhubs.EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME;

/**
 * Integration tests with Azure Event Hubs service. There are other tests that also test {@link EventHubConsumer} in
 * other scenarios.
 *
 * @see SetPrefetchCountTest
 * @see EventPositionIntegrationTest
 */
public class EventHubConsumerIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";
    // The maximum number of receivers on a partition + consumer group is 5.
    private static final int MAX_NUMBER_OF_CONSUMERS = 5;
    private static final String MESSAGE_TRACKING_ID = UUID.randomUUID().toString();

    private EventHubAsyncClient client;

    public EventHubConsumerIntegrationTest() {
        super(new ClientLogger(EventHubConsumerIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        skipIfNotRecordMode();

        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        final ConnectionStringProperties properties = new ConnectionStringProperties(getConnectionString());
        final ConnectionOptions connectionOptions = new ConnectionOptions(properties.endpoint().getHost(),
            properties.eventHubName(), getTokenCredential(), getAuthorizationType(), TransportType.AMQP,
            RETRY_OPTIONS, ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.parallel());

        client = new EventHubAsyncClient(connectionOptions, getReactorProvider(), handlerProvider);
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
        skipIfNotRecordMode();

        // Arrange
        final int numberOfEvents = 10;
        final List<String> partitionIds = client.getPartitionIds().collectList().block(TIMEOUT);
        if (partitionIds == null || partitionIds.isEmpty()) {
            Assert.fail("Should have partitions");
        }

        final CountDownLatch countDownLatch = new CountDownLatch(partitionIds.size());
        final EventHubConsumer[] consumers = new EventHubConsumer[partitionIds.size()];
        final EventHubProducer[] producers = new EventHubProducer[partitionIds.size()];
        final Disposable.Composite subscriptions = Disposables.composite();
        try {
            for (int i = 0; i < partitionIds.size(); i++) {
                final String partitionId = partitionIds.get(i);
                final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, partitionId,
                    EventPosition.fromEnqueuedTime(Instant.now()));
                consumers[i] = consumer;

                final Disposable subscription = consumer.receive().take(numberOfEvents).subscribe(event -> {
                    logger.info("Event[{}] received. partition: {}", event.sequenceNumber(), partitionId);
                }, error -> {
                        Assert.fail("An error should not have occurred:" + error.toString());
                    }, () -> {
                        logger.info("Disposing of consumer now that the receive is complete.");
                        countDownLatch.countDown();
                    });

                subscriptions.add(subscription);

                producers[i] = client.createProducer(new EventHubProducerOptions().partitionId(partitionId));
            }

            // Act
            Flux.fromArray(producers).flatMap(producer -> producer.send(TestUtils.getEvents(numberOfEvents, MESSAGE_TRACKING_ID)))
                .blockLast(TIMEOUT);

            // Assert
            // Wait for all the events we sent to be received.
            countDownLatch.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            Assert.assertEquals(0, countDownLatch.getCount());
        } catch (InterruptedException e) {
            Assert.fail("Countdown latch was interrupted:" + e.toString());
        } finally {
            logger.info("Disposing of subscriptions, consumers, producers.");

            subscriptions.dispose();
            dispose(consumers);
            dispose(producers);
        }
    }

    /**
     * Verify that if we set the identifier in the consumer, it shows up in the quota error.
     */
    @Ignore("Investigate. The sixth receiver is not causing an exception to be thrown.")
    @Test
    public void consumerIdentifierShowsUpInQuotaErrors() {
        // Arrange
        final String prefix = UUID.randomUUID().toString();
        final Consumer<AmqpException> validateException = error -> {
            Assert.assertEquals(RESOURCE_LIMIT_EXCEEDED, error.getErrorCondition());

            final String errorMsg = error.getMessage();
            for (int i = 0; i < MAX_NUMBER_OF_CONSUMERS; i++) {
                Assert.assertTrue(errorMsg.contains(prefix + ":" + i));
            }
        };

        final List<EventHubConsumer> consumers = new ArrayList<>();
        final Disposable.Composite subscriptions = Disposables.composite();
        EventHubConsumer exceededConsumer = null;
        try {
            for (int i = 0; i < MAX_NUMBER_OF_CONSUMERS; i++) {
                final EventHubConsumerOptions options = new EventHubConsumerOptions().identifier(prefix + ":" + i);
                final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.earliest(), options);
                consumers.add(consumer);
                subscriptions.add(consumer.receive().take(TIMEOUT).subscribe(eventData -> {
                    // Received an event. We don't need to log it though.
                }));
            }

            // Act & Verify
            exceededConsumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, PARTITION_ID, EventPosition.earliest());
            StepVerifier.create(exceededConsumer.receive())
                .expectErrorSatisfies(exception -> {
                    Assert.assertTrue(exception instanceof AmqpException);
                    validateException.accept((AmqpException) exception);
                })
                .verify();
        } catch (AmqpException e) {
            validateException.accept(e);
        } finally {
            subscriptions.dispose();
            dispose(exceededConsumer);
            dispose(consumers.toArray(new EventHubConsumer[0]));
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
            .ownerLevel(1L);
        final EventHubConsumer consumer = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, secondPartitionId,
            position, options);
        final EventHubConsumer consumer2 = client.createConsumer(DEFAULT_CONSUMER_GROUP_NAME, secondPartitionId,
            position, options);
        final AtomicBoolean isActive = new AtomicBoolean(true);
        final Disposable.Composite subscriptions = Disposables.composite();

        final EventHubProducer producer = client.createProducer();
        subscriptions.add(getEvents(isActive).flatMap(event -> producer.send(event)).subscribe(
            sent -> logger.info("Event sent."),
            error -> logger.error("Error sending event", error)));

        // Act
        logger.info("STARTED CONSUMING FROM PARTITION 1");
        semaphore.acquire();

        subscriptions.add(consumer.receive()
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C1:\tReceived event sequence: {}", event.sequenceNumber()),
                ex -> logger.error("C1:\tERROR", ex),
                () -> {
                    logger.info("C1:\tCompleted.");
                    semaphore.release();
                }));

        Thread.sleep(2000);

        logger.info("STARTED CONSUMING FROM PARTITION 1 with a different consumer");
        subscriptions.add(consumer2.receive()
            .filter(event -> TestUtils.isMatchingEvent(event, MESSAGE_TRACKING_ID))
            .subscribe(
                event -> logger.info("C3:\tReceived event sequence: {}", event.sequenceNumber()),
                ex -> logger.error("C3:\tERROR", ex),
                () -> logger.info("C3:\tCompleted.")));

        // Assert
        try {
            boolean success = semaphore.tryAcquire(15, TimeUnit.SECONDS);
            Assert.assertTrue(success);
        } finally {
            subscriptions.dispose();
            isActive.set(false);
            dispose(producer, consumer, consumer2);
        }
    }

    private Flux<EventData> getEvents(AtomicBoolean isActive) {
        return Flux.interval(Duration.ofMillis(500))
            .takeWhile(count -> isActive.get())
            .map(position -> TestUtils.getEvent("Event: " + position, MESSAGE_TRACKING_ID, position.intValue()));
    }
}
