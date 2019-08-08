// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLink;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import org.apache.qpid.proton.message.Message;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests to verify functionality of {@link EventHubConsumer}.
 */
public class EventHubConsumerTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;

    private final ClientLogger logger = new ClientLogger(EventHubConsumerTest.class);
    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final DirectProcessor<Throwable> errorProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpShutdownSignal> shutdownProcessor = DirectProcessor.create();

    @Mock
    private AmqpReceiveLink amqpReceiveLink;

    @Captor
    private ArgumentCaptor<Supplier<Integer>> creditSupplier;

    private Mono<AmqpReceiveLink> receiveLinkMono;
    private List<Message> messages = new ArrayList<>();
    private EventHubConsumerOptions options;
    private EventHubConsumer consumer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        receiveLinkMono = Mono.fromCallable(() -> amqpReceiveLink);

        when(amqpReceiveLink.receive()).thenReturn(messageProcessor);
        when(amqpReceiveLink.getErrors()).thenReturn(errorProcessor);
        when(amqpReceiveLink.getConnectionStates()).thenReturn(endpointProcessor);
        when(amqpReceiveLink.getShutdownSignals()).thenReturn(shutdownProcessor);

        options = new EventHubConsumerOptions()
            .identifier("an-identifier")
            .prefetchCount(PREFETCH)
            .retry(new RetryOptions())
            .scheduler(Schedulers.elastic());
        consumer = new EventHubConsumer(receiveLinkMono, options);
    }

    @After
    public void teardown() throws IOException {
        messages.clear();
        Mockito.framework().clearInlineMocks();
        consumer.close();
    }

    /**
     * Verifies that this receives a number of events. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    public void receivesNumberOfEvents() {
        // Arrange
        final int numberOfEvents = 10;

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .then(() -> sendMessages(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        verify(amqpReceiveLink, times(1)).addCredits(PREFETCH);
    }

    /**
     * Verifies that we can resubscribe to the receiver multiple times.
     */
    @Test
    public void canResubscribeToConsumer() {
        // Arrange
        final int numberOfEvents = 10;

        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents, 0);

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .then(() -> sendMessages(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .then(() -> sendMessages(numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        // After the initial prefetch, when we subscribe, and when we do, it'll ask for Long.MAXVALUE, which will set
        // the limit request to MAXIMUM_REQUEST = 100.
        verify(amqpReceiveLink, times(1)).addCredits(PREFETCH);
        verify(amqpReceiveLink, times(1)).addCredits(100);
    }

    /**
     * Verify that receive can have multiple subscribers.
     */
    @Test
    public void canHaveMultipleSubscribers() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 7;
        final CountDownLatch firstConsumerCountDown = new CountDownLatch(numberOfEvents);
        final CountDownLatch secondConsumerCountDown = new CountDownLatch(numberOfEvents);
        final CountDownLatch thirdCountDownEvent = new CountDownLatch(numberOfEvents);

        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        // Act
        final Disposable.Composite subscriptions = Disposables.composite(
            consumer.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID)).take(numberOfEvents)
                .subscribe(event -> firstConsumerCountDown.countDown()),
            consumer.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID)).take(numberOfEvents)
                .subscribe(event -> secondConsumerCountDown.countDown()),
            consumer.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID)).take(numberOfEvents)
                .subscribe(event -> thirdCountDownEvent.countDown())
        );

        sendMessages(numberOfEvents);
        try {
            firstConsumerCountDown.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            secondConsumerCountDown.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            thirdCountDownEvent.await(TIMEOUT.getSeconds(), TimeUnit.SECONDS);
        } finally {
            subscriptions.dispose();
        }

        // Assert that we were able to see all these events.
        Assert.assertEquals(0, firstConsumerCountDown.getCount());
        Assert.assertEquals(0, secondConsumerCountDown.getCount());
        Assert.assertEquals(0, thirdCountDownEvent.getCount());
    }

    /**
     * Verifies that we can limit the number of deliveries added on the link at a given time.
     */
    @Test
    public void canLimitRequestsBackpressure() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 20;
        final int backpressureRequest = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);
        consumer.receive().take(numberOfEvents).subscribe(new BaseSubscriber<EventData>() {
            final AtomicInteger count = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(backpressureRequest);
                super.hookOnSubscribe(subscription);
            }

            @Override
            protected void hookOnNext(EventData value) {
                if (count.incrementAndGet() == backpressureRequest) {
                    request(backpressureRequest);
                    count.set(0);
                }

                logger.verbose("Event Received. {}", countDownLatch.getCount());
                countDownLatch.countDown();
                super.hookOnNext(value);
            }
        });

        // Act
        sendMessages(numberOfEvents);
        countDownLatch.await(30, TimeUnit.SECONDS);

        // Assert
        Assert.assertEquals(0, countDownLatch.getCount());
        verify(amqpReceiveLink, atLeastOnce()).addCredits(PREFETCH);
    }

    /**
     * Verifies that if we have limited the request, the number of credits added is the same as that limit.
     */
    @Test
    public void returnsCorrectCreditRequest() throws InterruptedException {
        // Arrange
        final int numberOfEvents = 20;
        final int backpressureRequest = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfEvents);

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);
        consumer.receive().take(numberOfEvents).subscribe(new BaseSubscriber<EventData>() {
            final AtomicInteger count = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                subscription.request(backpressureRequest);
                super.hookOnSubscribe(subscription);
            }

            @Override
            protected void hookOnNext(EventData value) {
                if (count.incrementAndGet() == backpressureRequest) {
                    request(backpressureRequest);
                    count.set(0);
                }

                logger.info("Event Received. {}", countDownLatch.getCount());
                countDownLatch.countDown();
                super.hookOnNext(value);
            }
        });

        // Act
        sendMessages(numberOfEvents);
        countDownLatch.await(30, TimeUnit.SECONDS);

        // Assert
        Assert.assertEquals(0, countDownLatch.getCount());
        verify(amqpReceiveLink, atLeastOnce()).addCredits(PREFETCH);
    }

    /**
     * Verify that the correct number of credits are returned when the link is empty, and there are subscribers.
     */
    @Test
    public void suppliesCreditsWhenSubscribers() {
        // Arrange
        final int backPressure = 8;

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);

        final Disposable subscription = consumer.receive().subscribe(
            e -> logger.info("Event received"),
            error -> Assert.fail(error.toString()),
            () -> logger.info("Complete"), sub -> {
                sub.request(backPressure);
            });

        try {
            // Act
            // Capturing the credit supplier that we set when the link was received.
            verify(amqpReceiveLink).setEmptyCreditListener(creditSupplier.capture());
            final Supplier<Integer> supplier = creditSupplier.getValue();
            final int actualCredits = supplier.get();

            // Assert
            Assert.assertEquals(backPressure, actualCredits);
        } finally {
            subscription.dispose();
        }
    }

    /**
     * Verify that 0 credits are returned when there are no subscribers for this link anymore.
     */
    @Test
    public void suppliesNoCreditsWhenNoSubscribers() {
        // Arrange
        final int backPressure = 8;

        when(amqpReceiveLink.getCredits()).thenReturn(PREFETCH);

        final Disposable subscription = consumer.receive().subscribe(
            e -> logger.info("Event received"),
            error -> Assert.fail(error.toString()),
            () -> logger.info("Complete"),
            sub -> sub.request(backPressure));

        // Capturing the credit supplier that we set when the link was received.
        verify(amqpReceiveLink).setEmptyCreditListener(creditSupplier.capture());
        final Supplier<Integer> supplier = creditSupplier.getValue();

        // Disposing of the downstream listener we had.
        subscription.dispose();

        // Act
        final int actualCredits = supplier.get();

        // Assert
        Assert.assertEquals(0, actualCredits);
    }

    /**
     * Verifies that the consumer closes and completes any listeners on a shutdown signal.
     */
    @Test
    public void listensToShutdownSignals() throws InterruptedException, IOException {
        // Arrange
        final int numberOfEvents = 7;
        final CountDownLatch shutdownReceived = new CountDownLatch(3);
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false,
            "Test message");

        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        final Disposable.Composite subscriptions = Disposables.composite(
            consumer.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .subscribe(
                    event -> logger.verbose("1. Received: {}", event.sequenceNumber()),
                    error -> Assert.fail(error.toString()),
                    () -> {
                        logger.info("1. Shutdown received");
                        shutdownReceived.countDown();
                    }),
            consumer.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .subscribe(
                    event -> logger.verbose("2. Received: {}", event.sequenceNumber()),
                    error -> Assert.fail(error.toString()),
                    () -> {
                        logger.info("2. Shutdown received");
                        shutdownReceived.countDown();
                    }),
            consumer.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID))
                .subscribe(
                    event -> logger.verbose("3. Received: {}", event.sequenceNumber()),
                    error -> Assert.fail(error.toString()),
                    () -> {
                        logger.info("3. Shutdown received");
                        shutdownReceived.countDown();
                    }));

        // Act
        sendMessages(numberOfEvents);
        shutdownProcessor.onNext(shutdownSignal);

        // Assert
        try {
            boolean successful = shutdownReceived.await(5, TimeUnit.SECONDS);
            Assert.assertTrue(successful);
            Assert.assertEquals(0, shutdownReceived.getCount());
            verify(amqpReceiveLink, times(1)).close();
        } finally {
            subscriptions.dispose();
        }
    }

    private void sendMessages(int numberOfEvents) {
        // When we start receiving, then send those 10 messages.
        FluxSink<Message> sink = messageProcessor.sink();
        for (int i = 0; i < numberOfEvents; i++) {
            sink.next(getMessage(PAYLOAD_BYTES, messageTrackingUUID));
        }
    }
}
