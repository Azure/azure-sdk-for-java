// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReactorReceiverTest {
    @Mock
    private Receiver receiver;
    @Mock
    private ClaimsBasedSecurityNode cbsNode;
    @Mock
    private Event event;
    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private Record record;
    @Mock
    private ReactorDispatcher dispatcher;
    @Mock
    private Supplier<Integer> creditSupplier;
    @Mock
    private TokenManager tokenManager;

    private final TestPublisher<AmqpResponseCode> testPublisher = TestPublisher.createCold();
    private ReceiveLinkHandler receiverHandler;
    private ReactorReceiver reactorReceiver;
    private AutoCloseable mocksCloseable;

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
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(cbsNode.authorize(any(), any())).thenReturn(Mono.empty());

        when(event.getLink()).thenReturn(receiver);
        when(receiver.getRemoteSource()).thenReturn(new Source());

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.attachments()).thenReturn(record);

        final String entityPath = "test-entity-path";
        receiverHandler = new ReceiveLinkHandler("test-connection-id", "test-host",
            "test-receiver-name", entityPath);

        when(tokenManager.getAuthorizationResults()).thenReturn(testPublisher.flux());

        reactorReceiver = new ReactorReceiver(entityPath, receiver, receiverHandler, tokenManager, dispatcher);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Verify we can add credits to the link.
     */
    @Test
    void addCredits() throws IOException {
        final int credits = 15;

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(dispatcher).invoke(any(Runnable.class));

        // Act
        reactorReceiver.addCredits(credits);

        // Assert
        verify(receiver).flow(credits);
    }

    /**
     * Verifies EndpointStates are propagated.
     */
    @Test
    void updateEndpointState() {
        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> receiverHandler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .verifyComplete();
    }

    /**
     * Verifies on a non-transient AmqpException, closes link.
     */
    @Test
    void closesOnNonRetriableError() {
        // Arrange
        final Link link = mock(Link.class);
        final Session session = mock(Session.class);
        final Symbol symbol = Symbol.getSymbol(
            AmqpErrorCondition.UNAUTHORIZED_ACCESS.getErrorCondition());
        final String description = "test-symbol-description";
        final ErrorCondition condition = new ErrorCondition(symbol, description);
        final ArgumentCaptor<ErrorCondition> captor = ArgumentCaptor.forClass(ErrorCondition.class);

        when(event.getLink()).thenReturn(link);
        when(session.getLocalState()).thenReturn(EndpointState.ACTIVE);
        when(link.getRemoteCondition()).thenReturn(condition);
        when(link.getSession()).thenReturn(session);
        when(link.getLocalState()).thenReturn(EndpointState.ACTIVE);

        // Act
        StepVerifier.create(reactorReceiver.receive())
            .then(() -> receiverHandler.onLinkRemoteClose(event))
            .verifyComplete();

        // Assert
        verify(link, times(1)).close();
        verify(session, never()).close();

        verify(link).setCondition(captor.capture());
        Assertions.assertSame(condition, captor.getValue());
    }

    @Test
    void closesOnNonAmqpException() {
        // Arrange
        final Link link = mock(Link.class);
        final Session session = mock(Session.class);
        final Symbol symbol = Symbol.getSymbol(
            AmqpErrorCondition.NOT_IMPLEMENTED.getErrorCondition());
        final String description = "test-symbol-not implemented";
        final ErrorCondition condition = new ErrorCondition(symbol, description);

        when(event.getLink()).thenReturn(link);
        when(link.getRemoteCondition()).thenReturn(condition);
        when(link.getSession()).thenReturn(session);

        // Act & Assert
        StepVerifier.create(reactorReceiver.receive())
            .then(() -> receiverHandler.onLinkRemoteClose(event))
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    @Test
    void addsMoreCreditsWhenPrefetchIsDone() throws IOException {
        // Arrange
        // This message was copied from one that was received.
        final byte[] messageBytes = new byte[] { 0, 83, 114, -63, 73, 6, -93, 21, 120, 45, 111, 112, 116, 45, 115, 101,
            113, 117, 101, 110, 99, 101, 45, 110, 117, 109, 98, 101, 114, 85, 0, -93, 12, 120, 45, 111, 112, 116, 45,
            111, 102, 102, 115, 101, 116, -95, 1, 48, -93, 19, 120, 45, 111, 112, 116, 45, 101, 110, 113, 117, 101, 117,
            101, 100, 45, 116, 105, 109, 101, -125, 0, 0, 1, 112, -54, 124, -41, 90, 0, 83, 117, -96, 12, 80, 111, 115,
            105, 116, 105, 111, 110, 53, 58, 32, 48};
        final Link link = mock(Link.class);
        final Delivery delivery = mock(Delivery.class);

        when(event.getLink()).thenReturn(link);
        when(event.getDelivery()).thenReturn(delivery);

        when(delivery.getLink()).thenReturn(receiver);
        when(delivery.isPartial()).thenReturn(false);
        when(delivery.isSettled()).thenReturn(false);
        when(delivery.pending()).thenReturn(messageBytes.length);

        when(receiver.getRemoteCredit()).thenReturn(0);
        when(receiver.recv(any(), eq(0), eq(messageBytes.length))).thenAnswer(invocation -> {
            final byte[] buffer = invocation.getArgument(0);
            System.arraycopy(messageBytes, 0, buffer, 0, messageBytes.length);
            return messageBytes.length;
        });

        when(creditSupplier.get()).thenReturn(10);
        reactorReceiver.setEmptyCreditListener(creditSupplier);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(dispatcher).invoke(any(Runnable.class));

        // Act & Assert
        StepVerifier.create(reactorReceiver.receive())
            .then(() -> receiverHandler.onDelivery(event))
            .assertNext(message -> {
                Assertions.assertNotNull(message.getMessageAnnotations());

                final Map<Symbol, Object> values = message.getMessageAnnotations().getValue();
                Assertions.assertTrue(values.containsKey(Symbol.getSymbol(AmqpMessageConstant.OFFSET_ANNOTATION_NAME.getValue())));
                Assertions.assertTrue(values.containsKey(Symbol.getSymbol(AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME.getValue())));
                Assertions.assertTrue(values.containsKey(Symbol.getSymbol(AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue())));
            })
            .thenCancel()
            .verify();

        verify(creditSupplier).get();

        // Verify that the get addCredits was called on that dispatcher.
        verify(receiver).flow(10);
    }

    @Test
    void closesWhenNoLongerAuthorized() throws IOException {
        // Arrange
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "not-allowed",
            new AmqpErrorContext("foo-bar"));

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(dispatcher).invoke(any(Runnable.class));

        // Assert and Act
        StepVerifier.create(reactorReceiver.receive())
            .then(() -> testPublisher.error(error))
            .verifyComplete();
    }

    @Test
    void closesWhenAuthorizationResultsComplete() throws IOException {
        // Arrange
        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(dispatcher).invoke(any(Runnable.class));

        // Assert and Act
        StepVerifier.create(reactorReceiver.receive())
            .then(testPublisher::complete)
            .verifyComplete();
    }
}
