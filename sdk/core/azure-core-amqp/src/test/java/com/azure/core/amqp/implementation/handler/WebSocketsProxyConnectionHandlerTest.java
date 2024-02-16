// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
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
import com.azure.core.util.Header;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.impl.TransportImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates the global ProxySelector")
public class WebSocketsProxyConnectionHandlerTest {
    private static final String CONNECTION_ID = "some-connection-id";
    private static final String HOSTNAME = "event-hubs.windows.core.net";
    private static final int AMQP_PORT = 5671;
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final ProxyOptions PROXY_OPTIONS
        = new ProxyOptions(ProxyAuthenticationType.DIGEST, PROXY, USERNAME, PASSWORD);
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final List<Header> HEADER_LIST = Collections.singletonList(new Header("foo-bar", "some-values"));
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions().setHeaders(HEADER_LIST);

    private final SslPeerDetails peerDetails = Proton.sslPeerDetails(HOSTNAME, 2192);

    private ProxySelector originalProxySelector;

    @Mock
    private ProxySelector proxySelector;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Scheduler scheduler;

    private ConnectionOptions connectionOptions;
    private WebSocketsProxyConnectionHandler handler;
    private AutoCloseable mocksCloseable;

    /**
     * Creates mocks of the proxy selector and authenticator and sets them as defaults.
     */
    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        this.connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP, new AmqpRetryOptions(),
            ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        this.originalProxySelector = ProxySelector.getDefault();
        this.proxySelector = mock(ProxySelector.class, Mockito.CALLS_REAL_METHODS);
        ProxySelector.setDefault(proxySelector);
    }

    @AfterEach
    public void teardown() throws Exception {
        if (handler != null) {
            handler.close();
        }

        ProxySelector.setDefault(originalProxySelector);
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void constructorNull() {
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(null, connectionOptions,
            PROXY_OPTIONS, peerDetails, AmqpMetricsProvider.noop()));
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(CONNECTION_ID, null,
            PROXY_OPTIONS, peerDetails, AmqpMetricsProvider.noop()));
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(CONNECTION_ID,
            connectionOptions, PROXY_OPTIONS, null, AmqpMetricsProvider.noop()));
    }

    /**
     * Verifies that if no proxy configuration is set, then it will use the system configured proxy.
     */
    @Test
    public void noProxySelected() {
        // Arrange
        when(proxySelector.select(argThat(u -> u.getHost().equals(HOSTNAME))))
            .thenReturn(Collections.singletonList(PROXY));

        this.handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, connectionOptions, PROXY_OPTIONS,
            peerDetails, AmqpMetricsProvider.noop());

        // Act and Assert
        Assertions.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assertions.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());

        handler.close();
    }

    /**
     * Verifies that if we use the system proxy configuration, then it will use the system configured proxy.
     */
    @Test
    public void systemProxyConfigurationSelected() {
        // Arrange
        when(proxySelector.select(argThat(u -> u.getHost().equals(HOSTNAME))))
            .thenReturn(Collections.singletonList(PROXY));

        this.handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, connectionOptions,
            ProxyOptions.SYSTEM_DEFAULTS, peerDetails, AmqpMetricsProvider.noop());

        // Act and Assert
        Assertions.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assertions.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());

        verify(proxySelector).select(argThat(u -> u.getHost().equals(HOSTNAME)));
    }

    /**
     * Verifies that if we use the proxy configuration.
     */
    @Test
    public void proxyConfigurationSelected() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);

        when(proxySelector.select(any())).thenReturn(Collections.singletonList(PROXY));

        this.handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, connectionOptions, proxyOptions, peerDetails,
            AmqpMetricsProvider.noop());

        // Act and Assert
        Assertions.assertEquals(address.getHostName(), handler.getHostname());
        Assertions.assertEquals(address.getPort(), handler.getProtocolPort());

        verifyNoInteractions(proxySelector);
    }

    /**
     * Verifies that the hostname:port for Proxy CONNECT created from
     * the FQDN host field in {@link ConnectionOptions}.
     */
    @Test
    public void proxyConfigureConnectHostnameAndPortDerivesFromFqdn() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);

        this.handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, connectionOptions, proxyOptions, peerDetails,
            AmqpMetricsProvider.noop());

        // Act and Assert
        try (MockedConstruction<ProxyImpl> mockConstruction = mockConstruction(ProxyImpl.class)) {
            this.handler.addTransportLayers(mock(Event.class, Mockito.CALLS_REAL_METHODS),
                mock(TransportImpl.class, Mockito.CALLS_REAL_METHODS));

            final List<ProxyImpl> constructed = mockConstruction.constructed();
            assertEquals(1, constructed.size());
            // The ProxyImpl object constructed inside addTransportLayer method.
            final ProxyImpl proxyImpl = constructed.get(0);
            final String expectedConnectHostnameAndPort = HOSTNAME + ":" + AMQP_PORT;
            verify(proxyImpl).configure(eq(expectedConnectHostnameAndPort), any(), any(), any());
        }
    }

    /**
     * Verifies that the hostname:port for Proxy CONNECT created from
     * the Custom host fields in {@link ConnectionOptions}.
     */
    @Test
    public void proxyConfigureConnectHostnameAndPortDerivesFromCustomEndpoint() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);
        final String customEndpointHostname = "order-events.contoso.com";
        final int customEndpointPort = 200;

        final ConnectionOptions connectionOptionsWithCustomEndpoint
            = new ConnectionOptions(HOSTNAME, tokenCredential, CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope",
                AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler,
                CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION, customEndpointHostname, customEndpointPort);

        this.handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, connectionOptionsWithCustomEndpoint,
            proxyOptions, peerDetails, AmqpMetricsProvider.noop());

        // Act and Assert
        try (MockedConstruction<ProxyImpl> mockConstruction = mockConstruction(ProxyImpl.class)) {
            this.handler.addTransportLayers(mock(Event.class, Mockito.CALLS_REAL_METHODS),
                mock(TransportImpl.class, Mockito.CALLS_REAL_METHODS));

            final List<ProxyImpl> constructed = mockConstruction.constructed();
            assertEquals(1, constructed.size());
            // The ProxyImpl object constructed inside addTransportLayer method.
            final ProxyImpl proxyImpl = constructed.get(0);
            final String expectedConnectHostnameAndPort = customEndpointHostname + ":" + customEndpointPort;
            verify(proxyImpl).configure(eq(expectedConnectHostnameAndPort), any(), any(), any());
        }
    }

    @Test
    public void shouldUseProxyNoLegalProxyAddress() {
        // Arrange
        final String hostname = "foo.eventhubs.azure.com";
        final int port = 10000;

        when(proxySelector.select(argThat(u -> u.getHost().equals(hostname) && u.getPort() == port)))
            .thenReturn(Collections.emptyList());

        // Act and Assert
        Assertions.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(hostname, port));
    }

    @Test
    public void shouldUseProxyHostNull() {
        assertThrows(NullPointerException.class, () -> WebSocketsProxyConnectionHandler.shouldUseProxy(null, 1000));
    }

    @Test
    public void shouldUseProxyNullProxySelector() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        final int port = 2000;
        ProxySelector.setDefault(null);

        // Act and Assert
        Assertions.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(host, port));
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
        WebSocketsProxyConnectionHandler handlerWithMetrics = new WebSocketsProxyConnectionHandler(CONNECTION_ID,
            connectionOptions, PROXY_OPTIONS, peerDetails, new AmqpMetricsProvider(meter, HOSTNAME, null));

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
