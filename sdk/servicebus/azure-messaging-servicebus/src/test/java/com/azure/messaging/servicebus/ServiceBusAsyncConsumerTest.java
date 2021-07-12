// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private final TestPublisher<ServiceBusReceiveLink> linkPublisher = TestPublisher.create();
    private final Flux<ServiceBusReceiveLink> linkFlux = linkPublisher.flux();
    private final TestPublisher<Message> messagePublisher = TestPublisher.create();
    private final Flux<Message> messageFlux = messagePublisher.flux();
    private final TestPublisher<AmqpEndpointState> endpointPublisher = TestPublisher.create();
    private final Flux<AmqpEndpointState> endpointStateFlux = endpointPublisher.flux();

    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);

    private ServiceBusReceiveLinkProcessor linkProcessor;
    private Function<String, Mono<OffsetDateTime>> onRenewLock;

    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private ServiceBusReceiveLink link;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private MessageSerializer serializer;
    @Mock
    LockContainer<LockRenewalOperation> messageLockContainer;

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

        when(link.getEndpointStates()).thenReturn(endpointStateFlux);
        when(link.receive()).thenReturn(messageFlux);
        when(link.addCredits(anyInt())).thenReturn(Mono.empty());

        linkProcessor = linkFlux.subscribeWith(new ServiceBusReceiveLinkProcessor(10, retryPolicy));

        when(connection.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link.updateDisposition(anyString(), any(DeliveryState.class))).thenReturn(Mono.empty());
        onRenewLock = (lockToken) -> Mono.just(OffsetDateTime.now().plusSeconds(1));
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("[{}]: Tearing down.", testInfo.getDisplayName());

        Mockito.framework().clearInlineMocks();

        linkProcessor.dispose();
        linkPublisher.complete();
        endpointPublisher.complete();
        messagePublisher.complete();
    }

    /**
     * Verifies that we can receive messages from the processor and it does not auto complete them.
     */
    @Test
    void receiveNoAutoComplete() {
        // Arrange
        final int prefetch = 10;
        final Duration maxAutoLockRenewDuration = Duration.ofSeconds(0);
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(3);
        final ReceiverOptions receiverOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, prefetch,
            maxAutoLockRenewDuration, false, "sessionId", null);

        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(LINK_NAME, linkProcessor, serializer,
            receiverOptions);

        final Message message1 = mock(Message.class);
        final Message message2 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);
        final String lockToken1 = UUID.randomUUID().toString();
        final ServiceBusReceivedMessage receivedMessage2 = mock(ServiceBusReceivedMessage.class);
        final String lockToken2 = UUID.randomUUID().toString();

        when(receivedMessage1.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);
        when(receivedMessage1.getLockedUntil()).thenReturn(lockedUntil);
        when(receivedMessage2.getLockedUntil()).thenReturn(lockedUntil);

        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);
        when(serializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        // Act and Assert
        StepVerifier.create(consumer.receive())
            .then(() -> {
                linkPublisher.next(link);
                endpointPublisher.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1);
            })
            .expectNext(receivedMessage1)
            .then(() -> messagePublisher.next(message2))
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
        final int prefetch = 10;
        final Duration maxAutoLockRenewDuration = Duration.ofSeconds(40);
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(3);
        final String lockToken = UUID.randomUUID().toString();
        final ReceiverOptions receiverOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, prefetch,
            maxAutoLockRenewDuration, false, "sessionId", null);

        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(LINK_NAME, linkProcessor, serializer,
            receiverOptions);

        final Message message1 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);

        when(receivedMessage1.getLockToken()).thenReturn(lockToken);
        when(receivedMessage1.getLockedUntil()).thenReturn(lockedUntil);
        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);

        // Act and Assert
        StepVerifier.create(consumer.receive())
            .then(() -> {
                linkPublisher.next(link);
                endpointPublisher.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1);
            })
            .expectNext(receivedMessage1)
            .then(() -> {
                linkPublisher.complete();
                endpointPublisher.complete();
            })
            .verifyComplete();

        verify(link, never()).updateDisposition(anyString(), any(DeliveryState.class));
    }

    /**
     * Verifies that if publisher errors out, it also complete with error.
     */
    @Test
    void onError() {
        // Arrange
        final int prefetch = 10;
        final Duration maxAutoLockRenewDuration = Duration.ofSeconds(40);
        final OffsetDateTime lockedUntil = OffsetDateTime.now().plusSeconds(3);
        final String lockToken = UUID.randomUUID().toString();
        final ReceiverOptions receiverOptions = new ReceiverOptions(ServiceBusReceiveMode.RECEIVE_AND_DELETE, prefetch,
            maxAutoLockRenewDuration, false, "sessionId", null);

        final ServiceBusAsyncConsumer consumer = new ServiceBusAsyncConsumer(LINK_NAME, linkProcessor, serializer,
            receiverOptions);

        final Message message1 = mock(Message.class);
        final ServiceBusReceivedMessage receivedMessage1 = mock(ServiceBusReceivedMessage.class);

        /*
         * Beginning in Mockito 3.4.0+ the default value for duration changed from null to Duration.ZERO
         */
        when(retryPolicy.calculateRetryDelay(any(), anyInt())).thenReturn(null);
        when(receivedMessage1.getLockToken()).thenReturn(lockToken);
        when(receivedMessage1.getLockedUntil()).thenReturn(lockedUntil);
        when(serializer.deserialize(message1, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage1);

        // Act and Assert
        StepVerifier.create(consumer.receive())
            .then(() -> {
                linkPublisher.next(link);
                endpointPublisher.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1);
            })
            .expectNext(receivedMessage1)
            .then(() -> {
                linkPublisher.error(new Throwable("fake error"));
                endpointPublisher.complete();
            })
            .verifyError();

        verify(link, never()).updateDisposition(anyString(), any(DeliveryState.class));
    }
}
