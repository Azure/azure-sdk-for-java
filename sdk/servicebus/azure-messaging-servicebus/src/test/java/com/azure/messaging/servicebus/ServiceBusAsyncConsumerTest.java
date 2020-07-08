// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusAsyncConsumer}.
 */
class ServiceBusAsyncConsumerTest {
    private static final String LINK_NAME = "some-link";
    private final EmitterProcessor<Message> messageProcessor = EmitterProcessor.create();
    private final FluxSink<Message> messageProcessorSink = messageProcessor.sink();
    private final EmitterProcessor<AmqpEndpointState> endpointProcessor = EmitterProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointProcessorSink = endpointProcessor.sink();
    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);

    private ServiceBusReceiveLinkProcessor linkProcessor;

    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private ServiceBusReceiveLink link;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private Disposable parentConnection;
    @Mock
    private MessageSerializer serializer;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(20));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        logger.info("[{}]: Setting up.", testInfo.getDisplayName());

        MockitoAnnotations.initMocks(this);

        when(link.getEndpointStates()).thenReturn(endpointProcessor);
        when(link.receive()).thenReturn(messageProcessor);
        linkProcessor = Flux.<ServiceBusReceiveLink>create(sink -> sink.onRequest(requested -> {
            logger.info("Requested link: {}", requested);
            sink.next(link);
        })).subscribeWith(new ServiceBusReceiveLinkProcessor(10, retryPolicy, parentConnection,
            new AmqpErrorContext("a-namespace")));

        when(connection.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link.updateDisposition(anyString(), any(DeliveryState.class))).thenReturn(Mono.empty());
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}]: Tearing down.", testInfo.getDisplayName());

        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that we can receive messages from the processor and it does not auto complete them.
     */
    @Test
    void receiveNoAutoComplete() {
        // Arrange
        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(LINK_NAME, linkProcessor, serializer);

        final Message message1 = mock(Message.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);
        final String lockToken1 = UUID.randomUUID().toString();
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);
        final String lockToken2 = UUID.randomUUID().toString();

        when(receivedMessage1.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);

        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);
        when(serializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        // Act and Assert
        StepVerifier.create(consumer.receive())
            .then(() -> messageProcessorSink.next(message1))
            .expectNext(receivedMessage1)
            .then(() -> messageProcessorSink.next(message2))
            .expectNext(receivedMessage2)
            .thenCancel()
            .verify();

        verify(link, never()).updateDisposition(anyString(), any(DeliveryState.class));
    }

    /**
     * Verifies that if we dispose the consumer, it also completes.
     */
    @Test
    void canDispose() {
        // Arrange
        final String lockToken = UUID.randomUUID().toString();
        when(linkProcessor.updateDisposition(lockToken, Accepted.getInstance()))
            .thenReturn(Mono.error(new IllegalArgumentException("Should not have called complete.")));

        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(LINK_NAME, linkProcessor, serializer);

        final Message message1 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);

        when(receivedMessage1.getLockToken()).thenReturn(lockToken);
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

        verify(link, never()).updateDisposition(anyString(), any(DeliveryState.class));
    }
}
