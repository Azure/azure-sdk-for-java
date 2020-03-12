// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceBusAsyncConsumerTest {
    private static final String ENTITY_PATH = "test-entity-path";
    private static final String NAMESPACE = "test-fqdn";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.SUBSCRIPTION;


    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageProcessorSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointProcessorSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    private ServiceBusReceiveLinkProcessor linkProcessor;

    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private ServiceBusManagementNode managementNode;
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

        connectionProcessor = Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection)).subscribeWith(
            new ServiceBusConnectionProcessor(NAMESPACE, ENTITY_PATH, new AmqpRetryOptions()));

        when(connection.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));

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
        final boolean isAutoComplete = true;
        final String transferEntityPath = "some-transfer-path";
        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(linkProcessor, connectionProcessor,
            serializer, isAutoComplete, transferEntityPath, ENTITY_TYPE);
        final Message message1 = mock(Message.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);
        final UUID lockToken1 = UUID.randomUUID();
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);
        final UUID lockToken2 = UUID.randomUUID();

        when(receivedMessage1.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);

        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);
        when(serializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(connection.getManagementNode(ENTITY_PATH, transferEntityPath, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.complete(lockToken1)).thenReturn(Mono.empty());
        when(managementNode.complete(lockToken2)).thenReturn(Mono.empty());

        // Act and Assert
        StepVerifier.create(consumer.receive().take(2))
            .then(() -> {
                endpointProcessorSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
                messageProcessorSink.next(message2);
            })
            .expectNext(receivedMessage1, receivedMessage2)
            .verifyComplete();

        verify(managementNode, times(2)).complete(any());
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
        final String transferEntityPath = "some-transfer-path";
        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(linkProcessor, connectionProcessor,
            serializer, isAutoComplete, transferEntityPath, ENTITY_TYPE);

        final Message message1 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);

        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);

        when(connection.getManagementNode(ENTITY_PATH, transferEntityPath, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

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

    /**
     * Verifies that we error if we try to complete a message without a lock token.
     */
    @Test
    void completeWithoutLockToken() {
        // Arrange
        final boolean isAutoComplete = false;
        final String transferEntityPath = "some-transfer-path";
        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(linkProcessor, connectionProcessor,
            serializer, isAutoComplete, transferEntityPath, ENTITY_TYPE);

        when(connection.getManagementNode(ENTITY_PATH, transferEntityPath, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);

        when(connection.getManagementNode(ENTITY_PATH, transferEntityPath, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.complete(any())).thenReturn(Mono.empty());

        StepVerifier.create(consumer.complete(receivedMessage1))
            .expectError(AmqpException.class)
            .verify();

        verify(managementNode, times(0)).complete(any());
    }

    @Test
    void completeWithLockToken() {
        // Arrange
        final UUID token = UUID.randomUUID();
        final boolean isAutoComplete = false;
        final String transferEntityPath = "some-transfer-path";
        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(linkProcessor, connectionProcessor,
            serializer, isAutoComplete, transferEntityPath, ENTITY_TYPE);

        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage1.getLockToken()).thenReturn(token);

        when(connection.getManagementNode(ENTITY_PATH, transferEntityPath, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.complete(token)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(consumer.complete(receivedMessage1))
            .expectComplete()
            .verify();
    }
}
