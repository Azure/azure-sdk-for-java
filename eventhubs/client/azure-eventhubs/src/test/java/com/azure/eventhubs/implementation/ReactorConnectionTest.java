// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.CredentialInfo;
import com.azure.eventhubs.ProxyConfiguration;
import com.azure.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.amqp.Symbol;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorConnectionTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String SESSION_NAME = "test-session-name";
    private static final Duration TEST_DURATION = Duration.ofSeconds(30);
    private static final CredentialInfo CREDENTIAL_INFO = CredentialInfo.from("Endpoint=sb://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummySharedKeyName;SharedAccessKey=dummySharedKeyValue;EntityPath=eventhub1;");
    private static final String HOSTNAME = CREDENTIAL_INFO.endpoint().getHost();
    private static final Scheduler SCHEDULER = Schedulers.elastic();

    private AmqpConnection connection;
    private SessionHandler sessionHandler;

    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private AmqpResponseMapper responseMapper;

    private MockReactorProvider reactorProvider;
    private MockReactorHandlerProvider reactorHandlerProvider;
    private ConnectionHandler connectionHandler;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(reactor.selectable()).thenReturn(selectable);

        connectionHandler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);

        ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        reactorProvider = new MockReactorProvider(reactor, reactorDispatcher);
        sessionHandler = new SessionHandler(CONNECTION_ID, HOSTNAME, SESSION_NAME, reactorDispatcher, TEST_DURATION);
        reactorHandlerProvider = new MockReactorHandlerProvider(reactorProvider, connectionHandler, sessionHandler, null, null);

        ConnectionParameters connectionParameters = new ConnectionParameters(CREDENTIAL_INFO, TEST_DURATION, tokenProvider, TransportType.AMQP, Retry.getDefaultRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, SCHEDULER);
        connection = new ReactorConnection(CONNECTION_ID, connectionParameters, reactorProvider, reactorHandlerProvider, responseMapper);
    }

    @After
    public void teardown() {
        reactor = null;
        selectable = null;

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Can create a ReactorConnection and the appropriate properties are set for TransportType.AMQP
     */
    @Test
    public void createConnection() {
        // Arrange
        final Map<String, Object> expectedProperties = new HashMap<>(connectionHandler.getConnectionProperties());

        // Assert
        Assert.assertTrue(connection instanceof ReactorConnection);
        Assert.assertEquals(CONNECTION_ID, connection.getIdentifier());
        Assert.assertEquals(HOSTNAME, connection.getHost());

        Assert.assertEquals(connectionHandler.getMaxFrameSize(), connection.getMaxFrameSize());

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
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler)).thenReturn(connectionProtonJ);
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
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler)).thenReturn(connectionProtonJ);
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
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler)).thenReturn(connectionProtonJ);
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
            .then(() -> connectionHandler.onConnectionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            // getConnectionStates is distinct. We don't expect to see another event with the same status.
            .then(() -> connectionHandler.onConnectionRemoteOpen(event))
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
        // Arrange
        final Connection reactorConnection = mock(Connection.class);
        when(reactorConnection.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(reactorConnection);
        connectionHandler.onConnectionRemoteOpen(mock);

        // Act and Assert
        StepVerifier.create(this.connection.getCBSNode())
            .assertNext(node -> {
                Assert.assertTrue(node instanceof CBSChannel);
            }).verifyComplete();
    }

    /**
     * Verifies that if the connection cannot be created within the timeout period, it errors.
     */
    @Test
    public void createCBSNodeTimeoutException() throws IOException {
        // Arrange
        Duration timeout = Duration.ofSeconds(5);
        ConnectionParameters parameters = new ConnectionParameters(CREDENTIAL_INFO, timeout, tokenProvider,
            TransportType.AMQP, Retry.getDefaultRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.elastic());

        // Act and Assert
        try (ReactorConnection connectionBad = new ReactorConnection(CONNECTION_ID, parameters, reactorProvider, reactorHandlerProvider, mock(AmqpResponseMapper.class))) {
            StepVerifier.create(connectionBad.getCBSNode())
                .verifyError(TimeoutException.class);
        }
    }

    /**
     * Verifies if the ConnectionHandler transport fails, then we are unable to create the CBS node or sessions.
     */
    @Test
    public void cannotCreateResourcesOnFailure() {
        // Arrange
        final Event event = mock(Event.class);
        final Connection connectionProtonJ = mock(Connection.class);
        final Transport transport = mock(Transport.class);
        final ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol("amqp:not-found"), "Not found");

        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(transport.getCondition()).thenReturn(errorCondition);
        when(connectionProtonJ.getHostname()).thenReturn(HOSTNAME);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        connectionHandler.onTransportError(event);

        StepVerifier.create(connection.getCBSNode())
            .assertNext(node -> {
                Assert.assertTrue(node instanceof CBSChannel);
            }).verifyComplete();

        verify(transport, times(1)).unbind();
    }
}
