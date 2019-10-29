// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.eventhubs.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.eventhubs.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;

public class EventHubConsumerTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;

    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final EmitterProcessor<Message> messageProcessor = EmitterProcessor.create(100, false);
    private final FluxSink<Message> sink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Throwable> errorProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpShutdownSignal> shutdownProcessor = DirectProcessor.create();
    private final MessageSerializer serializer = new EventHubMessageSerializer();

    @Mock
    private AmqpReceiveLink amqpReceiveLink;

    private EventHubConsumer consumer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mono<AmqpReceiveLink> receiveLinkMono = Mono.fromCallable(() -> amqpReceiveLink);

        when(amqpReceiveLink.receive()).thenReturn(messageProcessor);
        when(amqpReceiveLink.getErrors()).thenReturn(errorProcessor);
        when(amqpReceiveLink.getConnectionStates()).thenReturn(endpointProcessor);
        when(amqpReceiveLink.getShutdownSignals()).thenReturn(shutdownProcessor);

        EventHubConsumerOptions options = new EventHubConsumerOptions()
            .setIdentifier("an-identifier")
            .setPrefetchCount(PREFETCH);
        EventHubAsyncConsumer asyncConsumer = new EventHubAsyncConsumer(receiveLinkMono, serializer, options);
        consumer = new EventHubConsumer(asyncConsumer, new RetryOptions().getTryTimeout());
    }

    @After
    public void teardown() throws IOException {
        Mockito.framework().clearInlineMocks();
        consumer.close();
    }

    /**
     * Verify that by default, lastEnqueuedInformation is null if {@link EventHubConsumerOptions#getTrackLastEnqueuedEventProperties()}
     * is not set.
     */
    @Test
    public void lastEnqueuedEventInformationIsNull() {
        // Assert
        Assert.assertNull(consumer.getLastEnqueuedEventProperties());
    }

    /**
     * Verify that the default information is set and is null because no information has been received.
     */
    @Test
    public void lastEnqueuedEventInformationCreated() {
        // Arrange
        final EventHubAsyncConsumer runtimeConsumer = new EventHubAsyncConsumer(
            Mono.just(amqpReceiveLink),
            serializer,
            new EventHubConsumerOptions().setTrackLastEnqueuedEventProperties(true));

        // Act
        final LastEnqueuedEventProperties lastEnqueuedEventProperties = runtimeConsumer.getLastEnqueuedEventProperties();

        // Assert
        Assert.assertNotNull(lastEnqueuedEventProperties);
        Assert.assertNull(lastEnqueuedEventProperties.getOffset());
        Assert.assertNull(lastEnqueuedEventProperties.getSequenceNumber());
        Assert.assertNull(lastEnqueuedEventProperties.getRetrievalTime());
        Assert.assertNull(lastEnqueuedEventProperties.getEnqueuedTime());
    }

    /**
     * Verifies that this receives a number of events.
     */
    @Test
    public void receivesNumberOfEvents() {
        // Arrange
        final int numberOfEvents = 10;
        sendMessages(numberOfEvents);
        final int numberToReceive = 3;

        // Act
        final IterableStream<EventData> receive = consumer.receive(numberToReceive);

        // Assert
        final Map<Integer, EventData> actual = receive.stream()
            .collect(Collectors.toMap(e -> {
                final String value = String.valueOf(e.getProperties().get(MESSAGE_POSITION_ID));
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
        final IterableStream<EventData> receive = consumer.receive(firstReceive);
        final IterableStream<EventData> receive2 = consumer.receive(secondReceive);

        // Assert
        final Map<Integer, EventData> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerTest::getPositionId, Function.identity()));
        final Map<Integer, EventData> secondActual = receive2.stream()
            .collect(Collectors.toMap(EventHubConsumerTest::getPositionId, Function.identity()));

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
        final IterableStream<EventData> receive = consumer.receive(firstReceive, timeout);

        // Assert
        final Map<Integer, EventData> firstActual = receive.stream()
            .collect(Collectors.toMap(EventHubConsumerTest::getPositionId, Function.identity()));

        Assert.assertEquals(numberOfEvents, firstActual.size());
        IntStream.range(0, numberOfEvents)
            .forEachOrdered(number -> Assert.assertTrue(firstActual.containsKey(number)));
    }

    private static Integer getPositionId(EventData event) {
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
