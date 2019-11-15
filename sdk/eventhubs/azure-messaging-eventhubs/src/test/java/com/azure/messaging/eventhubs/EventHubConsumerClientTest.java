// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.CBSAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
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

import static com.azure.messaging.eventhubs.EventHubConsumerAsyncClientTest.PARTITION_ID_HEADER;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static com.azure.messaging.eventhubs.TestUtils.isMatchingEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventHubConsumerClientTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String HOSTNAME = "hostname-foo";
    private static final String EVENT_HUB_NAME = "event-hub-name";
    private static final String CONSUMER_GROUP = "consumer-group-test";
    private static final String PARTITION_ID = "partition-id";
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final EmitterProcessor<Message> messageProcessor = EmitterProcessor.create(100, false);
    private final FluxSink<Message> sink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Throwable> errorProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpShutdownSignal> shutdownProcessor = DirectProcessor.create();
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();

    @Mock
    private AmqpReceiveLink amqpReceiveLink;
    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private EventHubSession session;
    @Mock
    private TokenCredential tokenCredential;

    private EventHubConsumerClient consumer;
    private EventHubConnection linkProvider;
    private ConnectionOptions connectionOptions;
    private EventHubConsumerAsyncClient asyncConsumer;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(amqpReceiveLink.receive()).thenReturn(messageProcessor);
        when(amqpReceiveLink.getErrors()).thenReturn(errorProcessor);
        when(amqpReceiveLink.getConnectionStates()).thenReturn(endpointProcessor);
        when(amqpReceiveLink.getShutdownSignals()).thenReturn(shutdownProcessor);

        connectionOptions = new ConnectionOptions(HOSTNAME, "event-hub-path", tokenCredential,
            CBSAuthorizationType.SHARED_ACCESS_SIGNATURE, TransportType.AMQP_WEB_SOCKETS, new RetryOptions(),
            ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.parallel());
        linkProvider = new EventHubConnection(Mono.just(connection), connectionOptions);
        when(connection.createSession(argThat(name -> name.endsWith(PARTITION_ID))))
            .thenReturn(Mono.fromCallable(() -> session));
        when(session.createConsumer(any(), argThat(name -> name.endsWith(PARTITION_ID)), any(), any(), any(), any()))
            .thenReturn(Mono.fromCallable(() -> amqpReceiveLink));

        EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(PREFETCH);
        asyncConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            linkProvider, messageSerializer, CONSUMER_GROUP, EventPosition.earliest(), options, false);
        consumer = new EventHubConsumerClient(asyncConsumer, Duration.ofSeconds(10));
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        consumer.close();
    }

    @AfterAll
    public static void dispose() {
        EXECUTOR_SERVICE.shutdown();
    }

    /**
     * Verify that by default, lastEnqueuedInformation is null if {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()}
     * is not set.
     */
    @Test
    public void lastEnqueuedEventInformationIsNull() {
        // Arrange
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(
            HOSTNAME, EVENT_HUB_NAME, linkProvider, messageSerializer, CONSUMER_GROUP, EventPosition.earliest(),
            new EventHubConsumerOptions().setTrackLastEnqueuedEventProperties(false), false);
        final EventHubConsumerClient consumer = new EventHubConsumerClient(runtimeConsumer, Duration.ofSeconds(5));
        final int numberOfEvents = 10;
        sendMessages(sink, numberOfEvents, PARTITION_ID);
        final int numberToReceive = 3;

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, numberToReceive);

        // Assert
        Assertions.assertNotNull(receive);

        for (PartitionEvent event : receive) {
            Assertions.assertNull(event.getPartitionContext().getLastEnqueuedEventProperties());
        }
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    public void lastEnqueuedEventInformationCreated() {
        // Arrange
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(
            HOSTNAME, EVENT_HUB_NAME, linkProvider, messageSerializer, CONSUMER_GROUP, EventPosition.earliest(),
            new EventHubConsumerOptions().setTrackLastEnqueuedEventProperties(true), false);
        final EventHubConsumerClient consumer = new EventHubConsumerClient(runtimeConsumer, Duration.ofSeconds(5));

        final int numberOfEvents = 10;
        sendMessages(sink, numberOfEvents, PARTITION_ID);
        final int numberToReceive = 3;

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, numberOfEvents);

        // Assert
        Assertions.assertNotNull(receive);

        for (PartitionEvent event : receive) {
            final LastEnqueuedEventProperties properties = event.getPartitionContext().getLastEnqueuedEventProperties();
            Assertions.assertNotNull(properties);
            Assertions.assertNull(properties.getOffset());
            Assertions.assertNull(properties.getSequenceNumber());
            Assertions.assertNull(properties.getRetrievalTime());
            Assertions.assertNull(properties.getEnqueuedTime());
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
        EXECUTOR_SERVICE.execute(() -> {
            received.set(consumer.receive(PARTITION_ID, numberToReceive));
            semaphore.release();
        });

        EXECUTOR_SERVICE.execute(() -> sendMessages(sink, 10, PARTITION_ID));

        // Assert
        semaphore.acquire();
        final IterableStream<PartitionEvent> receive = received.get();
        Assertions.assertNotNull(receive);

        final Map<Integer, PartitionEvent> actual = receive.stream()
            .collect(Collectors.toMap(e -> {
                final String value = String.valueOf(e.getData().getProperties().get(MESSAGE_POSITION_ID));
                return Integer.valueOf(value);
            }, Function.identity()));

        Assertions.assertEquals(numberToReceive, actual.size());

        IntStream.range(0, numberToReceive).forEachOrdered(number -> {
            Assertions.assertTrue(actual.containsKey(number));
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

        sendMessages(sink, numberOfEvents, PARTITION_ID);

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, firstReceive);
        final IterableStream<PartitionEvent> receive2 = consumer.receive(PARTITION_ID, secondReceive);

        // Assert
        final Map<Integer, PartitionEvent> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));
        final Map<Integer, PartitionEvent> secondActual = receive2.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        Assertions.assertEquals(firstReceive, firstActual.size());
        Assertions.assertEquals(secondReceive, secondActual.size());

        int startingIndex = 0;
        int endIndex = firstReceive;
        IntStream.range(startingIndex, endIndex).forEachOrdered(number -> Assertions.assertTrue(firstActual.containsKey(number)));

        startingIndex += firstReceive;
        endIndex += secondReceive;
        IntStream.range(startingIndex, endIndex).forEachOrdered(number -> Assertions.assertTrue(secondActual.containsKey(number)));
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

        sendMessages(sink, numberOfEvents, PARTITION_ID);

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receive(PARTITION_ID, firstReceive, timeout);

        // Assert
        final Map<Integer, PartitionEvent> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        Assertions.assertEquals(numberOfEvents, firstActual.size());
        IntStream.range(0, numberOfEvents)
            .forEachOrdered(number -> Assertions.assertTrue(firstActual.containsKey(number)));
    }

    @Test
    public void setsCorrectProperties() {
        EventPosition position = EventPosition.fromOffset(105L);
        EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setOwnerLevel(100L)
            .setPrefetchCount(100);

        // Act
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .connectionString("Endpoint=sb://doesnotexist.servicebus.windows.net/;SharedAccessKeyName=doesnotexist;SharedAccessKey=dGhpcyBpcyBub3QgYSB2YWxpZCBrZXkgLi4uLi4uLi4=;EntityPath=dummy-event-hub")
            .startingPosition(position)
            .consumerGroup(CONSUMER_GROUP)
            .consumerOptions(options)
            .buildConsumer();

        Assertions.assertEquals("dummy-event-hub", consumer.getEventHubName());
        Assertions.assertEquals("doesnotexist.servicebus.windows.net", consumer.getFullyQualifiedNamespace());
        Assertions.assertEquals(CONSUMER_GROUP, consumer.getConsumerGroup());
        Assertions.assertSame(position, consumer.getStartingPosition());
    }

    @Test
    public void receivesMultiplePartitions() {
        int numberOfEvents = 10;
        when(amqpReceiveLink.getCredits()).thenReturn(numberOfEvents);

        EventHubAmqpConnection connection1 = mock(EventHubAmqpConnection.class);
        EventHubConnection eventHubConnection = new EventHubConnection(Mono.fromCallable(() -> connection1),
            connectionOptions);

        String id2 = "partition-2";
        String id3 = "partition-3";
        String[] partitions = new String[]{PARTITION_ID, id2, id3};
        EventHubManagementNode managementNode = mock(EventHubManagementNode.class);
        when(connection1.getManagementNode()).thenReturn(Mono.just(managementNode));
        when(managementNode.getEventHubProperties())
            .thenReturn(Mono.just(new EventHubProperties(EVENT_HUB_NAME, Instant.EPOCH, partitions)));

        EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setPrefetchCount(PREFETCH);

        EventHubConsumerAsyncClient asyncClient = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            eventHubConnection, messageSerializer, CONSUMER_GROUP, EventPosition.earliest(), options, false);

        EmitterProcessor<Message> processor2 = EmitterProcessor.create();
        FluxSink<Message> processor2sink = processor2.sink();
        AmqpReceiveLink link2 = mock(AmqpReceiveLink.class);
        EventHubSession session2 = mock(EventHubSession.class);

        EmitterProcessor<Message> processor3 = EmitterProcessor.create();
        FluxSink<Message> processor3sink = processor3.sink();
        AmqpReceiveLink link3 = mock(AmqpReceiveLink.class);
        EventHubSession session3 = mock(EventHubSession.class);

        when(link2.receive()).thenReturn(processor2);
        when(link2.getErrors()).thenReturn(Flux.never());
        when(link2.getConnectionStates()).thenReturn(Flux.just(AmqpEndpointState.ACTIVE));
        when(link2.getShutdownSignals()).thenReturn(Flux.never());
        when(link2.getCredits()).thenReturn(numberOfEvents);

        when(link3.receive()).thenReturn(processor3);
        when(link3.getErrors()).thenReturn(Flux.never());
        when(link3.getConnectionStates()).thenReturn(Flux.just(AmqpEndpointState.ACTIVE));
        when(link3.getShutdownSignals()).thenReturn(Flux.never());
        when(link3.getCredits()).thenReturn(numberOfEvents);

        when(connection1.createSession(any())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);

            if (name.endsWith(PARTITION_ID)) {
                return Mono.just(session);
            } else if (name.endsWith(id2)) {
                return Mono.just(session2);
            } else if (name.endsWith(id3)) {
                return Mono.just(session3);
            } else {
                return Mono.error(new IllegalArgumentException("Unknown session: " + name));
            }
        });
        when(session2.createConsumer(any(), argThat(name -> name.endsWith(id2)), any(), any(), any(), any()))
            .thenReturn(Mono.just(link2));

        when(session3.createConsumer(any(), argThat(name -> name.endsWith(id3)), any(), any(), any(), any()))
            .thenReturn(Mono.just(link3));

        // Act & Assert
        StepVerifier.create(asyncClient.receive().filter(e -> isMatchingEvent(e, messageTrackingUUID)))
            .then(() -> sendMessages(processor2sink, 2, id2))
            .assertNext(event -> assertPartition(id2, event))
            .assertNext(event -> assertPartition(id2, event))
            .then(() -> sendMessages(processor3sink, 1, id3))
            .assertNext(event -> assertPartition(id3, event))
            .then(() -> sendMessages(processor2sink, 1, id2))
            .assertNext(event -> assertPartition(id2, event))
            .thenCancel()
            .verify(TIMEOUT);
    }

    private static Integer getPositionId(PartitionEvent partitionEvent) {
        EventData event = partitionEvent.getData();
        final String value = String.valueOf(event.getProperties().get(MESSAGE_POSITION_ID));
        return Integer.valueOf(value);
    }

    private void assertPartition(String partitionId, PartitionEvent event) {
        System.out.println("Event received: " + event.getPartitionContext().getPartitionId());
        final Object value = event.getData().getProperties().get(PARTITION_ID_HEADER);
        Assertions.assertTrue(value instanceof String);
        Assertions.assertEquals(partitionId, value);

        Assertions.assertEquals(partitionId, event.getPartitionContext().getPartitionId());
        Assertions.assertEquals(EVENT_HUB_NAME, event.getPartitionContext().getEventHubName());
        Assertions.assertEquals(CONSUMER_GROUP, event.getPartitionContext().getConsumerGroup());
    }

    private void sendMessages(FluxSink<Message> sink, int numberOfEvents, String partitionId) {
        for (int i = 0; i < numberOfEvents; i++) {
            Map<String, String> set = new HashMap<>();
            set.put(MESSAGE_POSITION_ID, Integer.valueOf(i).toString());
            set.put(PARTITION_ID_HEADER, partitionId);
            sink.next(getMessage(PAYLOAD_BYTES, messageTrackingUUID, set));
        }
    }
}
