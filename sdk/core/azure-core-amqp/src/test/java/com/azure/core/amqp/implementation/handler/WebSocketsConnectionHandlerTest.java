// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpErrorCode;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.metrics.TestMeasurement;
import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.ClientOptions;
import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.impl.TransportImpl;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.core.amqp.implementation.handler.ConnectionHandler.FRAMEWORK;
import static com.azure.core.amqp.implementation.handler.ConnectionHandler.PLATFORM;
import static com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler.HTTPS_PORT;
import static com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler.MAX_FRAME_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebSocketsConnectionHandlerTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String CONNECTION_ID = "some-random-id";
    private static final String HOSTNAME = "hostname-random";
    private static final String CUSTOM_ENDPOINT_HOSTNAME = "custom-hostname-random";
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

        this.connectionOptions
            = new ConnectionOptions(HOSTNAME, tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE,
                "authorization-scope", AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(),
                ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        this.handler = new WebSocketsConnectionHandler(CONNECTION_ID, connectionOptions, peerDetails,
            AmqpMetricsProvider.noop());
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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "authorization-scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT,
            CLIENT_VERSION, customEndpoint, port);

        try (WebSocketsConnectionHandler handler = new WebSocketsConnectionHandler(CONNECTION_ID, connectionOptions,
            peerDetails, AmqpMetricsProvider.noop())) {

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

    @Test
    public void websocketConfigureUsesFqdnAsHostname() {
        try (MockedConstruction<WebSocketImpl> mockConstruction = mockConstruction(WebSocketImpl.class)) {
            handler.addTransportLayers(mock(Event.class, Mockito.CALLS_REAL_METHODS),
                mock(TransportImpl.class, Mockito.CALLS_REAL_METHODS));

            final List<WebSocketImpl> constructed = mockConstruction.constructed();
            assertEquals(1, constructed.size());
            // The WebSocketImpl object constructed inside addTransportLayer method.
            final WebSocketImpl webSocketImpl = constructed.get(0);
            final String expectedHostName = HOSTNAME;
            verify(webSocketImpl).configure(eq(expectedHostName), eq("/$servicebus/websocket"), eq(""), eq(0),
                eq("AMQPWSB10"), eq(null), eq(null));
        }
    }

    @Test
    public void websocketConfigureUsesCustomEndpointHostnameAsHostname() {
        final String customEndpointHostname = "order-events.contoso.com";
        final ConnectionOptions connectionOptionsWithCustomEndpoint
            = new ConnectionOptions(HOSTNAME, tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope",
                AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler,
                CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION, customEndpointHostname, 200);

        try (WebSocketsConnectionHandler handler = new WebSocketsConnectionHandler(CONNECTION_ID,
            connectionOptionsWithCustomEndpoint, peerDetails, AmqpMetricsProvider.noop())) {
            try (MockedConstruction<WebSocketImpl> mockConstruction = mockConstruction(WebSocketImpl.class)) {
                handler.addTransportLayers(mock(Event.class, Mockito.CALLS_REAL_METHODS),
                    mock(TransportImpl.class, Mockito.CALLS_REAL_METHODS));

                final List<WebSocketImpl> constructed = mockConstruction.constructed();
                assertEquals(1, constructed.size());
                // The WebSocketImpl object constructed inside addTransportLayer method.
                final WebSocketImpl webSocketImpl = constructed.get(0);
                verify(webSocketImpl).configure(eq(customEndpointHostname), eq("/$servicebus/websocket"), eq(""), eq(0),
                    eq("AMQPWSB10"), eq(null), eq(null));
            }
        }
    }

    @Test
    void onConnectionCloseMetrics() {
        // Arrange
        final ErrorCondition errorCondition
            = new ErrorCondition(Symbol.valueOf(AmqpErrorCode.SERVER_BUSY_ERROR.toString()), "");
        Event openEvent = mock(Event.class);
        Event closeEventWithError = mock(Event.class);
        Event closeEventNoError = mock(Event.class);

        Connection connectionWithError = mock(Connection.class);
        when(openEvent.getConnection()).thenReturn(connectionWithError);
        when(closeEventWithError.getConnection()).thenReturn(connectionWithError);

        Connection connectionNoError = mock(Connection.class);
        when(openEvent.getConnection()).thenReturn(connectionNoError);
        when(closeEventNoError.getConnection()).thenReturn(connectionNoError);

        when(connectionWithError.getCondition()).thenReturn(errorCondition);
        when(connectionWithError.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        when(connectionNoError.getCondition()).thenReturn(new ErrorCondition(null, ""));
        when(connectionNoError.getRemoteState()).thenReturn(EndpointState.ACTIVE);

        TestMeter meter = new TestMeter();
        WebSocketsConnectionHandler handlerWithMetrics = new WebSocketsConnectionHandler(CONNECTION_ID,
            connectionOptions, peerDetails, new AmqpMetricsProvider(meter, HOSTNAME, null));

        handlerWithMetrics.onConnectionInit(openEvent);
        handlerWithMetrics.onConnectionInit(openEvent);
        handlerWithMetrics.onConnectionFinal(closeEventWithError);
        handlerWithMetrics.onConnectionFinal(closeEventNoError);

        // Assert
        List<TestMeasurement<Long>> closedConnections
            = meter.getCounters().get("messaging.az.amqp.client.connections.closed").getMeasurements();
        assertEquals(2, closedConnections.size());

        assertEquals(1, closedConnections.get(0).getValue());
        assertEquals(1, closedConnections.get(1).getValue());

        assertEquals(HOSTNAME, closedConnections.get(0).getAttributes().get(ClientConstants.HOSTNAME_KEY));
        assertEquals("com.microsoft:server-busy",
            closedConnections.get(0).getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));
        assertEquals("ok", closedConnections.get(1).getAttributes().get(ClientConstants.ERROR_CONDITION_KEY));
    }
}
