// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLinkProcessor;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventHubPartitionAsyncConsumerTest {
    private static final int PREFETCH = 5;
    private static final String HOSTNAME = "hostname-foo";
    private static final String EVENT_HUB_NAME = "event-hub-name";
    private static final String CONSUMER_GROUP = "consumer-group-test";
    private static final String PARTITION_ID = "a-partition-id";
    private static final Date TEST_DATE = Date.from(Instant.ofEpochSecond(1578643343));

    @Mock
    private AmqpReceiveLink link1;
    @Mock
    private AmqpReceiveLink link2;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private Message message1;
    @Mock
    private Message message2;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private Disposable parentConnection;

    private final EventPosition originalPosition = EventPosition.latest();
    private final AtomicReference<Supplier<EventPosition>> currentPosition = new AtomicReference<>(() -> originalPosition);
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();

    private final ClientLogger logger = new ClientLogger(EventHubPartitionAsyncConsumerTest.class);
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageProcessorSink = messageProcessor.sink();

    private AmqpReceiveLinkProcessor linkProcessor;
    private EventHubPartitionAsyncConsumer consumer;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(link1.getEndpointStates()).thenReturn(endpointProcessor);
        when(link1.receive()).thenReturn(messageProcessor);
    }

    @AfterEach
    void teardown() {
        if (consumer != null) {
            consumer.close();
        }

        linkProcessor.cancel();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void receivesMessages(boolean trackLastEnqueuedProperties) {
        // Arrange
        linkProcessor = createSink(link1, link2).subscribeWith(new AmqpReceiveLinkProcessor(PREFETCH, retryPolicy, parentConnection));
        consumer = new EventHubPartitionAsyncConsumer(linkProcessor, messageSerializer, HOSTNAME, EVENT_HUB_NAME,
            CONSUMER_GROUP, PARTITION_ID, currentPosition, trackLastEnqueuedProperties, Schedulers.parallel());

        final EventData event1 = new EventData("Foo");
        final EventData event2 = new EventData("Bar");
        final LastEnqueuedEventProperties last1 = new LastEnqueuedEventProperties(10L, 15L,
            Instant.ofEpochMilli(1243454), Instant.ofEpochMilli(1240004));
        final LastEnqueuedEventProperties last2 = new LastEnqueuedEventProperties(1005L, 154L,
            Instant.ofEpochMilli(8796254), Instant.ofEpochMilli(8795200));

        when(messageSerializer.deserialize(same(message1), eq(EventData.class))).thenReturn(event1);
        when(messageSerializer.deserialize(same(message1), eq(LastEnqueuedEventProperties.class))).thenReturn(last1);

        when(messageSerializer.deserialize(same(message2), eq(EventData.class))).thenReturn(event2);
        when(messageSerializer.deserialize(same(message2), eq(LastEnqueuedEventProperties.class))).thenReturn(last2);

        // Act & Assert
        StepVerifier.create(consumer.receive())
            .then(() -> {
                messageProcessorSink.next(message1);
                messageProcessorSink.next(message2);
            })
            .assertNext(partitionEvent -> {
                verifyPartitionContext(partitionEvent.getPartitionContext());
                verifyLastEnqueuedInformation(trackLastEnqueuedProperties, last1,
                    partitionEvent.getLastEnqueuedEventProperties());

                Assertions.assertSame(event1, partitionEvent.getData());
            })
            .assertNext(partitionEvent -> {
                verifyPartitionContext(partitionEvent.getPartitionContext());
                verifyLastEnqueuedInformation(trackLastEnqueuedProperties, last2,
                    partitionEvent.getLastEnqueuedEventProperties());

                Assertions.assertSame(event2, partitionEvent.getData());
            })
            .thenCancel()
            .verify();

        // The emitter processor is not closed until the partition consumer is.
        Assertions.assertFalse(linkProcessor.isTerminated());
        Assertions.assertSame(originalPosition, currentPosition.get().get());
    }

    @Test
    void receiveMultipleTimes() {
        // Arrange
        linkProcessor = createSink(link1, link2).subscribeWith(new AmqpReceiveLinkProcessor(PREFETCH, retryPolicy, parentConnection));
        consumer = new EventHubPartitionAsyncConsumer(linkProcessor, messageSerializer, HOSTNAME, EVENT_HUB_NAME,
            CONSUMER_GROUP, PARTITION_ID, currentPosition, false, Schedulers.parallel());

        final Message message3 = mock(Message.class);
        final String secondOffset = "54";
        final String lastOffset = "65";
        final EventData event1 = new EventData(BinaryData.fromBytes("Foo".getBytes()), getSystemProperties("25", 14),
            Context.NONE);
        final EventData event2 = new EventData(BinaryData.fromBytes("Bar".getBytes()),
            getSystemProperties(secondOffset, 21), Context.NONE);
        final EventData event3 = new EventData(BinaryData.fromBytes("Baz".getBytes()),
            getSystemProperties(lastOffset, 53), Context.NONE);

        when(messageSerializer.deserialize(same(message1), eq(EventData.class))).thenReturn(event1);
        when(messageSerializer.deserialize(same(message2), eq(EventData.class))).thenReturn(event2);
        when(messageSerializer.deserialize(same(message3), eq(EventData.class))).thenReturn(event3);

        // Act & Assert
        StepVerifier.create(consumer.receive())
            .then(() -> {
                messageProcessorSink.next(message1);
                messageProcessorSink.next(message2);
            })
            .assertNext(partitionEvent -> {
                verifyPartitionContext(partitionEvent.getPartitionContext());
                verifyLastEnqueuedInformation(false, null, partitionEvent.getLastEnqueuedEventProperties());
                Assertions.assertSame(event1, partitionEvent.getData());
            })
            .assertNext(partitionEvent -> {
                verifyPartitionContext(partitionEvent.getPartitionContext());
                verifyLastEnqueuedInformation(false, null, partitionEvent.getLastEnqueuedEventProperties());
                Assertions.assertSame(event2, partitionEvent.getData());
            })
            .thenCancel()
            .verify();

        // Assert that we have the current offset.
        final EventPosition firstPosition = currentPosition.get().get();
        Assertions.assertNotNull(firstPosition);
        Assertions.assertEquals(secondOffset, firstPosition.getOffset());
        Assertions.assertFalse(firstPosition.isInclusive());

        StepVerifier.create(consumer.receive())
            .then(() -> messageProcessorSink.next(message3))
            .assertNext(partitionEvent -> {
                verifyPartitionContext(partitionEvent.getPartitionContext());
                verifyLastEnqueuedInformation(false, null, partitionEvent.getLastEnqueuedEventProperties());
                Assertions.assertSame(event3, partitionEvent.getData());
            })
            .then(() -> consumer.close())
            .expectComplete()
            .verify();

        // We terminated the processor. This should be terminated as well.
        Assertions.assertTrue(linkProcessor.isTerminated());

        // Assert that we have that last position.
        final EventPosition actual = currentPosition.get().get();
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(lastOffset, actual.getOffset());
        Assertions.assertFalse(actual.isInclusive());
    }


    /**
     * Verifies that the consumer closes and completes any listeners on a shutdown signal.
     */
    @Test
    void listensToShutdownSignals() throws InterruptedException {
        // Arrange
        linkProcessor = createSink(link1, link2).subscribeWith(new AmqpReceiveLinkProcessor(PREFETCH, retryPolicy, parentConnection));
        consumer = new EventHubPartitionAsyncConsumer(linkProcessor, messageSerializer, HOSTNAME, EVENT_HUB_NAME,
            CONSUMER_GROUP, PARTITION_ID, currentPosition, false, Schedulers.parallel());

        final Message message3 = mock(Message.class);
        final String secondOffset = "54";
        final String lastOffset = "65";
        final EventData event1 = new EventData(BinaryData.fromBytes("Foo".getBytes()), getSystemProperties("25", 14),
            Context.NONE);
        final EventData event2 = new EventData(BinaryData.fromBytes("Bar".getBytes()), getSystemProperties(secondOffset,
            21), Context.NONE);
        final EventData event3 = new EventData(BinaryData.fromBytes("Baz".getBytes()), getSystemProperties(lastOffset,
            53), Context.NONE);

        when(messageSerializer.deserialize(same(message1), eq(EventData.class))).thenReturn(event1);
        when(messageSerializer.deserialize(same(message2), eq(EventData.class))).thenReturn(event2);
        when(messageSerializer.deserialize(same(message3), eq(EventData.class))).thenReturn(event3);

        final CountDownLatch shutdownReceived = new CountDownLatch(3);

        final Disposable.Composite subscriptions = Disposables.composite(
            consumer.receive()
                .subscribe(
                    event -> logger.info("1. Received: {}", event.getData().getSequenceNumber()),
                    error -> Assertions.fail(error.toString()),
                    () -> {
                        logger.info("1. Shutdown received");
                        shutdownReceived.countDown();
                    }),

            consumer.receive()
                .subscribe(
                    event -> logger.info("2. Received: {}", event.getData().getSequenceNumber()),
                    error -> Assertions.fail(error.toString()),
                    () -> {
                        logger.info("2. Shutdown received");
                        shutdownReceived.countDown();
                    }),
            consumer.receive()
                .subscribe(
                    event -> logger.info("3. Received: {}", event.getData().getSequenceNumber()),
                    error -> Assertions.fail(error.toString()),
                    () -> {
                        logger.info("3. Shutdown received");
                        shutdownReceived.countDown();
                    }));

        // Act
        messageProcessorSink.next(message1);
        messageProcessorSink.next(message2);
        messageProcessorSink.next(message3);

        linkProcessor.cancel();

        // Assert
        try {
            boolean successful = shutdownReceived.await(5, TimeUnit.SECONDS);
            Assertions.assertTrue(successful);
            Assertions.assertEquals(0, shutdownReceived.getCount());

            verify(link1).dispose();
        } finally {
            subscriptions.dispose();
        }
    }

    private void verifyLastEnqueuedInformation(boolean trackLastEnqueued,
        LastEnqueuedEventProperties expected, LastEnqueuedEventProperties actual) {

        if (!trackLastEnqueued) {
            Assertions.assertNull(actual);
            return;
        }

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getEnqueuedTime(), actual.getEnqueuedTime());
        Assertions.assertEquals(expected.getOffset(), actual.getOffset());
        Assertions.assertEquals(expected.getRetrievalTime(), actual.getRetrievalTime());
        Assertions.assertEquals(expected.getSequenceNumber(), actual.getSequenceNumber());
    }

    private void verifyPartitionContext(PartitionContext context) {
        Assertions.assertEquals(HOSTNAME, context.getFullyQualifiedNamespace());
        Assertions.assertEquals(PARTITION_ID, context.getPartitionId());
        Assertions.assertEquals(EVENT_HUB_NAME, context.getEventHubName());
        Assertions.assertEquals(CONSUMER_GROUP, context.getConsumerGroup());
    }

    private static Flux<AmqpReceiveLink> createSink(AmqpReceiveLink... links) {
        return Flux.create(emitter -> {
            final AtomicInteger counter = new AtomicInteger();

            emitter.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int index = counter.getAndIncrement();

                    if (index == links.length) {
                        emitter.error(new RuntimeException(String.format(
                            "Cannot emit more. Index: %s. # of Connections: %s",
                            index, links.length)));
                        break;
                    }

                    emitter.next(links[index]);
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    private static EventData.SystemProperties getSystemProperties(String offset, long sequenceNumber) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AmqpMessageConstant.OFFSET_ANNOTATION_NAME.getValue(), offset);
        properties.put(AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), sequenceNumber);
        properties.put(AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), TEST_DATE);

        return new EventData.SystemProperties(properties);
    }
}
