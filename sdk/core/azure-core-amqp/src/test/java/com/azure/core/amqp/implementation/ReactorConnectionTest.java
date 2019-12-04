// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.credential.TokenCredential;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private static final ConnectionStringProperties CREDENTIAL_INFO = new ConnectionStringProperties("Endpoint=sb://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummySharedKeyName;SharedAccessKey=dummySharedKeyValue;EntityPath=eventhub1;");
    private static final String HOSTNAME = CREDENTIAL_INFO.getEndpoint().getHost();
    private static final Scheduler SCHEDULER = Schedulers.elastic();

    private ReactorConnection connection;
    private SessionHandler sessionHandler;

    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private TokenCredential tokenProvider;
    @Mock
    private Connection connectionProtonJ;
    @Mock
    private Session session;
    @Mock
    private Record record;
    @Mock
    private TokenManagerProvider tokenManager;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private ReactorProvider reactorProvider;
    private ConnectionHandler connectionHandler;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(reactor.selectable()).thenReturn(selectable);

        connectionHandler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(CONNECTION_ID, connectionHandler.getMaxFrameSize())).thenReturn(reactor);

        sessionHandler = new SessionHandler(CONNECTION_ID, HOSTNAME, SESSION_NAME, reactorDispatcher, TEST_DURATION);

        final ReactorHandlerProvider reactorHandlerProvider = new MockReactorHandlerProvider(reactorProvider, connectionHandler, sessionHandler, null, null);

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setTryTimeout(TEST_DURATION);
        final ConnectionOptions connectionOptions = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            CREDENTIAL_INFO.getEntityPath(), tokenProvider, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE,
            AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, SCHEDULER);
        connection = new ReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, reactorHandlerProvider,
            tokenManager, messageSerializer);
    }

    @AfterEach
    public void teardown() {
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
        Assertions.assertTrue(connection instanceof ReactorConnection);
        Assertions.assertEquals(CONNECTION_ID, connection.getId());
        Assertions.assertEquals(HOSTNAME, connection.getFullyQualifiedNamespace());

        Assertions.assertEquals(connectionHandler.getMaxFrameSize(), connection.getMaxFrameSize());

        Assertions.assertNotNull(connection.getConnectionProperties());
        Assertions.assertEquals(expectedProperties.size(), connection.getConnectionProperties().size());

        expectedProperties.forEach((key, value) -> {
            final Object removed = connection.getConnectionProperties().remove(key);
            Assertions.assertNotNull(removed);

            final String expected = String.valueOf(value);
            final String actual = String.valueOf(removed);
            Assertions.assertEquals(expected, actual);
        });
        Assertions.assertTrue(connection.getConnectionProperties().isEmpty());
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    public void createSession() {
        // Arrange
        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(), connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .assertNext(s -> {
                Assertions.assertNotNull(s);
                Assertions.assertEquals(SESSION_NAME, s.getSessionName());
                Assertions.assertTrue(s instanceof ReactorSession);
                Assertions.assertSame(session, ((ReactorSession) s).session());
            }).verifyComplete();

        // Assert that the same instance is obtained and we don't get a new session with the same name.
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .assertNext(s -> {
                Assertions.assertNotNull(s);
                Assertions.assertEquals(SESSION_NAME, s.getSessionName());
                Assertions.assertTrue(s instanceof ReactorSession);
                Assertions.assertSame(session, ((ReactorSession) s).session());
            }).verifyComplete();

        verify(record, Mockito.times(1)).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    public void removeSessionThatExists() {
        // Arrange
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
        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> {
                connection.close();
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
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(connectionProtonJ.getHostname()).thenReturn(HOSTNAME);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // Act & Assert
        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> connectionHandler.onConnectionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            // getConnectionStates is distinct. We don't expect to see another event with the same status.
            .then(() -> connectionHandler.onConnectionRemoteOpen(event))
            .then(() -> {
                connection.close();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can get the CBS node.
     */
    @Test
    public void createCBSNode() {
        // Arrange
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(connectionProtonJ);
        connectionHandler.onConnectionRemoteOpen(mock);

        // Act and Assert
        StepVerifier.create(this.connection.getClaimsBasedSecurityNode())
            .assertNext(node -> {
                Assertions.assertTrue(node instanceof ClaimsBasedSecurityChannel);
            }).verifyComplete();
    }

    /**
     * Verifies that if the connection cannot be created within the timeout period, it errors.
     */
    @Test
    public void createCBSNodeTimeoutException() {
        // Arrange
        final ConnectionHandler handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME);
        final ReactorHandlerProvider provider = new MockReactorHandlerProvider(reactorProvider, handler, sessionHandler,
            null, null);

        Duration timeout = Duration.ofSeconds(2);
        AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMaxRetries(2)
            .setDelay(Duration.ofMillis(200))
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(timeout);
        ConnectionOptions parameters = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            CREDENTIAL_INFO.getEntityPath(), tokenProvider, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE,
            AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());

        // Act and Assert
        try (ReactorConnection connectionBad = new ReactorConnection(CONNECTION_ID, parameters, reactorProvider,
            provider, tokenManager, messageSerializer)) {
            StepVerifier.create(connectionBad.getClaimsBasedSecurityNode())
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
        final Transport transport = mock(Transport.class);
        final AmqpErrorCondition condition = AmqpErrorCondition.NOT_FOUND;
        final ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol(condition.getErrorCondition()), "Not found");

        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(transport.getCondition()).thenReturn(errorCondition);
        when(connectionProtonJ.getHostname()).thenReturn(HOSTNAME);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        connectionHandler.onTransportError(event);

        // Act & Assert
        StepVerifier.create(connection.getClaimsBasedSecurityNode())
            .expectErrorSatisfies(e -> {
                Assertions.assertTrue(e instanceof AmqpException);
                AmqpException amqpException = (AmqpException) e;
                Assertions.assertEquals(condition, amqpException.getErrorCondition());
            })
            .verify(Duration.ofSeconds(10));

        verify(transport, times(1)).unbind();
    }
}
