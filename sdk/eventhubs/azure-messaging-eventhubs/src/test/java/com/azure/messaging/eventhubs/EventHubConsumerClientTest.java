// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubSession;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

import static com.azure.messaging.eventhubs.EventHubConsumerAsyncClientTest.PARTITION_ID_HEADER;
import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();

    @Mock
    private AmqpReceiveLink amqpReceiveLink;
    @Mock
    private AmqpReceiveLink amqpReceiveLink2;

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

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(amqpReceiveLink.receive()).thenReturn(messageProcessor);
        when(amqpReceiveLink.getEndpointStates()).thenReturn(Flux.never());
        when(amqpReceiveLink.getCredits()).thenReturn(10);

        connectionOptions = new ConnectionOptions(HOSTNAME, "event-hub-path", tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(),
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());
        linkProvider = new EventHubConnection(Mono.just(connection), connectionOptions);
        when(connection.createSession(argThat(name -> name.endsWith(PARTITION_ID))))
            .thenReturn(Mono.fromCallable(() -> session));
        when(session.createConsumer(any(), argThat(name -> name.endsWith(PARTITION_ID)), any(), any(), any(), any()))
            .thenReturn(Mono.just(amqpReceiveLink), Mono.fromCallable(() -> {
                System.out.println("Returning second link");
                return amqpReceiveLink2;
            }));

        asyncConsumer = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME,
            linkProvider, messageSerializer, CONSUMER_GROUP, PREFETCH, false);
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
     * Verify that by default, lastEnqueuedInformation is null if
     * {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()} is not set.
     */
    @Test
    public void lastEnqueuedEventInformationIsNull() {
        // Arrange
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(
            HOSTNAME, EVENT_HUB_NAME, linkProvider, messageSerializer, CONSUMER_GROUP,
            PREFETCH, false);
        final EventHubConsumerClient consumer = new EventHubConsumerClient(runtimeConsumer, Duration.ofSeconds(5));
        final int numberOfEvents = 10;
        sendMessages(sink, numberOfEvents, PARTITION_ID);
        final int numberToReceive = 3;
        final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(false);

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, numberToReceive,
            EventPosition.earliest(), TIMEOUT, options);

        // Assert
        Assertions.assertNotNull(receive);

        for (PartitionEvent event : receive) {
            Assertions.assertNull(event.getLastEnqueuedEventProperties());
        }
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    public void lastEnqueuedEventInformationCreated() {
        // Arrange
        final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);
        final EventHubConsumerAsyncClient runtimeConsumer = new EventHubConsumerAsyncClient(
            HOSTNAME, EVENT_HUB_NAME, linkProvider, messageSerializer, CONSUMER_GROUP, PREFETCH, false);
        final EventHubConsumerClient consumer = new EventHubConsumerClient(runtimeConsumer, Duration.ofSeconds(5));

        final int numberOfEvents = 10;
        sendMessages(sink, numberOfEvents, PARTITION_ID);
        final int numberToReceive = 3;

        // Act
        final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents,
            EventPosition.earliest(), TIMEOUT, options);

        // Assert
        Assertions.assertNotNull(receive);

        for (PartitionEvent event : receive) {
            final LastEnqueuedEventProperties properties = event.getLastEnqueuedEventProperties();
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
            received.set(consumer.receiveFromPartition(PARTITION_ID, numberToReceive, EventPosition.earliest()));
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

        EmitterProcessor<Message> processor = EmitterProcessor.create(100, false);
        FluxSink<Message> sink2 = processor.sink(FluxSink.OverflowStrategy.BUFFER);
        when(amqpReceiveLink2.receive()).thenReturn(processor);
        when(amqpReceiveLink2.getEndpointStates()).thenReturn(Flux.never());
        when(amqpReceiveLink2.getCredits()).thenReturn(10);

        // Act
        sendMessages(sink, numberOfEvents, PARTITION_ID);
        final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, firstReceive, EventPosition.earliest());

        sendMessages(sink2, numberOfEvents, PARTITION_ID);
        final IterableStream<PartitionEvent> receive2 = consumer.receiveFromPartition(PARTITION_ID, secondReceive, EventPosition.earliest());

        // Assert
        System.out.println("First receive.");
        final Map<Integer, PartitionEvent> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        System.out.println("Second receive.");
        final Map<Integer, PartitionEvent> secondActual = receive2.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        verify(amqpReceiveLink, times(1)).receive();
        verify(amqpReceiveLink2, times(1)).receive();

        Assertions.assertEquals(firstReceive, firstActual.size());
        Assertions.assertEquals(secondReceive, secondActual.size());

        IntStream.range(0, firstReceive).forEachOrdered(number -> Assertions.assertTrue(firstActual.containsKey(number)));
        IntStream.range(0, secondReceive).forEachOrdered(number -> Assertions.assertTrue(secondActual.containsKey(number)));
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
        final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, firstReceive, EventPosition.earliest(), timeout);

        // Assert
        final Map<Integer, PartitionEvent> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

        Assertions.assertEquals(numberOfEvents, firstActual.size());
        IntStream.range(0, numberOfEvents)
            .forEachOrdered(number -> Assertions.assertTrue(firstActual.containsKey(number)));
    }

    @Test
    public void setsCorrectProperties() {
        // Act
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .connectionString("Endpoint=sb://doesnotexist.servicebus.windows.net/;SharedAccessKeyName=doesnotexist;SharedAccessKey=dGhpcyBpcyBub3QgYSB2YWxpZCBrZXkgLi4uLi4uLi4=;EntityPath=dummy-event-hub")
            .consumerGroup(CONSUMER_GROUP)
            .prefetchCount(100)
            .buildConsumerClient();

        Assertions.assertEquals("dummy-event-hub", consumer.getEventHubName());
        Assertions.assertEquals("doesnotexist.servicebus.windows.net", consumer.getFullyQualifiedNamespace());
        Assertions.assertEquals(CONSUMER_GROUP, consumer.getConsumerGroup());
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
            final Message message = getMessage(PAYLOAD_BYTES, messageTrackingUUID, set);
            sink.next(message);
        }
    }
}
