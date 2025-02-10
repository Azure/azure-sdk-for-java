// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReactorReceiver} using
 * {@link reactor.test.scheduler.VirtualTimeScheduler} hence needs
 * to run in isolated and sequential.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class ReactorReceiverIsolatedTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);

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
    private AmqpConnection amqpConnection;
    @Mock
    private TokenManager tokenManager;

    private final TestPublisher<AmqpShutdownSignal> shutdownSignals = TestPublisher.create();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
    private final TestPublisher<AmqpResponseCode> authorizationResults = TestPublisher.createCold();

    private ReceiveLinkHandler receiverHandler;
    private ReactorReceiver reactorReceiver;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(cbsNode.authorize(any(), any())).thenReturn(Mono.empty());

        when(event.getLink()).thenReturn(receiver);
        when(receiver.getRemoteSource()).thenReturn(new Source());

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.attachments()).thenReturn(record);

        final String entityPath = "test-entity-path";
        receiverHandler
            = new ReceiveLinkHandler("test-connection-id", "test-host", "test-receiver-name", entityPath, null);

        when(tokenManager.getAuthorizationResults()).thenReturn(authorizationResults.flux());

        when(amqpConnection.getShutdownSignals()).thenReturn(shutdownSignals.flux());

        reactorReceiver
            = new ReactorReceiver(amqpConnection, entityPath, receiver, new ReceiveLinkHandlerWrapper(receiverHandler),
                tokenManager, reactorDispatcher, retryOptions, AmqpMetricsProvider.noop());
    }

    @AfterEach
    void teardown() throws Exception {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Tests the completion of {@link ReactorReceiver#getEndpointStates()}
     * when there is no link remote-close frame.
     */
    @Test
    @Execution(ExecutionMode.SAME_THREAD)
    void endpointStateCompletesOnNoRemoteCloseAck() throws IOException {
        // Arrange
        final String message = "some-message";
        final AmqpErrorCondition errorCondition = AmqpErrorCondition.UNAUTHORIZED_ACCESS;
        final ErrorCondition condition
            = new ErrorCondition(Symbol.getSymbol(errorCondition.getErrorCondition()), "Test-users");

        when(receiver.getLocalState()).thenReturn(EndpointState.ACTIVE);

        doAnswer(invocation -> {
            // The ReactorDispatcher running localClose() work scheduled by beginClose(...).
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(reactorDispatcher).invoke(any(Runnable.class));

        // The localClose() initiated local-close via receiver.close(), here we mock scenario where
        // there is no remote-close ack from the broker for the local-close, hence do nothing i.e. no
        // call to receiverHandler.onLinkRemoteClose(event)
        doNothing().when(receiver).close();

        // Act & Assert
        final VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create();

        try {
            StepVerifier
                .withVirtualTime(() -> reactorReceiver.closeAsync(message, condition), () -> virtualTimeScheduler, 1)
                // Advance virtual time beyond the default timeout of 60 sec, so endpoint state
                // completion timeout kicks in.
                .thenAwait(Duration.ofSeconds(100))
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Assert
            StepVerifier.create(reactorReceiver.getEndpointStates())
                // Assert endpoint state completes (via timeout) when there is no broker remote-close ack.
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            assertTrue(reactorReceiver.isDisposed());

            verify(receiver).setCondition(condition);
            verify(receiver).close();

            shutdownSignals.assertNoSubscribers();
        } finally {
            virtualTimeScheduler.dispose();
        }
    }
}
