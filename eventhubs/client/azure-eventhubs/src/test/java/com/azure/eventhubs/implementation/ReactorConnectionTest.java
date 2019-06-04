// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorConnectionTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String HOSTNAME = "test-host-name";
    private static final String SCHEDULER_NAME = "test-scheduler";
    private static final String SESSION_NAME = "test-session-name";
    private static final Duration TEST_DURATION = Duration.ofSeconds(30);

    private AmqpConnection connection;
    private ConnectionHandler handler;
    private Reactor reactor;
    private ReactorDispatcher reactorDispatcher;
    private ReactorHandlerProvider reactorHandlerProvider;
    private ReactorProvider reactorProvider;
    private Scheduler scheduler;
    private Selectable selectable;
    private SessionHandler sessionHandler;
    private TokenProvider tokenProvider;

    @Before
    public void initialize() throws IOException {
        scheduler = Schedulers.newSingle(SCHEDULER_NAME);
        tokenProvider = mock(TokenProvider.class);
        reactor = mock(Reactor.class);
        selectable = mock(Selectable.class);
        when(reactor.selectable()).thenReturn(selectable);

        reactorDispatcher = new ReactorDispatcher(reactor);
        reactorProvider = new MockReactorProvider(reactor, reactorDispatcher);
        handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);
        sessionHandler = new SessionHandler(CONNECTION_ID, HOSTNAME, SESSION_NAME, reactorDispatcher, TEST_DURATION);
        reactorHandlerProvider = new MockReactorHandlerProvider(reactorProvider, handler, sessionHandler);

        connection = ReactorConnection.create(CONNECTION_ID, HOSTNAME, tokenProvider, reactorProvider, reactorHandlerProvider, scheduler);
    }

    /**
     * Can create a ReactorConnection and the appropriate properties are set for TransportType.AMQP
     */
    @Test
    public void createConnection() {
        // Arrange
        final Map<String, Object> expectedProperties = new HashMap<>(handler.getConnectionProperties());

        // Assert
        Assert.assertTrue(connection instanceof ReactorConnection);
        Assert.assertEquals(CONNECTION_ID, connection.getIdentifier());
        Assert.assertEquals(HOSTNAME, connection.getHost());

        Assert.assertEquals(handler.getMaxFrameSize(), connection.getMaxFrameSize());

        Assert.assertNotNull(connection.getConnectionProperties());
        Assert.assertEquals(expectedProperties.size(), connection.getConnectionProperties().size());

        expectedProperties.forEach((key, value) -> {
            final Object removed = connection.getConnectionProperties().remove(key);
            Assert.assertNotNull(removed);

            final String expected = String.valueOf(value);
            final String actual = String.valueOf(removed);
            Assert.assertEquals(expected, actual);
        });
        Assert.assertTrue(connection.getConnectionProperties().isEmpty());
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    public void createSession() {
        // Arrange
        final Connection connectionProtonJ = mock(Connection.class);
        final Session session = mock(Session.class);
        final Record record = mock(Record.class);

        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .assertNext(s -> {
                Assert.assertNotNull(s);
                Assert.assertEquals(SESSION_NAME, s.getSessionName());
                Assert.assertTrue(s instanceof ReactorSession);
                Assert.assertSame(session, ((ReactorSession) s).session());
            }).verifyComplete();

        // Assert that the same instance is obtained and we don't get a new session with the same name.
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .assertNext(s -> {
                Assert.assertNotNull(s);
                Assert.assertEquals(SESSION_NAME, s.getSessionName());
                Assert.assertTrue(s instanceof ReactorSession);
                Assert.assertSame(session, ((ReactorSession) s).session());
            }).verifyComplete();

        verify(record, Mockito.times(1)).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    public void removeSessionThatExists() {
        // Arrange
        final Connection connectionProtonJ = mock(Connection.class);
        final Session session = mock(Session.class);
        final Record record = mock(Record.class);

        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME).map(s -> connection.removeSession(s.getSessionName())))
            .expectNext(true)
            .verifyComplete();

        verify(record, Mockito.times(1)).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    public void removeSessionThatDoesNotExist() {
        // Arrange
        final Connection connectionProtonJ = mock(Connection.class);
        final Session session = mock(Session.class);
        final Record record = mock(Record.class);

        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(handler.getHostname(), handler.getProtocolPort(), handler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME).map(s -> connection.removeSession("does-not-exist")))
            .expectNext(false)
            .verifyComplete();

        verify(record, Mockito.times(1)).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Verifies initial endpoint state is uninitialized and completes when the connection is closed.
     */
    @Test
    public void initialConnectionState() {
        // Assert
        StepVerifier.create(connection.getConnectionStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> {
                try {
                    connection.close();
                } catch (IOException e) {
                    Assert.fail("Should not have thrown an error.");
                }
            })
            .verifyComplete();
    }

    /**
     * Verifies Connection state reports correct status when ConnectionHandler updates its state.
     */
    @Test
    public void onConnectionStateOpen() {
        // Arrange
        final Event event = mock(Event.class);
        final Connection connectionProtonJ = mock(Connection.class);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(connectionProtonJ.getHostname()).thenReturn(HOSTNAME);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // Act & Assert
        StepVerifier.create(connection.getConnectionStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> handler.onConnectionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            // getConnectionStates is distinct. We don't expect to see another event with the same status.
            .then(() -> handler.onConnectionRemoteOpen(event))
            .then(() -> {
                try {
                    connection.close();
                } catch (IOException e) {
                    Assert.fail("Should not have thrown an error.");
                }
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can get the CBS node.
     */
    @Test
    public void createCBSNode() {
        // Act and Assert
        StepVerifier.create(connection.getCBSNode())
            .assertNext(node -> {
                Assert.assertTrue(node instanceof CBSChannel);
            }).verifyComplete();
    }

    /**
     * Verifies if the ConnectionHandler transport fails, then we are unable to create the CBS node or sessions.
     */
    @Test
    public void cannotCreateResourcesWhenErrored() {
        // Arrange
        final Event event = mock(Event.class);
        final Connection connectionProtonJ = mock(Connection.class);
        final Transport transport = mock(Transport.class);
        final ErrorCondition errorCondition = mock(ErrorCondition.class);

        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(transport.getCondition()).thenReturn(errorCondition);
        when(connectionProtonJ.getHostname()).thenReturn(HOSTNAME);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        handler.onTransportError(event);

        StepVerifier.create(connection.getCBSNode())
            .assertNext(node -> {
                Assert.assertTrue(node instanceof CBSChannel);
            }).verifyComplete();

        verify(transport, times(1)).unbind();
    }
}
