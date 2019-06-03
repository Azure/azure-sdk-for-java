// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
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

import static org.mockito.Mockito.mock;
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
        // Act
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
                Assert.assertEquals(SESSION_NAME, s.sessionName());
            }).verifyComplete();

        verify(record, Mockito.times(1)).set(Handler.class, Handler.class, sessionHandler);
    }
}
