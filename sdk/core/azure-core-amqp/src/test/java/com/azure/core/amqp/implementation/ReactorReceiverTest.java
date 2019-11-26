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
import org.apache.qpid.proton.engine.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorReceiverTest {
    @Mock
    private Receiver receiver;
    @Mock
    private ClaimsBasedSecurityNode cbsNode;
    @Mock
    private Event event;

    private ReceiveLinkHandler receiverHandler;
    private ActiveClientTokenManager tokenManager;
    private ReactorReceiver reactorReceiver;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(cbsNode.authorize(any(), any())).thenReturn(Mono.empty());

        when(event.getLink()).thenReturn(receiver);
        when(receiver.getRemoteSource()).thenReturn(new Source());

        final String entityPath = "test-entity-path";
        receiverHandler = new ReceiveLinkHandler("test-connection-id", "test-host",
            "test-receiver-name", entityPath);
        tokenManager = new ActiveClientTokenManager(Mono.just(cbsNode), "test-tokenAudience", "test-scopes");
        reactorReceiver = new ReactorReceiver(entityPath, receiver, receiverHandler, tokenManager);
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();

        receiver = null;
        cbsNode = null;
        event = null;
    }

    /**
     * Verify we can add credits to the link.
     */
    @Test
    public void addCredits() {
        final int credits = 15;
        reactorReceiver.addCredits(credits);

        verify(receiver, times(1)).flow(credits);
    }

    /**
     * Verifies EndpointStates are propagated.
     */
    @Test
    public void updateEndpointState() {
        StepVerifier.create(reactorReceiver.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> receiverHandler.onLinkRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> receiverHandler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .then(() -> reactorReceiver.close())
            .verifyComplete();
    }

    /**
     * Verifies on a non-transient AmqpException, closes link.
     */
    @Test
    public void closesOnNonRetriableError() {
        // Arrange
        final Link link = mock(Link.class);
        final Session session = mock(Session.class);
        final Symbol symbol = Symbol.getSymbol(
            AmqpErrorCondition.UNAUTHORIZED_ACCESS.getErrorCondition());
        final String description = "test-symbol-description";
        final ErrorCondition condition = new ErrorCondition(symbol, description);
        final ArgumentCaptor<ErrorCondition> captor = ArgumentCaptor.forClass(ErrorCondition.class);
        final ArgumentCaptor<ErrorCondition> captor2 = ArgumentCaptor.forClass(ErrorCondition.class);

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
        verify(session, times(1)).close();

        verify(link).setCondition(captor.capture());
        Assertions.assertSame(condition, captor.getValue());

        verify(session).setCondition(captor2.capture());
        Assertions.assertSame(condition, captor2.getValue());
    }

    @Test
    public void closesOnNonAmqpException() {
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
