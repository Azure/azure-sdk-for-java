// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransactionCoordinator;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transaction.Coordinator;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorSessionTest {
    private static final String ID = "test-connection-id";
    private static final String HOST = "test-host";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final String NAME = "test-session-name";
    private static final Duration TIMEOUT = Duration.ofSeconds(45);

    private SessionHandler handler;
    private ReactorSession reactorSession;

    @Mock
    private Session session;
    @Mock
    private Reactor reactor;
    @Mock
    private Event event;
    @Mock
    private Receiver receiver;
    @Mock
    private Sender sender;
    @Mock
    private Record record;
    @Mock
    private ClaimsBasedSecurityNode cbsNode;
    @Mock
    private MessageSerializer serializer;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private ReactorHandlerProvider reactorHandlerProvider;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private TokenManagerProvider tokenManagerProvider;
    @Mock
    private AmqpConnection amqpConnection;

    private Mono<ClaimsBasedSecurityNode> cbsNodeSupplier;
    private AutoCloseable mocksCloseable;

    private final TestPublisher<AmqpShutdownSignal> connectionShutdown = TestPublisher.createCold();

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        this.handler = new SessionHandler(ID, HOST, ENTITY_PATH, reactorDispatcher, Duration.ofSeconds(60));
        this.cbsNodeSupplier = Mono.just(cbsNode);

        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);

        when(event.getSession()).thenReturn(session);

        when(sender.attachments()).thenReturn(record);
        when(receiver.attachments()).thenReturn(record);

        doAnswer(invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(reactorDispatcher).invoke(any());

        when(amqpConnection.getShutdownSignals()).thenReturn(connectionShutdown.flux());

        final AmqpRetryOptions options = new AmqpRetryOptions().setTryTimeout(TIMEOUT);
        this.reactorSession = new ReactorSession(amqpConnection, session, handler, NAME, reactorProvider,
            reactorHandlerProvider, cbsNodeSupplier, tokenManagerProvider, serializer, options);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void verifyConstructor() {
        // Assert
        verify(session, times(1)).open();

        Assertions.assertSame(session, reactorSession.session());
        Assertions.assertEquals(NAME, reactorSession.getSessionName());
        Assertions.assertEquals(TIMEOUT, reactorSession.getOperationTimeout());
    }

    @Test
    public void verifyEndpointStates() {
        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        StepVerifier.create(reactorSession.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> handler.onSessionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> handler.close())
            .expectNext(AmqpEndpointState.CLOSED)
            .expectComplete()
            .verify();
    }

    @Test
    public void verifyDispose() {
        reactorSession.dispose();
        assertTrue(reactorSession.isDisposed());
    }

    /**
     * Verifies that we can create the producer.
     */
    @Test
    void createProducer() {
        // Arrange
        final String linkName = "test-link-name";
        final String entityPath = "test-entity-path";

        final Duration timeout = Duration.ofSeconds(10);
        final AmqpRetryOptions options = new AmqpRetryOptions().setTryTimeout(timeout)
            .setMaxRetries(1)
            .setMode(AmqpRetryMode.FIXED);
        final AmqpRetryPolicy amqpRetryPolicy = new FixedAmqpRetryPolicy(options);

        final Map<Symbol, Object> linkProperties = new HashMap<>();
        final TokenManager tokenManager = mock(TokenManager.class);
        final SendLinkHandler sendLinkHandler = new SendLinkHandler(ID, HOST, linkName, entityPath);

        when(session.sender(linkName)).thenReturn(sender);
        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        when(tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath)).thenReturn(tokenManager);
        when(tokenManager.authorize()).thenReturn(Mono.just(1000L));
        when(tokenManager.getAuthorizationResults())
            .thenReturn(Flux.create(sink -> sink.next(AmqpResponseCode.ACCEPTED)));
        when(reactorHandlerProvider.createSendLinkHandler(ID, HOST, linkName, entityPath))
            .thenReturn(sendLinkHandler);

        StepVerifier.create(
            reactorSession.createProducer(linkName, entityPath, timeout, amqpRetryPolicy, linkProperties))
            .then(() -> handler.onSessionRemoteOpen(event))
            .thenAwait(Duration.ofSeconds(2))
            .assertNext(producer -> assertTrue(producer instanceof ReactorSender))
            .verifyComplete();

        final AmqpLink sendLink = reactorSession.createProducer(linkName, entityPath, timeout, amqpRetryPolicy,
            linkProperties)
            .block(TIMEOUT);

        assertNotNull(sendLink);
    }

    /**
     * Verifies that we can create the producer.
     */
    @Test
    void createProducerAgainAfterException() {
        // Arrange
        final String linkName = "test-link-name";
        final String entityPath = "test-entity-path";

        final Duration timeout = Duration.ofSeconds(10);
        final AmqpRetryOptions options = new AmqpRetryOptions().setTryTimeout(timeout)
            .setMaxRetries(1)
            .setMode(AmqpRetryMode.FIXED);
        final AmqpRetryPolicy amqpRetryPolicy = new FixedAmqpRetryPolicy(options);

        final Map<Symbol, Object> linkProperties = new HashMap<>();
        final TokenManager tokenManager = mock(TokenManager.class);
        final SendLinkHandler sendLinkHandler = new SendLinkHandler(ID, HOST, linkName, entityPath);

        final Event closeSendEvent = mock(Event.class);
        when(closeSendEvent.getLink()).thenReturn(sender);

        final ErrorCondition errorCondition = new ErrorCondition(
            Symbol.valueOf(AmqpErrorCondition.SERVER_BUSY_ERROR.getErrorCondition()), "test-busy");
        when(sender.getRemoteCondition()).thenReturn(errorCondition);

        when(session.sender(linkName)).thenReturn(sender);
        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        when(tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath)).thenReturn(tokenManager);
        when(tokenManager.authorize()).thenReturn(Mono.just(1000L));
        when(tokenManager.getAuthorizationResults())
            .thenReturn(Flux.create(sink -> sink.next(AmqpResponseCode.ACCEPTED)));
        when(reactorHandlerProvider.createSendLinkHandler(ID, HOST, linkName, entityPath))
            .thenReturn(sendLinkHandler);

        handler.onSessionRemoteOpen(event);

        final AmqpLink sendLink = reactorSession.createProducer(linkName, entityPath, timeout, amqpRetryPolicy,
            linkProperties)
            .block(TIMEOUT);

        assertNotNull(sendLink);
        assertTrue(sendLink instanceof AmqpSendLink);

        // Act
        sendLinkHandler.onLinkRemoteClose(closeSendEvent);
    }

    /**
     * Verifies that we can create coordinator.
     */
    @Test
    void getOrCreateTransactionCoordinator() {
        // Arrange
        final String transactionLinkName = "coordinator";
        final String linkName = transactionLinkName;
        final String entityPath = transactionLinkName;

        final TokenManager tokenManager = mock(TokenManager.class);
        final SendLinkHandler sendLinkHandler = new SendLinkHandler(ID, HOST, linkName, entityPath);

        when(session.sender(linkName)).thenReturn(sender);
        when(tokenManagerProvider.getTokenManager(cbsNodeSupplier, entityPath)).thenReturn(tokenManager);
        when(tokenManager.authorize()).thenReturn(Mono.just(1000L));
        when(tokenManager.getAuthorizationResults())
            .thenReturn(Flux.create(sink -> sink.next(AmqpResponseCode.ACCEPTED)));
        when(reactorHandlerProvider.createSendLinkHandler(ID, HOST, linkName, entityPath))
            .thenReturn(sendLinkHandler);

        StepVerifier.create(
            reactorSession.getOrCreateTransactionCoordinator())
            .then(() -> handler.onSessionRemoteOpen(event))
            .thenAwait(Duration.ofSeconds(2))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        verify(session).sender(transactionLinkName);
        verify(sender).setTarget(any(Coordinator.class));
        verify(session).open();

        final AmqpTransactionCoordinator coordinator1 = reactorSession.getOrCreateTransactionCoordinator()
            .block(TIMEOUT);

        final AmqpTransactionCoordinator coordinator2 = reactorSession.getOrCreateTransactionCoordinator()
            .block(TIMEOUT);

        assertSame(coordinator1, coordinator2);
    }

    @Test
    void createConsumer() {
        // Arrange
        final String linkName = "test-link-name";
        final String entityPath = "test-entity-path";
        final AmqpRetryPolicy amqpRetryPolicy = mock(AmqpRetryPolicy.class);
    }
}
