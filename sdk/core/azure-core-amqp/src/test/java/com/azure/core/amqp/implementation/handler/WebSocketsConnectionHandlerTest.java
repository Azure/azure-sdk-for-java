// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.implementation.handler.ConnectionHandler.FRAMEWORK;
import static com.azure.core.amqp.implementation.handler.ConnectionHandler.PLATFORM;
import static com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler.HTTPS_PORT;
import static com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler.MAX_FRAME_SIZE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebSocketsConnectionHandlerTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String CONNECTION_ID = "some-random-id";
    private static final String HOSTNAME = "hostname-random";

    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;
    private static final String PRODUCT = "my-product";
    private static final String CLIENT_VERSION = "1.5.1-alpha";
    private final SslPeerDetails peerDetails = Proton.sslPeerDetails(HOSTNAME, 2919);

    private WebSocketsConnectionHandler handler;
    private ConnectionOptions connectionOptions;

    @Captor
    ArgumentCaptor<Map<Symbol, Object>> argumentCaptor;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Scheduler scheduler;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        this.connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "authorization-scope",
            AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS,
            scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        this.handler = new WebSocketsConnectionHandler(CONNECTION_ID, connectionOptions, peerDetails);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (handler != null) {
            handler.close();
        }

        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void createHandler() {
        // Arrange
        final Map<String, String> expected = new HashMap<>();
        expected.put(PLATFORM.toString(), ClientConstants.PLATFORM_INFO);
        expected.put(FRAMEWORK.toString(), ClientConstants.FRAMEWORK_INFO);

        // Assert
        Assertions.assertEquals(connectionOptions.getHostname(), handler.getHostname());
        Assertions.assertEquals(MAX_FRAME_SIZE, handler.getMaxFrameSize());
        Assertions.assertEquals(HTTPS_PORT, connectionOptions.getPort());
        Assertions.assertEquals(HTTPS_PORT, handler.getProtocolPort());

        final Map<String, Object> properties = handler.getConnectionProperties();
        expected.forEach((key, value) -> {
            Assertions.assertTrue(properties.containsKey(key));

            final Object actual = properties.get(key);

            Assertions.assertTrue(actual instanceof String);
            Assertions.assertEquals(value, actual);
        });
    }

    @Test
    public void addsSslLayer() {
        // Arrange
        final TransportInternal transport = mock(TransportInternal.class);
        final Connection connection = mock(Connection.class);
        when(connection.getRemoteState()).thenReturn(EndpointState.CLOSED);

        final Event event = mock(Event.class);
        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connection);

        // Act
        handler.onConnectionBound(event);

        StepVerifier.create(handler.getEndpointStates())
            .expectNext(EndpointState.CLOSED)
            .then(handler::close)
            .verifyComplete();

        // Assert
        verify(transport).ssl(any(SslDomain.class), any(SslPeerDetails.class));
    }

    @Test
    public void onConnectionInit() {
        // Arrange
        final Map<String, Object> expectedProperties = new HashMap<>(handler.getConnectionProperties());
        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        when(event.getConnection()).thenReturn(connection);

        // Act
        handler.onConnectionInit(event);

        // Assert
        verify(connection).setHostname(HOSTNAME);
        verify(connection).setContainer(CONNECTION_ID);
        verify(connection).open();

        verify(connection).setProperties(argumentCaptor.capture());
        Map<Symbol, Object> actualProperties = argumentCaptor.getValue();
        Assertions.assertEquals(expectedProperties.size(), actualProperties.size());
        expectedProperties.forEach((key, value) -> {
            final Symbol symbol = Symbol.getSymbol(key);
            final Object removed = actualProperties.remove(symbol);
            Assertions.assertNotNull(removed);

            final String expected = String.valueOf(value);
            final String actual = String.valueOf(removed);
            Assertions.assertEquals(expected, actual);
        });
        Assertions.assertTrue(actualProperties.isEmpty());
    }

    /**
     * verifies that with a different connection endpoint, we still use the correct remote host.
     */
    @Test
    public void onConnectionInitDifferentEndpoint() {
        // Arrange
        final Map<String, Object> expectedProperties = new HashMap<>(handler.getConnectionProperties());
        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        when(event.getConnection()).thenReturn(connection);

        final String fullyQualifiedNamespace = "foo.servicebus.windows.net";
        final String customEndpoint = "internal.service.net";
        final int port = 9888;

        final ConnectionOptions connectionOptions = new ConnectionOptions(fullyQualifiedNamespace, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "authorization-scope",
            AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler,
            CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION, customEndpoint, port);

        try (WebSocketsConnectionHandler handler = new WebSocketsConnectionHandler(CONNECTION_ID, connectionOptions,
            peerDetails)) {

            // Act
            handler.onConnectionInit(event);

            // Assert
            verify(connection).setHostname(connectionOptions.getFullyQualifiedNamespace());
            verify(connection).setContainer(CONNECTION_ID);
            verify(connection).open();

            verify(connection).setProperties(argumentCaptor.capture());
            Map<Symbol, Object> actualProperties = argumentCaptor.getValue();
            Assertions.assertEquals(expectedProperties.size(), actualProperties.size());
            expectedProperties.forEach((key, value) -> {
                final Symbol symbol = Symbol.getSymbol(key);
                final Object removed = actualProperties.remove(symbol);
                Assertions.assertNotNull(removed);

                final String expected = String.valueOf(value);
                final String actual = String.valueOf(removed);
                Assertions.assertEquals(expected, actual);
            });
            Assertions.assertTrue(actualProperties.isEmpty());

            Assertions.assertEquals(port, connectionOptions.getPort());
            Assertions.assertEquals(port, handler.getProtocolPort());
        }
    }
}
