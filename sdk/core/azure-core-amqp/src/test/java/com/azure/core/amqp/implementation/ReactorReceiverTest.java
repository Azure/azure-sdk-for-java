// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private Supplier<Integer> creditSupplier;
    @Mock
    private AmqpConnection amqpConnection;
    @Mock
    private TokenManager tokenManager;

    private final TestPublisher<AmqpShutdownSignal> shutdownSignals = TestPublisher.create();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
    private final TestPublisher<AmqpResponseCode> authorizationResults = TestPublisher.createCold();

    private ReceiveLinkHandler receiverHandler;
    private ReactorReceiver reactorReceiver;
    private AutoCloseable mocksCloseable;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
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

        when(tokenManager.getAuthorizationResults()).thenReturn(authorizationResults.flux());

        when(amqpConnection.getShutdownSignals()).thenReturn(shutdownSignals.flux());

        reactorReceiver = new ReactorReceiver(amqpConnection, entityPath, receiver, receiverHandler, tokenManager,
            reactorDispatcher, retryOptions);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Verify we can add and get credits to and from the link.
     */
    @Test
    void addCredits() throws IOException {
        final int credits = 15;
        final int currentCredits = 13;

        when(receiver.getRemoteCredit()).thenReturn(currentCredits);

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        StepVerifier.create(reactorReceiver.addCredits(credits))
            .verifyComplete();

        // Assert
        verify(receiver).flow(credits);

        assertEquals(currentCredits, reactorReceiver.getCredits());
    }

    /**
     * Verify the sink errors if we cannot schedule work.
     */
    @Test
    void addCreditsErrors() throws IOException {
        final int credits = 15;

        doAnswer(invocation -> {
            throw new IOException("Fake exception");
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        StepVerifier.create(reactorReceiver.addCredits(credits))
            .expectError(RuntimeException.class)
            .verify();

        // Assert
        verifyNoInteractions(receiver);
    }

    /**
     * Verifies EndpointStates are propagated.
     */
    @Test
    void updateEndpointState() {
        final Event closeEvent = mock(Event.class);
        final Receiver closeReceiver = mock(Receiver.class);
        when(closeEvent.getLink()).thenReturn(closeReceiver);
        when(closeEvent.getReceiver()).thenReturn(closeReceiver);

        when(closeReceiver.getLocalState()).thenReturn(EndpointState.ACTIVE);
        when(closeReceiver.getRemoteCondition()).thenReturn(null);

        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> receiverHandler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .then(() -> receiverHandler.onLinkRemoteClose(closeEvent))
            .verifyComplete();
    }


    /**
     * Verifies EndpointStates are propagated.
     */
    @Test
    void updateEndpointStateWithError() {
        final Event closeEvent = mock(Event.class);
        final Receiver closeReceiver = mock(Receiver.class);
        final AmqpErrorCondition condition = AmqpErrorCondition.CONNECTION_FORCED;
        final ErrorCondition errorCondition = new ErrorCondition(
            Symbol.valueOf(condition.getErrorCondition()), "Forced error condition");
        when(closeEvent.getLink()).thenReturn(closeReceiver);
        when(closeEvent.getReceiver()).thenReturn(closeReceiver);

        when(closeReceiver.getLocalState()).thenReturn(EndpointState.ACTIVE);
        when(closeReceiver.getRemoteCondition()).thenReturn(errorCondition);

        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> receiverHandler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .then(() -> receiverHandler.onLinkRemoteClose(closeEvent))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(condition, ((AmqpException) error).getErrorCondition());
            })
            .verify();

        verify(closeReceiver).close();
        verify(closeReceiver).setCondition(errorCondition);
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

        final int creditsToAdd = 10;

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        when(creditSupplier.get()).thenReturn(creditsToAdd);
        reactorReceiver.setEmptyCreditListener(creditSupplier);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act & Assert
        StepVerifier.create(reactorReceiver.receive())
            .then(() -> receiverHandler.onDelivery(event))
            .assertNext(message -> {
                Assertions.assertNotNull(message.getMessageAnnotations());

                final Map<Symbol, Object> values = message.getMessageAnnotations().getValue();
                assertTrue(values.containsKey(Symbol.getSymbol(AmqpMessageConstant.OFFSET_ANNOTATION_NAME.getValue())));
                assertTrue(values.containsKey(Symbol.getSymbol(AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME.getValue())));
                assertTrue(values.containsKey(Symbol.getSymbol(AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue())));
            })
            .thenCancel()
            .verify();

        verify(creditSupplier).get();

        verify(receiver).flow(creditsToAdd);
    }

    /**
     * Verifies that when an exception occurs in the parent, the connection is also closed.
     */
    @Test
    void parentDisposesConnection() throws IOException {
        // Arrange
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false,
            "Test-shutdown-signal");
        final Event event = mock(Event.class);
        final Link link = mock(Link.class);

        when(link.getLocalState()).thenReturn(EndpointState.ACTIVE);

        when(event.getLink()).thenReturn(link);

        doAnswer(invocationOnMock -> {
            receiverHandler.onLinkRemoteClose(event);
            return null;
        }).when(receiver).close();

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        shutdownSignals.next(shutdownSignal);

        // We are in the process of disposing.
        assertTrue(reactorReceiver.isDisposed());

        // This turns it into a synchronous operation so we know that it is disposed completely.
        reactorReceiver.dispose();

        // Assert
        verify(receiver).close();
    }

    /**
     * Verifies that when an exception occurs in the parent, the endpoints are also disposed.
     */
    @Test
    void parentClosesEndpoint() throws IOException {
        // Arrange
        final AmqpShutdownSignal shutdownSignal = new AmqpShutdownSignal(false, false, "Test-shutdown-signal");
        final Event event = mock(Event.class);
        final Link link = mock(Link.class);

        when(link.getLocalState()).thenReturn(EndpointState.ACTIVE);

        when(event.getLink()).thenReturn(link);

        doAnswer(invocationOnMock -> {
            receiverHandler.onLinkRemoteClose(event);
            return null;
        }).when(receiver).close();

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> shutdownSignals.next(shutdownSignal))
            .expectNext(AmqpEndpointState.CLOSED)
            .expectComplete()
            .verify();

        // Assert
        assertTrue(reactorReceiver.isDisposed());

        verify(receiver).close();
    }

    /**
     * An error in the handler will also close the sender.
     */
    @Test
    void disposesOnHandlerError() {
        // Arrange
        final AmqpErrorCondition amqpErrorCondition = AmqpErrorCondition.CONNECTION_FRAMING_ERROR;
        final Event event = mock(Event.class);
        final Link link = mock(Link.class);
        final ErrorCondition errorCondition = new ErrorCondition(
            Symbol.getSymbol(amqpErrorCondition.getErrorCondition()), "Test error condition");

        when(link.getLocalState()).thenReturn(EndpointState.ACTIVE);
        when(link.getRemoteCondition()).thenReturn(errorCondition);

        when(event.getLink()).thenReturn(link);

        // Act and Assert
        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkRemoteClose(event))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertEquals(((AmqpException) error).getErrorCondition(), amqpErrorCondition);
            })
            .verify();

        assertTrue(reactorReceiver.isDisposed());
    }

    /**
     * A complete in the handler will also close the sender.
     */
    @Test
    void disposesOnHandlerComplete() {
        // Arrange
        final Event event = mock(Event.class);
        final Link link = mock(Link.class);

        when(link.getLocalState()).thenReturn(EndpointState.ACTIVE);
        when(event.getLink()).thenReturn(link);

        // Act and Assert
        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkFinal(event))
            .expectNext(AmqpEndpointState.CLOSED)
            .expectComplete()
            .verify();

        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.CLOSED)
            .verifyComplete();

        assertTrue(reactorReceiver.isDisposed());
    }

    /**
     * An error in scheduling the close work will close the receiver.
     */
    @Test
    void disposesOnErrorSchedulingCloseWork() throws IOException {
        // Arrange
        final AtomicBoolean wasClosed = new AtomicBoolean();
        doAnswer(invocationOnMock -> {
            if (wasClosed.get()) {
                throw new RejectedExecutionException("Test-resource-exception");
            } else {
                final Runnable runnable = invocationOnMock.getArgument(0);
                runnable.run();
                return null;
            }
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act and Assert
        StepVerifier.create(reactorReceiver.closeAsync().doOnSubscribe(subscribed -> wasClosed.set(true)))
            .expectComplete()
            .verify();
    }

    /**
     * Tests {@link ReactorReceiver#closeAsync(String, ErrorCondition)}.
     */
    @Test
    void disposeCompletes() throws IOException {
        // Arrange
        final String message = "some-message";
        final AmqpErrorCondition errorCondition = AmqpErrorCondition.UNAUTHORIZED_ACCESS;
        final ErrorCondition condition = new ErrorCondition(Symbol.getSymbol(errorCondition.getErrorCondition()),
            "Test-users");
        final Event event = mock(Event.class);

        when(receiver.getLocalState()).thenReturn(EndpointState.ACTIVE, EndpointState.CLOSED);

        when(event.getLink()).thenReturn(receiver);

        doAnswer(invocationOnMock -> {
            receiverHandler.onLinkRemoteClose(event);
            return null;
        }).when(receiver).close();

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // Act
        StepVerifier.create(reactorReceiver.closeAsync(message, condition))
            .expectComplete()
            .verify();

        // Expect the same outcome.
        StepVerifier.create(reactorReceiver.closeAsync("something", null))
            .expectComplete()
            .verify();

        StepVerifier.create(reactorReceiver.closeAsync())
            .expectComplete()
            .verify();

        // Assert
        assertTrue(reactorReceiver.isDisposed());

        verify(receiver).setCondition(condition);
        verify(receiver).close();

        shutdownSignals.assertNoSubscribers();
    }

    /**
     * Tests {@link ReactorReceiver#dispose()}.
     */
    @Test
    void disposeBlocking() throws IOException {
        // Arrange
        final Event event = mock(Event.class);
        final Link link = mock(Link.class);

        when(link.getLocalState()).thenReturn(EndpointState.ACTIVE);

        when(event.getLink()).thenReturn(link);

        doAnswer(invocationOnMock -> {
            final Runnable runnable = invocationOnMock.getArgument(0);
            runnable.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        doAnswer(invocationOnMock -> {
            receiverHandler.onLinkRemoteClose(event);
            return null;
        }).when(receiver).close();

        // Act
        reactorReceiver.dispose();

        // Assert
        StepVerifier.create(reactorReceiver.closeAsync())
            .expectComplete()
            .verify();

        assertTrue(reactorReceiver.isDisposed());

        verify(receiver).close();

        shutdownSignals.assertNoSubscribers();
    }

    @Test
    void closesWhenNoLongerAuthorized() throws IOException {
        // Arrange
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "not-allowed",
            new AmqpErrorContext("foo-bar"));

        when(receiver.getLocalState()).thenReturn(EndpointState.ACTIVE, EndpointState.CLOSED);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        doAnswer(invocationOnMock -> {
            receiverHandler.onLinkRemoteClose(event);
            return null;
        }).when(receiver).close();

        // Assert and Act
        StepVerifier.create(reactorReceiver.receive())
            .then(() -> authorizationResults.error(error))
            .verifyComplete();
    }

    @Test
    void closesWhenAuthorizationResultsComplete() throws IOException {
        // Arrange
        final Event event = mock(Event.class);
        final Link link = mock(Link.class);

        when(event.getLink()).thenReturn(link);
        when(link.getLocalState()).thenReturn(EndpointState.CLOSED);

        doAnswer(invocationOnMock -> {
            final Runnable work = invocationOnMock.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        doAnswer(invocationOnMock -> {
            receiverHandler.onLinkRemoteClose(event);
            return null;
        }).when(receiver).close();

        // Assert and Act
        StepVerifier.create(reactorReceiver.receive())
            .then(authorizationResults::complete)
            .verifyComplete();
    }
}
