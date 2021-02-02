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
import com.azure.core.util.ClientOptions;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.SslDomain.VerifyMode;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReactorConnectionTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String SESSION_NAME = "test-session-name";
    private static final Duration TEST_DURATION = Duration.ofSeconds(30);
    private static final ConnectionStringProperties CREDENTIAL_INFO = new ConnectionStringProperties("Endpoint=sb"
        + "://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummySharedKeyName;"
        + "SharedAccessKey=dummySharedKeyValue;EntityPath=eventhub1;");
    private static final String HOSTNAME = CREDENTIAL_INFO.getEndpoint().getHost();
    private static final Scheduler SCHEDULER = Schedulers.elastic();
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final VerifyMode VERIFY_MODE = VerifyMode.VERIFY_PEER_NAME;

    private final ClientOptions clientOptions = new ClientOptions();

    private ReactorConnection connection;
    private ConnectionHandler connectionHandler;
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
    @Mock
    private ReactorHandlerProvider reactorHandlerProvider;

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

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMaxRetries(0).setTryTimeout(TEST_DURATION);
        final ConnectionOptions connectionOptions = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            tokenProvider, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, SCHEDULER, clientOptions, VERIFY_MODE);

        connectionHandler = new ConnectionHandler(CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION,
            VERIFY_MODE, clientOptions);

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.connectionToHost(HOSTNAME, connectionHandler.getProtocolPort(), connectionHandler))
            .thenReturn(connectionProtonJ);
        when(reactor.process()).thenReturn(true);

        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(CONNECTION_ID, connectionHandler.getMaxFrameSize())).thenReturn(reactor);

        when(reactorHandlerProvider.createConnectionHandler(CONNECTION_ID, HOSTNAME,
            connectionOptions.getTransportType(), connectionOptions.getProxyOptions(), PRODUCT, CLIENT_VERSION,
            VERIFY_MODE, connectionOptions.getClientOptions()))
            .thenReturn(connectionHandler);

        sessionHandler = new SessionHandler(CONNECTION_ID, HOSTNAME, SESSION_NAME, reactorDispatcher, TEST_DURATION);
        when(reactorHandlerProvider.createSessionHandler(anyString(), anyString(), anyString(), any(Duration.class)))
            .thenReturn(sessionHandler);

        connection = new ReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, reactorHandlerProvider,
            tokenManager, messageSerializer, PRODUCT, CLIENT_VERSION, SenderSettleMode.SETTLED,
            ReceiverSettleMode.FIRST);
    }

    @AfterEach
    void teardown() {
        connection.dispose();
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Can create a ReactorConnection and the appropriate properties are set for TransportType.AMQP
     */
    @Test
    void createConnection() {
        // Arrange
        final Map<String, Object> expectedProperties = new HashMap<>(connectionHandler.getConnectionProperties());

        // Assert
        Assertions.assertNotNull(connection);
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
    void createSession() {
        // Arrange
        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still
        // process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
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

        verify(record).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    void removeSessionThatExists() {
        // Arrange
        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still
        // process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME).map(s -> connection.removeSession(s.getSessionName())))
            .expectNext(true)
            .verifyComplete();

        verify(record).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    void removeSessionThatDoesNotExist() {
        // Arrange
        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still
        // process.
        when(reactor.process()).thenReturn(true);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME).map(s -> connection.removeSession("does-not-exist")))
            .expectNext(false)
            .verifyComplete();

        verify(record).set(Handler.class, Handler.class, sessionHandler);
    }

    /**
     * Verifies initial endpoint state is uninitialized and completes when the connection is closed.
     */
    @Test
    void initialConnectionState() {
        // Assert
        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> connection.dispose())
            .verifyComplete();
    }

    /**
     * Verifies Connection state reports correct status when ConnectionHandler updates its state.
     */
    @Test
    void onConnectionStateOpen() {
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
                connection.dispose();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can get the CBS node.
     */
    @Test
    void createCBSNode() {
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
    void createCBSNodeTimeoutException() {
        // Arrange
        final Duration timeout = Duration.ofSeconds(2);
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMaxRetries(1)
            .setDelay(Duration.ofMillis(200))
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(timeout);
        final ConnectionOptions parameters = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            tokenProvider, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel(), clientOptions, VERIFY_MODE);

        final ConnectionHandler handler = new ConnectionHandler(CONNECTION_ID, HOSTNAME, PRODUCT, CLIENT_VERSION,
            VERIFY_MODE, clientOptions);
        final ReactorHandlerProvider provider = mock(ReactorHandlerProvider.class);

        when(provider.createConnectionHandler(CONNECTION_ID, HOSTNAME, parameters.getTransportType(),
            parameters.getProxyOptions(), PRODUCT, CLIENT_VERSION, VERIFY_MODE, clientOptions))
            .thenReturn(handler);
        when(provider.createSessionHandler(CONNECTION_ID, HOSTNAME, SESSION_NAME, timeout)).thenReturn(sessionHandler);

        when(reactor.connectionToHost(HOSTNAME, handler.getProtocolPort(), handler)).thenReturn(connectionProtonJ);

        // Act and Assert
        final ReactorConnection connectionBad = new ReactorConnection(CONNECTION_ID, parameters, reactorProvider,
            provider, tokenManager, messageSerializer, PRODUCT, CLIENT_VERSION, SenderSettleMode.SETTLED,
            ReceiverSettleMode.FIRST);

        try {
            StepVerifier.create(connectionBad.getClaimsBasedSecurityNode())
                .expectErrorSatisfies(error -> {
                    Assertions.assertTrue(error instanceof TimeoutException
                        || error.getCause() instanceof TimeoutException);
                })
                .verify();
        } finally {
            connectionBad.dispose();
        }
    }

    /**
     * Verifies if the ConnectionHandler transport fails, then we are unable to create the CBS node or sessions.
     */
    @Test
    void cannotCreateResourcesOnFailure() {
        // Arrange
        final Event event = mock(Event.class);
        final Transport transport = mock(Transport.class);
        final AmqpErrorCondition condition = AmqpErrorCondition.NOT_FOUND;
        final ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol(condition.getErrorCondition()),
            "Not found");

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
                final AmqpException amqpException;
                if (e instanceof AmqpException) {
                    amqpException = (AmqpException) e;
                } else if (e.getCause() instanceof AmqpException) {
                    amqpException = (AmqpException) e.getCause();
                } else {
                    amqpException = null;
                    Assertions.fail("Exception was not the correct type: " + e);
                }

                Assertions.assertEquals(condition, amqpException.getErrorCondition());
            })
            .verify();

        verify(transport, times(1)).unbind();
    }
}
