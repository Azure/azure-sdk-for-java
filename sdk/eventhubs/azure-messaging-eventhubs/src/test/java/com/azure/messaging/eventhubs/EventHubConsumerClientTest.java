// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;

import static com.azure.messaging.eventhubs.EventHubConsumerAsyncClientTest.MockConnection;
import static com.azure.messaging.eventhubs.EventHubConsumerAsyncClientTest.MockReceiveLink;

public class EventHubConsumerClientTest {
    private static final ClientLogger LOGGER = new ClientLogger(EventHubConsumerClientTest.class);

    private static final int PREFETCH = 5;
    private static final String HOSTNAME = "hostname-foo";
    private static final String EVENT_HUB_NAME = "event-hub-name";
    private static final String CONSUMER_GROUP = "consumer-group-test";
    private static final String PARTITION_ID = "partition-id";
    private static final String CLIENT_IDENTIFIER = "my-client-identifier";
    private static final String DEFAULT_NAMESPACE_SUFFIX = ".servicebus.windows.net";
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final EventHubsConsumerInstrumentation DEFAULT_INSTRUMENTATION
        = new EventHubsConsumerInstrumentation(null, null, HOSTNAME, EVENT_HUB_NAME, CONSUMER_GROUP, true);

    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final MessageSerializer messageSerializer = new EventHubMessageSerializer();

    @Mock
    private Runnable onClientClosed;

    private EventHubConsumerClient consumer;
    private AutoCloseable mockCloseable;

    @BeforeEach
    public void setup() {
        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        mockCloseable.close();
    }

    @AfterAll
    public static void dispose() {
        EXECUTOR_SERVICE.shutdown();
    }

    /**
     * Verify that by default, lastEnqueuedInformation is null if {@link ReceiveOptions#getTrackLastEnqueuedEventProperties()}
     * is not set.
     */
    @Test
    public void lastEnqueuedEventInformationIsNull() {
        EventHubConsumerClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final int numberToReceive = 3;
            final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(false);
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH, Duration.ofSeconds(5));

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();
            receiveLink.emitMessages(numberOfEvents);

            final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, numberToReceive,
                EventPosition.earliest(), TIMEOUT, options);
            Assertions.assertNotNull(receive);
            for (PartitionEvent event : receive) {
                Assertions.assertNull(event.getLastEnqueuedEventProperties());
            }
            verifyNoMoreInteractions(onClientClosed);
        } finally {
            close(consumer);
        }
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    public void lastEnqueuedEventInformationCreated() {
        EventHubConsumerClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final ReceiveOptions options = new ReceiveOptions().setTrackLastEnqueuedEventProperties(true);
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH, Duration.ofSeconds(5));

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();
            receiveLink.emitMessages(numberOfEvents);

            final IterableStream<PartitionEvent> receive = consumer.receiveFromPartition(PARTITION_ID, numberOfEvents,
                EventPosition.earliest(), TIMEOUT, options);

            Assertions.assertNotNull(receive);
            for (PartitionEvent event : receive) {
                final LastEnqueuedEventProperties properties = event.getLastEnqueuedEventProperties();
                Assertions.assertNotNull(properties);
                Assertions.assertNull(properties.getOffset());
                Assertions.assertNull(properties.getSequenceNumber());
                Assertions.assertNull(properties.getRetrievalTime());
                Assertions.assertNull(properties.getEnqueuedTime());
            }
        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that this receives a number of events.
     */
    @Test
    public void receivesNumberOfEvents() throws InterruptedException {
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 10;
            final int numberToReceive = 3;
            final AtomicReference<IterableStream<PartitionEvent>> received = new AtomicReference<>();
            final Semaphore semaphore = new Semaphore(1);
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> connectionCache = connection.wrapInCache();

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();
            receiveLink.emitMessages(numberOfEvents);
            try (EventHubConsumerClient consumer = createConsumer(connectionCache, PREFETCH, Duration.ofSeconds(5))) {
                semaphore.acquire();
                EXECUTOR_SERVICE.execute(() -> {
                    received
                        .set(consumer.receiveFromPartition(PARTITION_ID, numberToReceive, EventPosition.earliest()));
                    semaphore.release();
                });

                EXECUTOR_SERVICE.execute(() -> receiveLink.emitMessages(numberOfEvents));

                // Assert
                semaphore.acquire();
                final IterableStream<PartitionEvent> receive = received.get();
                Assertions.assertNotNull(receive);

                final Map<Integer, PartitionEvent> actual = receive.stream().collect(Collectors.toMap(e -> {
                    final String value = String.valueOf(e.getData().getProperties().get(MESSAGE_POSITION_ID));
                    return Integer.valueOf(value);
                }, Function.identity()));

                Assertions.assertEquals(numberToReceive, actual.size());

                IntStream.range(0, numberToReceive).forEachOrdered(number -> {
                    Assertions.assertTrue(actual.containsKey(number));
                });
            }
        }
    }

    @Test
    public void receivesMultipleTimes() {
        EventHubConsumerClient consumer = null;
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink0 = new MockReceiveLink(PARTITION_ID, messageTrackingUUID);
            MockReceiveLink receiveLink1 = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 15;
            final int firstReceive = 8;
            final int secondReceive = 4;
            receiveLink0.arrange(numberOfEvents);
            receiveLink1.arrange(numberOfEvents);
            connection.arrange(receiveLink0, receiveLink1);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH, Duration.ofSeconds(5));

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink0.setEndpointActive();
            receiveLink1.setEndpointActive();

            receiveLink0.emitMessages(numberOfEvents);
            final IterableStream<PartitionEvent> receive0
                = consumer.receiveFromPartition(PARTITION_ID, firstReceive, EventPosition.earliest());

            LOGGER.log(LogLevel.VERBOSE, () -> "First receive.");
            final Map<Integer, PartitionEvent> firstActual = receive0.stream()
                .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

            receiveLink1.emitMessages(numberOfEvents);
            final IterableStream<PartitionEvent> receive1
                = consumer.receiveFromPartition(PARTITION_ID, secondReceive, EventPosition.earliest());

            LOGGER.log(LogLevel.VERBOSE, () -> "Second receive.");
            final Map<Integer, PartitionEvent> secondActual = receive1.stream()
                .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

            receiveLink0.verifyReceiveCalled();
            receiveLink1.verifyReceiveCalled();

            Assertions.assertEquals(firstReceive, firstActual.size());
            Assertions.assertEquals(secondReceive, secondActual.size());

            IntStream.range(0, firstReceive)
                .forEachOrdered(number -> Assertions.assertTrue(firstActual.containsKey(number)));
            IntStream.range(0, secondReceive)
                .forEachOrdered(number -> Assertions.assertTrue(secondActual.containsKey(number)));

        } finally {
            close(consumer);
        }
    }

    /**
     * Verifies that this completes after 1 second and receives as many events as possible in that time.
     */
    @Test
    public void receivesReachesTimeout() {
        try (MockConnection connection = new MockConnection();
            MockReceiveLink receiveLink = new MockReceiveLink(PARTITION_ID, messageTrackingUUID)) {
            final int numberOfEvents = 3;
            final int firstReceive = 8;
            final Duration timeout = Duration.ofSeconds(1);
            receiveLink.arrange(numberOfEvents);
            connection.arrange(receiveLink);
            final ReactorConnectionCache<EventHubReactorAmqpConnection> connectionCache = connection.wrapInCache();
            consumer = createConsumer(connectionCache, PREFETCH, Duration.ofSeconds(5));

            // Act and assert
            //
            connection.setEndpointActive();
            receiveLink.setEndpointActive();
            receiveLink.emitMessages(numberOfEvents);

            final IterableStream<PartitionEvent> receive
                = consumer.receiveFromPartition(PARTITION_ID, firstReceive, EventPosition.earliest(), timeout);

            // Assert
            final Map<Integer, PartitionEvent> firstActual = receive.stream()
                .collect(Collectors.toMap(EventHubConsumerClientTest::getPositionId, Function.identity()));

            Assertions.assertEquals(numberOfEvents, firstActual.size());
            IntStream.range(0, numberOfEvents)
                .forEachOrdered(number -> Assertions.assertTrue(firstActual.containsKey(number)));

        } finally {
            close(consumer);
        }
    }

    @Test
    public void setsCorrectProperties() {
        final String endpointPrefix = "contoso";
        final String endpointSuffix
            = Configuration.getGlobalConfiguration().get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", DEFAULT_NAMESPACE_SUFFIX);
        final String entityPath = "orders-eh";
        final String con
            = String.format("Endpoint=sb://%s%s/;SharedAccessKeyName=nnn;SharedAccessKey=kkk;EntityPath=%s",
                endpointPrefix, endpointSuffix, entityPath);

        final EventHubConsumerClient consumer = new EventHubClientBuilder().connectionString(con)
            .consumerGroup(CONSUMER_GROUP)
            .prefetchCount(100)
            .buildConsumerClient();

        Assertions.assertEquals(entityPath, consumer.getEventHubName());
        Assertions.assertEquals(String.format("%s%s", endpointPrefix, endpointSuffix),
            consumer.getFullyQualifiedNamespace());
        Assertions.assertEquals(CONSUMER_GROUP, consumer.getConsumerGroup());
    }

    private static Integer getPositionId(PartitionEvent partitionEvent) {
        EventData event = partitionEvent.getData();
        final String value = String.valueOf(event.getProperties().get(MESSAGE_POSITION_ID));
        return Integer.valueOf(value);
    }

    private EventHubConsumerClient createConsumer(ReactorConnectionCache<EventHubReactorAmqpConnection> connectionCache,
        int prefetch, Duration tryTimeout) {
        final EventHubConsumerAsyncClient asyncConsumer
            = new EventHubConsumerAsyncClient(HOSTNAME, EVENT_HUB_NAME, connectionCache, messageSerializer,
                CONSUMER_GROUP, prefetch, true, onClientClosed, CLIENT_IDENTIFIER, DEFAULT_INSTRUMENTATION);
        return new EventHubConsumerClient(asyncConsumer, tryTimeout);
    }

    private static void close(EventHubConsumerClient consumer) {
        if (consumer != null) {
            consumer.close();
        }
    }
}
