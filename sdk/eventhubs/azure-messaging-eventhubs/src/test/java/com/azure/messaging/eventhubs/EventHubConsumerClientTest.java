// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.implementation.EventHubConnection;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.apache.qpid.proton.message.Message;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class EventHubConsumerClientTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String HOSTNAME = "hostname-foo";
    private static final String EVENT_HUB_NAME = "event-hub-name";
    private static final String CONSUMER_GROUP = "consumer-group-test";
    private static final String PARTITION_ID = "partition-id";

    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final EmitterProcessor<Message> messageProcessor = EmitterProcessor.create(100, false);
    private final FluxSink<Message> sink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Throwable> errorProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpShutdownSignal> shutdownProcessor = DirectProcessor.create();
    private final MessageSerializer serializer = new EventHubMessageSerializer();
    private final ExecutorService service = Executors.newFixedThreadPool(4);
    @Mock
    private AmqpReceiveLink amqpReceiveLink;
    @Mock
    private EventHubConnection connection;
    @Mock
    private EventHubSession session;

    private EventHubConsumerClient consumer;
    private EventHubLinkProvider linkProvider;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(amqpReceiveLink.receive()).thenReturn(messageProcessor);
        when(amqpReceiveLink.getErrors()).thenReturn(errorProcessor);
        when(amqpReceiveLink.getConnectionStates()).thenReturn(endpointProcessor);
        when(amqpReceiveLink.getShutdownSignals()).thenReturn(shutdownProcessor);

        linkProvider = new EventHubLinkProvider(Mono.just(connection), HOSTNAME, new RetryOptions());
        when(connection.createSession(any())).thenReturn(Mono.fromCallable(() -> session));
        when(session.createConsumer(any(), argThat(name -> name.endsWith(PARTITION_ID)), any(), any(), any(), any()))
            .thenReturn(Mono.fromCallable(() -> amqpReceiveLink));

        EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setIdentifier("an-identifier")
            .setPrefetchCount(PREFETCH);
        EventHubConsumerAsyncClient asyncConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            linkProvider, serializer, CONSUMER_GROUP, EventPosition.earliest(), options);
        consumer = new EventHubConsumerClient(asyncConsumer, Duration.ofSeconds(10));
    }

    @After
    public void teardown() throws IOException {
        Mockito.framework().clearInlineMocks();
        consumer.close();
        service.shutdown();
    }

    /**
     * Verify that by default, lastEnqueuedInformation is null if {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()}
     * is not set.
     */
    @Test
    public void lastEnqueuedEventInformationIsNull() {
        // Arrange
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(
            HOSTNAME, EVENT_HUB_NAME, linkProvider, serializer, CONSUMER_GROUP, EventPosition.earliest(),
            new EventHubConsumerOptions().setTrackLastEnqueuedEventProperties(false));
        final EventHubConsumerClient consumer = new EventHubConsumerClient(runtimeConsumer, Duration.ofSeconds(5));
        final int numberOfEvents = 10;
        sendMessages(numberOfEvents);
        final int numberToReceive = 3;

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, numberToReceive);

        // Assert
        Assert.assertNotNull(receive);

        for (PartitionEvent event : receive) {
            Assert.assertNull(event.getPartitionContext().getLastEnqueuedEventProperties());
        }
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    public void lastEnqueuedEventInformationCreated() {
        // Arrange
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(
            HOSTNAME, EVENT_HUB_NAME, linkProvider, serializer, CONSUMER_GROUP, EventPosition.earliest(),
            new EventHubConsumerOptions().setTrackLastEnqueuedEventProperties(true));
        final EventHubConsumerClient consumer = new EventHubConsumerClient(runtimeConsumer, Duration.ofSeconds(5));

        final int numberOfEvents = 10;
        sendMessages(numberOfEvents);
        final int numberToReceive = 3;

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, numberOfEvents);

        // Assert
        Assert.assertNotNull(receive);

        for (PartitionEvent event : receive) {
            final LastEnqueuedEventProperties properties = event.getPartitionContext().getLastEnqueuedEventProperties();
            Assert.assertNotNull(properties);
            Assert.assertNull(properties.getOffset());
            Assert.assertNull(properties.getSequenceNumber());
            Assert.assertNull(properties.getRetrievalTime());
            Assert.assertNull(properties.getEnqueuedTime());
        }
    }

    /**
     * Verifies that this receives a number of events.
     */
    @Test
    public void receivesNumberOfEvents() throws InterruptedException {
        // Arrange
        final int numberToReceive = 3;
        final AtomicReference<IterableStream<PartitionEvent>> received = new AtomicReference<>();
        final Semaphore semaphore = new Semaphore(1);

        // Act
        semaphore.acquire();
        service.execute(() -> {
            received.set(consumer.receive(PARTITION_ID, numberToReceive));
            semaphore.release();
        });

        service.execute(() -> {
            sendMessages(10);
        });

        // Assert
        semaphore.acquire();
        final IterableStream<PartitionEvent> receive = received.get();
        Assert.assertNotNull(receive);

        final Map<Integer, PartitionEvent> actual = receive.stream()
            .collect(Collectors.toMap(e -> {
                final String value = String.valueOf(e.getEventData().getProperties().get(MESSAGE_POSITION_ID));
                return Integer.valueOf(value);
            }, Function.identity()));

        Assert.assertEquals(numberToReceive, actual.size());

        IntStream.range(0, numberToReceive).forEachOrdered(number -> {
            Assert.assertTrue(actual.containsKey(number));
        });
    }

    /**
     * Verifies that this receives a number of events.
     */
    @Test
    public void receivesMultipleTimes() {
        // Arrange
        final int numberOfEvents = 15;
        final int firstReceive = 8;
        final int secondReceive = 4;

        sendMessages(numberOfEvents);

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, firstReceive);
        final IterableStream<PartitionEvent> receive2 = consumer.receive(PARTITION_ID, secondReceive);

        // Assert
        final Map<Integer, PartitionEvent> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));
        final Map<Integer, PartitionEvent> secondActual = receive2.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        Assert.assertEquals(firstReceive, firstActual.size());
        Assert.assertEquals(secondReceive, secondActual.size());

        int startingIndex = 0;
        int endIndex = firstReceive;
        IntStream.range(startingIndex, endIndex).forEachOrdered(number -> Assert.assertTrue(firstActual.containsKey(number)));

        startingIndex += firstReceive;
        endIndex += secondReceive;
        IntStream.range(startingIndex, endIndex).forEachOrdered(number -> Assert.assertTrue(secondActual.containsKey(number)));
    }

    /**
     * Verifies that this completes after 1 second and receives as many events as possible in that time.
     */
    @Test
    public void receivesReachesTimeout() {
        // Arrange
        final int numberOfEvents = 3;
        final int firstReceive = 8;
        final Duration timeout = Duration.ofSeconds(1);

        sendMessages(numberOfEvents);

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, firstReceive, timeout);

        // Assert
        final Map<Integer, PartitionEvent> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        Assert.assertEquals(numberOfEvents, firstActual.size());
        IntStream.range(0, numberOfEvents)
            .forEachOrdered(number -> Assert.assertTrue(firstActual.containsKey(number)));
    }

    private static Integer getPositionId(PartitionEvent partitionEvent) {
        EventData event = partitionEvent.getEventData();
        final String value = String.valueOf(event.getProperties().get(MESSAGE_POSITION_ID));
        return Integer.valueOf(value);
    }

    private void sendMessages(int numberOfEvents) {
        for (int i = 0; i < numberOfEvents; i++) {
            Map<String, String> set = new HashMap<>();
            set.put(MESSAGE_POSITION_ID, Integer.valueOf(i).toString());
            sink.next(getMessage(PAYLOAD_BYTES, messageTrackingUUID, set));
        }
    }
}
