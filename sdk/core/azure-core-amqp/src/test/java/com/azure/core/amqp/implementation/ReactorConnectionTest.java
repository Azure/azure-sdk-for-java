// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReactorConnectionTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String SESSION_NAME = "test-session-name";
    private static final Duration TEST_DURATION = Duration.ofSeconds(3);
    private static final ConnectionStringProperties CREDENTIAL_INFO = new ConnectionStringProperties("Endpoint=sb"
        + "://test-event-hub.servicebus.windows.net/;SharedAccessKeyName=dummySharedKeyName;"
        + "SharedAccessKey=dummySharedKeyValue;EntityPath=eventhub1;");
    private static final String FULLY_QUALIFIED_NAMESPACE = CREDENTIAL_INFO.getEndpoint().getHost();
    private static final Scheduler SCHEDULER = Schedulers.boundedElastic();
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;

    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions().setHeaders(
        Arrays.asList(new Header("name", PRODUCT), new Header("version", CLIENT_VERSION)));

    private final SslPeerDetails peerDetails = Proton.sslPeerDetails(FULLY_QUALIFIED_NAMESPACE, 3128);

    private ReactorConnection connection;
    private ConnectionHandler connectionHandler;
    private SessionHandler sessionHandler;
    private AutoCloseable mocksCloseable;
    private ConnectionOptions connectionOptions;

    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private TokenCredential tokenCredential;
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
    @Mock
    private Event connectionEvent;
    @Mock
    private Event sessionEvent;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMaxRetries(0).setTryTimeout(TEST_DURATION);
        connectionOptions = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope",
            AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, SCHEDULER, CLIENT_OPTIONS, VERIFY_MODE,
            PRODUCT, CLIENT_VERSION);

        connectionHandler = new ConnectionHandler(CONNECTION_ID, connectionOptions, peerDetails);

        when(reactor.selectable()).thenReturn(selectable);
        when(reactor.connectionToHost(FULLY_QUALIFIED_NAMESPACE, connectionHandler.getProtocolPort(),
            connectionHandler))
            .thenReturn(connectionProtonJ);
        when(reactor.attachments()).thenReturn(mock(Record.class));

        final Pipe pipe = Pipe.open();
        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(CONNECTION_ID, reactor, pipe);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(CONNECTION_ID, connectionHandler.getMaxFrameSize())).thenReturn(reactor);

        when(reactorHandlerProvider.createConnectionHandler(CONNECTION_ID, connectionOptions))
            .thenReturn(connectionHandler);
        sessionHandler = new SessionHandler(CONNECTION_ID, FULLY_QUALIFIED_NAMESPACE, SESSION_NAME, reactorDispatcher,
            TEST_DURATION);
        when(reactorHandlerProvider.createSessionHandler(anyString(), anyString(), anyString(), any(Duration.class)))
            .thenReturn(sessionHandler);

        connection = new ReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, reactorHandlerProvider,
            tokenManager, messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST);

        // Setting up onConnectionRemoteOpen.
        when(connectionEvent.getConnection()).thenReturn(connectionProtonJ);
        when(connectionEvent.getReactor()).thenReturn(reactor);

        when(sessionEvent.getSession()).thenReturn(session);
    }

    @AfterEach
    void teardown() throws Exception {
        connectionHandler.close();
        sessionHandler.close();

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
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
        Assertions.assertEquals(FULLY_QUALIFIED_NAMESPACE, connection.getFullyQualifiedNamespace());

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
        assertTrue(connection.getConnectionProperties().isEmpty());
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    void createSession() {
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);

        when(session.attachments()).thenReturn(record);
        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // We only want it to emit a session when it is active.
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        connectionHandler.onConnectionRemoteOpen(connectionEvent);

        sessionHandler.onSessionRemoteOpen(sessionEvent);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .assertNext(s -> {
                Assertions.assertNotNull(s);
                Assertions.assertEquals(SESSION_NAME, s.getSessionName());
                assertTrue(s instanceof ReactorSession);
                Assertions.assertSame(session, ((ReactorSession) s).session());
            }).verifyComplete();

        // Assert that the same instance is obtained and we don't get a new session with the same name.
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .assertNext(s -> {
                Assertions.assertNotNull(s);
                Assertions.assertEquals(SESSION_NAME, s.getSessionName());
                assertTrue(s instanceof ReactorSession);
                Assertions.assertSame(session, ((ReactorSession) s).session());
            }).verifyComplete();

        verify(record).set(Handler.class, Handler.class, sessionHandler);
    }

    @Test
    void createSessionWhenConnectionInactive() {
        // Arrange
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);

        when(session.attachments()).thenReturn(record);
        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // We only want it to emit a session when it is active.
        when(connectionProtonJ.getLocalState()).thenReturn(EndpointState.ACTIVE);
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.UNINITIALIZED);

        // Act & Assert
        StepVerifier.create(connection.createSession(SESSION_NAME))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                assertTrue(((AmqpException) error).isTransient());
            })
            .verify();
    }

    /**
     * Creates a session with the given name and set handler.
     */
    @Test
    void removeSessionThatExists() {
        final Record reactorRecord = mock(Record.class);
        when(reactor.attachments()).thenReturn(reactorRecord);
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // We only want it to emit a session when it is active.
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        connectionHandler.onConnectionRemoteOpen(connectionEvent);

        sessionHandler.onSessionRemoteOpen(sessionEvent);

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
        when(reactor.connectionToHost(connectionHandler.getHostname(), connectionHandler.getProtocolPort(),
            connectionHandler)).thenReturn(connectionProtonJ);
        when(connectionProtonJ.session()).thenReturn(session);
        when(session.attachments()).thenReturn(record);

        when(session.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // We only want it to emit a session when it is active.
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        connectionHandler.onConnectionRemoteOpen(connectionEvent);

        sessionHandler.onSessionRemoteOpen(sessionEvent);

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
            .thenCancel()
            .verify();
    }

    /**
     * Verifies Connection state reports correct status when ConnectionHandler updates its state.
     */
    @Test
    void onConnectionStateOpen() {
        final Event event = mock(Event.class);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(connectionProtonJ.getHostname()).thenReturn(FULLY_QUALIFIED_NAMESPACE);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        // Act & Assert
        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> connectionHandler.onConnectionRemoteOpen(event))
            .expectNext(AmqpEndpointState.ACTIVE)
            // getConnectionStates is distinct. We don't expect to see another event with the same status.
            .then(() -> connectionHandler.onConnectionRemoteOpen(event))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can get the CBS node.
     */
    @Test
    void createCBSNode() {
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(connectionProtonJ);
        connectionHandler.onConnectionRemoteOpen(mock);

        // Act and Assert
        StepVerifier.create(this.connection.getClaimsBasedSecurityNode())
            .assertNext(node -> assertTrue(node instanceof ClaimsBasedSecurityChannel))
            .verifyComplete();
    }

    /**
     * Verifies that if the connection cannot be created within the timeout period, it errors.
     */
    @Test
    void createCBSNodeTimeoutException() throws IOException {
        when(reactor.process()).then(invocation -> {
            TimeUnit.SECONDS.sleep(10);
            return true;
        });
        final Duration timeout = Duration.ofSeconds(2);
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMaxRetries(1)
            .setDelay(Duration.ofMillis(200))
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(timeout);
        final ConnectionOptions connectionOptions = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope",
            AmqpTransportType.AMQP, retryOptions, ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel(),
            CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        final ConnectionHandler handler = new ConnectionHandler(CONNECTION_ID, connectionOptions, peerDetails);
        final ReactorHandlerProvider handlerProvider = mock(ReactorHandlerProvider.class);
        final ReactorProvider reactorProvider = mock(ReactorProvider.class);
        final ReactorDispatcher dispatcher = mock(ReactorDispatcher.class);

        when(reactorProvider.createReactor(anyString(), anyInt())).thenReturn(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(dispatcher);

        when(dispatcher.getShutdownSignal()).thenReturn(Mono.never());

        when(handlerProvider.createConnectionHandler(CONNECTION_ID, connectionOptions))
            .thenReturn(handler);
        when(handlerProvider.createSessionHandler(CONNECTION_ID, FULLY_QUALIFIED_NAMESPACE, SESSION_NAME, timeout))
            .thenReturn(sessionHandler);

        when(reactor.connectionToHost(FULLY_QUALIFIED_NAMESPACE, handler.getProtocolPort(), handler))
            .thenReturn(connectionProtonJ);

        // Act and Assert
        final ReactorConnection connectionBad = new ReactorConnection(CONNECTION_ID, connectionOptions,
            reactorProvider, handlerProvider, tokenManager, messageSerializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.FIRST);

        StepVerifier.create(connectionBad.getClaimsBasedSecurityNode())
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);

                final AmqpException amqpException = (AmqpException) error;
                assertTrue(amqpException.isTransient());
                assertNull(amqpException.getErrorCondition());

                assertNotNull(amqpException.getMessage());

                assertNotNull(amqpException.getContext());
                assertEquals(FULLY_QUALIFIED_NAMESPACE, amqpException.getContext().getNamespace());
            })
            .verify();
    }

    /**
     * Ensures we get correct endpoints until connection is closed.
     */
    @Test
    void endpointStatesOnDispose() {
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE, EndpointState.CLOSED,
            EndpointState.CLOSED);
        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(connectionProtonJ);
        connectionHandler.onConnectionRemoteOpen(mock);

        final Event closeEvent = mock(Event.class);
        when(closeEvent.getConnection()).thenReturn(connectionProtonJ);

        final Event finalEvent = mock(Event.class);
        when(finalEvent.getConnection()).thenReturn(connectionProtonJ);

        // Act and Assert
        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.ACTIVE)
            .then(() -> connectionHandler.onConnectionRemoteClose(closeEvent))
            .expectNext(AmqpEndpointState.CLOSED)
            .then(() -> connectionHandler.onConnectionFinal(finalEvent))
            .verifyComplete();

        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.CLOSED)
            .verifyComplete();
    }

    /**
     * Ensures we get a transient AmqpException when connection is broken.
     */
    @Disabled("It's stuck at disposing the connection.")
    @Test
    void endpointStatesTransportError() {
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.UNINITIALIZED);
        final Event event = mock(Event.class);
        final Transport transport = mock(Transport.class);
        final ErrorCondition errorCondition = mock(ErrorCondition.class);
        when(errorCondition.getCondition()).thenReturn(AmqpErrorCode.CONNECTION_FORCED);
        when(errorCondition.getDescription()).thenReturn("mock condition description");
        when(transport.getCondition()).thenReturn(errorCondition);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(event.getTransport()).thenReturn(transport);

        // Act and Assert
        StepVerifier.create(connection.getEndpointStates())
            .expectNext(AmqpEndpointState.UNINITIALIZED)
            .then(() -> connectionHandler.onTransportError(event))
            .expectErrorMatches(error -> {
                AmqpException amqpExp = (AmqpException) error;
                return amqpExp.isTransient();
            })
            .verify();
    }

    /**
     * Ensures we get correct shutdown signal when connection is closed.
     */
    @Test
    void shutdownSignalsOnDispose() {
        // Arrange
        final AtomicBoolean isOpen = new AtomicBoolean(true);
        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still
        // process.
        when(reactor.process()).thenAnswer(invocation -> isOpen.get());

        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE, EndpointState.CLOSED,
            EndpointState.CLOSED);
        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(connectionProtonJ);
        connectionHandler.onConnectionRemoteOpen(mock);

        final Event closeEvent = mock(Event.class);
        when(closeEvent.getConnection()).thenReturn(connectionProtonJ);

        final Event finalEvent = mock(Event.class);
        when(finalEvent.getConnection()).thenReturn(connectionProtonJ);

        // Act and Assert
        StepVerifier.create(connection.getShutdownSignals())
            .then(() -> {
                connection.getReactorConnection().subscribe();
                isOpen.set(false);
            })
            .assertNext(signal -> {
                assertFalse(signal.isInitiatedByClient());
                assertFalse(signal.isTransient());
            })
            .verifyComplete();
    }

    @Test
    void connectionDisposeFinishesReactor() {
        // Arrange
        final AtomicBoolean isOpen = new AtomicBoolean(true);
        // We want to ensure that the ReactorExecutor does not shutdown unexpectedly. There are still items to still
        // process.
        when(reactor.process()).thenAnswer(invocation -> isOpen.get());

        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE, EndpointState.CLOSED,
            EndpointState.CLOSED);
        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(connectionProtonJ);
        connectionHandler.onConnectionRemoteOpen(mock);

        final Event closeEvent = mock(Event.class);
        when(closeEvent.getConnection()).thenReturn(connectionProtonJ);

        final Event finalEvent = mock(Event.class);
        when(finalEvent.getConnection()).thenReturn(connectionProtonJ);

        // Act and Assert
        StepVerifier.create(connection.getShutdownSignals())
            .then(() -> {
                connection.getReactorConnection().subscribe();
                isOpen.set(false);
            })
            .assertNext(signal -> {
                assertFalse(signal.isInitiatedByClient());
                assertFalse(signal.isTransient());
            })
            .verifyComplete();
    }

    /**
     * Verifies if the ConnectionHandler transport fails, then we are unable to create the CBS node or sessions.
     */
    @Test
    @Disabled("This will be revisited")
    void cannotCreateResourcesOnFailure() {
        final Record reactorRecord = mock(Record.class);
        when(reactor.attachments()).thenReturn(reactorRecord);

        final Event event = mock(Event.class);
        final Transport transport = mock(Transport.class);
        final AmqpErrorCondition condition = AmqpErrorCondition.LINK_STOLEN;
        final ErrorCondition errorCondition = new ErrorCondition(Symbol.getSymbol(condition.getErrorCondition()),
            "Not found");
        final Consumer<Throwable> assertException = error -> {
            final AmqpException amqpException;
            if (error instanceof AmqpException) {
                amqpException = (AmqpException) error;
            } else if (error.getCause() instanceof AmqpException) {
                amqpException = (AmqpException) error.getCause();
            } else {
                amqpException = null;
                Assertions.fail("Exception was not the correct type: " + error);
            }

            assertTrue(amqpException.isTransient());
        };

        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connectionProtonJ);
        when(transport.getCondition()).thenReturn(errorCondition);
        when(connectionProtonJ.getHostname()).thenReturn(FULLY_QUALIFIED_NAMESPACE);
        when(connectionProtonJ.getRemoteContainer()).thenReturn("remote-container");
        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        connectionHandler.onTransportError(event);

        // Act & Assert
        StepVerifier.create(connection.getClaimsBasedSecurityNode())
            .expectErrorSatisfies(assertException)
            .verify();

        StepVerifier.create(connection.getReactorConnection())
            .expectErrorSatisfies(assertException)
            .verify();

        StepVerifier.create(connection.createRequestResponseChannel(SESSION_NAME, "test-link-name",
            "test-entity-path"))
            .expectError(IllegalStateException.class)
            .verify();

        StepVerifier.create(connection.createSession(SESSION_NAME))
            .expectErrorSatisfies(assertException)
            .verify();

        verify(transport, times(1)).unbind();
    }

    /**
     * Verifies that if we use the custom endpoint, it will return the correct properties.
     */
    @Test
    void setsPropertiesUsingCustomEndpoint() throws IOException {
        final String connectionId = "new-connection-id";
        final String hostname = "custom-endpoint.com";
        final int port = 10002;
        final ConnectionOptions connectionOptions = new ConnectionOptions(CREDENTIAL_INFO.getEndpoint().getHost(),
            tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP,
            new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, SCHEDULER, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT,
            CLIENT_VERSION, hostname, port);

        final ConnectionHandler connectionHandler = new ConnectionHandler(connectionId, connectionOptions,
            peerDetails);

        when(reactor.connectionToHost(hostname, port, connectionHandler)).thenReturn(connectionProtonJ);

        final Pipe pipe = Pipe.open();
        final ReactorDispatcher reactorDispatcher = new ReactorDispatcher(CONNECTION_ID, reactor, pipe);
        when(reactorProvider.getReactor()).thenReturn(reactor);
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(reactorProvider.createReactor(connectionId, connectionHandler.getMaxFrameSize())).thenReturn(reactor);

        when(reactorHandlerProvider.createConnectionHandler(CONNECTION_ID, connectionOptions))
            .thenReturn(connectionHandler);

        final SessionHandler sessionHandler = new SessionHandler(connectionId, FULLY_QUALIFIED_NAMESPACE, SESSION_NAME,
            reactorDispatcher, TEST_DURATION);
        when(reactorHandlerProvider.createSessionHandler(anyString(), anyString(), anyString(), any(Duration.class)))
            .thenReturn(sessionHandler);

        connection = new ReactorConnection(CONNECTION_ID, connectionOptions, reactorProvider, reactorHandlerProvider,
            tokenManager, messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST);
    }

    @Test
    void disposeAsync() throws IOException {
        // Arrange
        final ReactorProvider provider = mock(ReactorProvider.class);
        final ReactorDispatcher dispatcher = mock(ReactorDispatcher.class);
        final ReactorConnection connection2 = new ReactorConnection(CONNECTION_ID, connectionOptions, provider,
            reactorHandlerProvider, tokenManager, messageSerializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.FIRST);
        final AmqpShutdownSignal signal = new AmqpShutdownSignal(false, false, "Remove");

        when(provider.getReactorDispatcher()).thenReturn(dispatcher);

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(dispatcher).invoke(any(Runnable.class));

        connection2.getReactorConnection().subscribe();

        // Act and Assert
        StepVerifier.create(connection2.closeAsync(signal))
            .verifyComplete();

        assertTrue(connection2.isDisposed());

        StepVerifier.create(connection2.closeAsync(signal))
            .verifyComplete();
    }

    @Test
    void dispose() throws IOException {
        // Arrange
        final ReactorProvider provider = mock(ReactorProvider.class);
        final ReactorDispatcher dispatcher = mock(ReactorDispatcher.class);
        final ReactorConnection connection2 = new ReactorConnection(CONNECTION_ID, connectionOptions, provider,
            reactorHandlerProvider, tokenManager, messageSerializer, SenderSettleMode.SETTLED,
            ReceiverSettleMode.FIRST);

        when(provider.getReactorDispatcher()).thenReturn(dispatcher);

        doAnswer(invocation -> {
            final Runnable work = invocation.getArgument(0);
            work.run();
            return null;
        }).when(dispatcher).invoke(any(Runnable.class));

        connection2.getReactorConnection().subscribe();

        // Act and Assert
        connection2.dispose();

        assertTrue(connection2.isDisposed());

        connection2.dispose();
    }

    @Test
    @Disabled("This will be revisited")
    void createManagementNode() {
        final String linkName = "bar";
        final String entityPath = "foo";
        final Session session = mock(Session.class);
        final Record record = mock(Record.class);
        final Sender sender = mock(Sender.class);
        final Receiver receiver = mock(Receiver.class);

        doAnswer(invocationOnMock -> {
            System.out.println("Sleeping in reactor process method.");
            TimeUnit.SECONDS.sleep(10);
            return false;
        }).when(reactor).process();

        when(session.attachments()).thenReturn(record);
        when(session.sender(argThat(name -> name.equals("mgmt:sender")))).thenReturn(sender);
        when(session.receiver(argThat(name -> name.equals("mgmt:receiver")))).thenReturn(receiver);
        when(sender.attachments()).thenReturn(record);
        when(receiver.attachments()).thenReturn(record);

        when(connectionProtonJ.getRemoteState()).thenReturn(EndpointState.ACTIVE);
        when(connectionProtonJ.session()).thenReturn(session);

        final Event mock = mock(Event.class);
        when(mock.getConnection()).thenReturn(connectionProtonJ);
        connectionHandler.onConnectionRemoteOpen(mock);

        final TestPublisher<AmqpResponseCode> resultsPublisher = TestPublisher.createCold();
        resultsPublisher.next(AmqpResponseCode.ACCEPTED);

        final TokenManager manager = mock(TokenManager.class);
        when(manager.authorize()).thenReturn(Mono.just(Duration.ofMinutes(20).toMillis()));
        when(manager.getAuthorizationResults()).thenReturn(resultsPublisher.flux());

        when(tokenManager.getTokenManager(any(), any())).thenReturn(manager);

        final TestPublisher<EndpointState> sessionEndpoints = TestPublisher.createCold();
        sessionEndpoints.next(EndpointState.ACTIVE);

        final SessionHandler sessionHandler = mock(SessionHandler.class);
        when(sessionHandler.getEndpointStates()).thenReturn(sessionEndpoints.flux());
        when(reactorHandlerProvider.createSessionHandler(any(),
            argThat(path -> path.contains("mgmt") && path.contains(entityPath)), anyString(), any()))
            .thenReturn(sessionHandler);

        final SendLinkHandler linkHandler = new SendLinkHandler(CONNECTION_ID, FULLY_QUALIFIED_NAMESPACE, linkName,
            entityPath);
        when(reactorHandlerProvider.createSendLinkHandler(eq(CONNECTION_ID), eq(FULLY_QUALIFIED_NAMESPACE),
            argThat(path -> path.contains("mgmt") && path.contains(entityPath)), argThat(path -> path.contains("management"))))
            .thenReturn(linkHandler);
        when(reactorHandlerProvider.createSendLinkHandler(eq(CONNECTION_ID), eq(FULLY_QUALIFIED_NAMESPACE),
            argThat(path -> path.contains("cbs") && path.contains(entityPath)), argThat(path -> path.contains("cbs"))))
            .thenReturn(linkHandler);

        final ReceiveLinkHandler receiveLinkHandler = new ReceiveLinkHandler(CONNECTION_ID, FULLY_QUALIFIED_NAMESPACE,
            linkName, entityPath);
        when(reactorHandlerProvider.createReceiveLinkHandler(eq(CONNECTION_ID), eq(FULLY_QUALIFIED_NAMESPACE),
            argThat(path -> path.contains("mgmt") && path.contains(entityPath)), argThat(path -> path.contains("management"))))
            .thenReturn(receiveLinkHandler);
        when(reactorHandlerProvider.createReceiveLinkHandler(eq(CONNECTION_ID), eq(FULLY_QUALIFIED_NAMESPACE),
            argThat(path -> path.contains("cbs") && path.contains(entityPath)), argThat(path -> path.contains("cbs"))))
            .thenReturn(receiveLinkHandler);

        // Act and Assert
        StepVerifier.create(connection.getManagementNode(entityPath))
            .assertNext(node -> assertTrue(node instanceof ManagementChannel))
            .verifyComplete();
    }
}
