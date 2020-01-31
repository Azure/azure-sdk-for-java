// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
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

import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
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

    private ReceiveLinkHandler receiverHandler;
    private ReactorReceiver reactorReceiver;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(cbsNode.authorize(any(), any())).thenReturn(Mono.empty());

        when(event.getLink()).thenReturn(receiver);
        when(receiver.getRemoteSource()).thenReturn(new Source());

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.attachments()).thenReturn(record);

        final String entityPath = "test-entity-path";
        receiverHandler = new ReceiveLinkHandler("test-connection-id", "test-host",
            "test-receiver-name", entityPath);
        final ActiveClientTokenManager tokenManager = new ActiveClientTokenManager(Mono.just(cbsNode),
            "test-tokenAudience", "test-scopes");
        final ReactorDispatcher dispatcher = new ReactorDispatcher(reactor);

        reactorReceiver = new ReactorReceiver(entityPath, receiver, receiverHandler, tokenManager, dispatcher);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();

        receiver = null;
        cbsNode = null;
        event = null;
    }

    /**
     * Verify we can add credits to the link.
     */
    @Test
    void addCredits() {
        final int credits = 15;
        reactorReceiver.addCredits(credits);

        verify(receiver, times(1)).flow(credits);
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
            .then(() -> reactorReceiver.dispose())
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
}
