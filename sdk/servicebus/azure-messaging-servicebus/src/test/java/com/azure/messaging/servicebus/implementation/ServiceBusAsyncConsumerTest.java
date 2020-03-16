// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusAsyncConsumerTest {
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageProcessorSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointProcessorSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    private ServiceBusReceiveLinkProcessor linkProcessor;

    @Mock
    private AmqpReceiveLink link;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private Disposable parentConnection;
    @Mock
    private MessageSerializer serializer;


    @BeforeAll
    static void beforeAll() {
//        StepVerifier.setDefaultTimeout(Duration.ofSeconds(20));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        linkProcessor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link))
            .subscribeWith(new ServiceBusReceiveLinkProcessor(10, retryPolicy, parentConnection));

        when(link.getEndpointStates()).thenReturn(endpointProcessor);
        when(link.receive()).thenReturn(messageProcessor);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that we can receive messages from the processor.
     */
    @Test
    void canReceive() {
        // Arrange
        final AtomicInteger counter = new AtomicInteger();
        final Function<ServiceBusReceivedMessage, Mono<Void>> onComplete = (message) -> {
            return Mono.fromRunnable(() -> counter.incrementAndGet());
        };

        final boolean isAutoComplete = true;
        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(linkProcessor, serializer, isAutoComplete, onComplete);
        final Message message1 = mock(Message.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);

        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);
        when(serializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        // Act and Assert
        StepVerifier.create(consumer.receive().take(2))
            .then(() -> {
                endpointProcessorSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
                messageProcessorSink.next(message2);
            })
            .expectNext(receivedMessage1, receivedMessage2)
            .verifyComplete();

        assertEquals(2, counter.get());
    }

    /**
     * Verifies that if we dispose the consumer, it also completes.
     */
    @Test
    void canDispose() {
        // Arrange
        final boolean isAutoComplete = false;
        final Function<ServiceBusReceivedMessage, Mono<Void>> onComplete = (message) -> {
            Assertions.fail("Should not complete");
            return Mono.empty();
        };

        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(linkProcessor, serializer, isAutoComplete, onComplete);
        final Message message1 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);

        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);

        // Act and Assert
        StepVerifier.create(consumer.receive())
            .then(() -> {
                endpointProcessorSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
            })
            .expectNext(receivedMessage1)
            .then(() -> consumer.close())
            .verifyComplete();
    }
}
