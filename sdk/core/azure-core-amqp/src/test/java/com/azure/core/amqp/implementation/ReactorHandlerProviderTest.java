// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsProxyConnectionHandler;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.impl.TransportImpl;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ReactorHandlerProvider}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates global ProxySelector")
public class ReactorHandlerProviderTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String FULLY_QUALIFIED_DOMAIN_NAME = "my-hostname.windows.com";
    private static final String HOSTNAME = "my.fake.hostname.com";
    private static final int PORT = 1003;
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER;

    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions().setHeaders(
        Arrays.asList(new Header("name", PRODUCT), new Header("version", CLIENT_VERSION)));


    @Mock
    private Reactor reactor;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Scheduler scheduler;
    @Mock
    private ProxySelector proxySelector;

    private ReactorHandlerProvider provider;
    private ProxySelector originalProxySelector;
    private AutoCloseable mocksCloseable;

    public static Stream<ProxyOptions> getProxyConfigurations() {
        return Stream.of(ProxyOptions.SYSTEM_DEFAULTS,
            new ProxyOptions(ProxyAuthenticationType.BASIC, null, "some username", "some password"),
            null
        );
    }

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        when(reactorProvider.createReactor(eq(CONNECTION_ID), anyInt())).thenReturn(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);

        provider = new ReactorHandlerProvider(reactorProvider);

        originalProxySelector = ProxySelector.getDefault();

        proxySelector = mock(ProxySelector.class, Mockito.CALLS_REAL_METHODS);
        ProxySelector.setDefault(proxySelector);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        ProxySelector.setDefault(originalProxySelector);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void constructorNull() {
        // Act
        assertThrows(NullPointerException.class, () -> new ReactorHandlerProvider(null));
    }

    @Test
    public void connectionHandlerNull() {
        // Arrange
        final ConnectionOptions connectionOptions = new ConnectionOptions("fqdn", tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope",
            AmqpTransportType.AMQP_WEB_SOCKETS, new AmqpRetryOptions(), null, scheduler, CLIENT_OPTIONS,
            VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        // Act
        assertThrows(NullPointerException.class,
            () -> provider.createConnectionHandler(null, connectionOptions));
        assertThrows(NullPointerException.class,
            () -> provider.createConnectionHandler(CONNECTION_ID, null));
    }

    public static Stream<Arguments> getHostnameAndPorts() {
        return Stream.of(
            Arguments.of(FULLY_QUALIFIED_DOMAIN_NAME, -1, FULLY_QUALIFIED_DOMAIN_NAME, ConnectionHandler.AMQPS_PORT),
            Arguments.of(HOSTNAME, PORT, HOSTNAME, PORT)
        );
    }

    @MethodSource("getHostnameAndPorts")
    @ParameterizedTest
    public void getsConnectionHandlerAMQP(String hostname, int port, String expectedHostname, int expectedPort) {
        // Act
        final ConnectionOptions connectionOptions = new ConnectionOptions(FULLY_QUALIFIED_DOMAIN_NAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP,
            new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT,
            CLIENT_VERSION, hostname, port);

        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Assert
        Assertions.assertNotNull(handler);
        Assertions.assertEquals(expectedHostname, handler.getHostname());
        Assertions.assertEquals(expectedPort, handler.getProtocolPort());
    }

    /**
     * Verify that if the user has not provided a proxy, and asks for websockets, we pass the correct handler.
     */
    @ParameterizedTest
    @MethodSource("getProxyConfigurations")
    public void getsConnectionHandlerWebSockets(ProxyOptions configuration) {
        // Act
        final ConnectionOptions connectionOptions = new ConnectionOptions(FULLY_QUALIFIED_DOMAIN_NAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), configuration, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Assert
        Assertions.assertTrue(handler instanceof WebSocketsConnectionHandler);
        Assertions.assertEquals(443, handler.getProtocolPort());
    }

    /**
     * Verify if user provides a proxy address, we return the correct proxy properties.
     */
    @Test
    public void getsConnectionHandlerProxy() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyOptions configuration = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME,
            PASSWORD);
        final String hostname = "foo.eventhubs.azure.com";
        final ConnectionOptions connectionOptions = new ConnectionOptions(hostname, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), configuration, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Assert
        Assertions.assertNotNull(handler);
        Assertions.assertTrue(handler instanceof WebSocketsProxyConnectionHandler);
        Assertions.assertEquals(address.getHostName(), handler.getHostname());
        Assertions.assertEquals(address.getPort(), handler.getProtocolPort());
    }

    public static Stream<Arguments> getsConnectionHandlerSystemProxy() {
        return Stream.of(
            Arguments.of("foo.eventhubs.azure.com", WebSocketsProxyConnectionHandler.HTTPS_PORT,
                PROXY_ADDRESS.getHostName(), PROXY_ADDRESS.getPort()),
            Arguments.of("foo.eventhubs.azure.com", 8882, "my-new2.proxy.com", 8888)
        );
    }

    /**
     * Verify that we use the system proxy.
     */
    @MethodSource
    @ParameterizedTest
    public void getsConnectionHandlerSystemProxy(String hostname, Integer port, String expectedHostname,
        int expectedPort) {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new2.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);

        final String fullyQualifiedDomainName = "foo.eventhubs.azure.com";
        final ConnectionOptions connectionOptions = new ConnectionOptions(fullyQualifiedDomainName, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), null, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION,
            hostname, port);

        when(proxySelector.select(any())).thenAnswer(invocation -> {
            final URI uri = invocation.getArgument(0);
            if (fullyQualifiedDomainName.equals(uri.getHost()) && uri.getPort() == WebSocketsConnectionHandler.HTTPS_PORT) {
                return Collections.singletonList(PROXY);
            }

            if (uri.getHost().equals(hostname) && uri.getPort() == port) {
                return Collections.singletonList(newProxy);
            }

            return Collections.emptyList();
        });

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Assert
        Assertions.assertNotNull(handler);
        Assertions.assertTrue(handler instanceof WebSocketsProxyConnectionHandler);
        Assertions.assertEquals(expectedHostname, handler.getHostname());
        Assertions.assertEquals(expectedPort, handler.getProtocolPort());
    }

    /**
     * Verifies that if no proxy configuration is set, then it will use the system configured proxy.
     */
    @ParameterizedTest
    @MethodSource("getProxyConfigurations")
    public void noProxySelected(ProxyOptions configuration) {
        // Arrange
        final String hostname = "foo.eventhubs.azure.com";

        // The default port used for the first ConnectionOptions constructor is the default HTTPS_PORT.
        when(proxySelector.select(argThat(u -> u.getHost().equals(hostname)
            && u.getPort() == WebSocketsConnectionHandler.HTTPS_PORT)))
            .thenReturn(Collections.singletonList(PROXY));

        final ConnectionOptions connectionOptions = new ConnectionOptions(hostname, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), configuration, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT, CLIENT_VERSION);

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Act and Assert
        Assertions.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assertions.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());
    }

    /**
     * Verify that when there are no legal proxy addresses, false is returned.
     */
    @Test
    public void shouldUseProxyNoLegalProxyAddress() {
        // Arrange
        final String hostname = "foo.eventhubs.azure.com";
        final int port = 1000;

        when(proxySelector.select(argThat(u -> u.getHost().equals(hostname) && u.getPort() == port)))
            .thenReturn(Collections.emptyList());

        // Act and Assert
        Assertions.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(hostname, port));
    }

    @Test
    public void shouldUseProxyHostNull() {
        assertThrows(NullPointerException.class, () -> WebSocketsProxyConnectionHandler.shouldUseProxy(null, 111));
    }

    @Test
    public void shouldUseProxyNullProxySelector() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        final int port = 1000;

        ProxySelector.setDefault(null);

        // Act and Assert
        Assertions.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(host, port));
    }

    /**
     * Verify that when we use a proxy, the SSL peer is the name of the AMQP messaae broker.
     */
    @Test
    public void correctPeerDetailsProxy() {
        // Arrange
        final String anotherFakeHostname = "hostname.fake";
        final ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC, PROXY, USERNAME, PASSWORD);
        final ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), proxyOptions, scheduler, CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME,
            PRODUCT, CLIENT_VERSION);

        final Connection connection = mock(Connection.class);
        when(connection.getRemoteState()).thenReturn(EndpointState.UNINITIALIZED);

        final TransportImpl transport = mock(TransportImpl.class);

        final Event event = mock(Event.class);
        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connection);
        when(connection.getHostname()).thenReturn(anotherFakeHostname);

        final ConnectionHandler connectionHandler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Act
        connectionHandler.onConnectionBound(event);

        // Assert
        verify(transport).ssl(
            argThat(sslDomain -> sslDomain.getMode() == SslDomain.Mode.CLIENT),
            argThat(peerDetails -> HOSTNAME.equals(peerDetails.getHostname())
                && WebSocketsConnectionHandler.HTTPS_PORT == peerDetails.getPort()));
    }

    /**
     * Verify that when we use a custom endpoint, the peer details are for that custom endpoint.
     */
    @Test
    public void correctPeerDetailsCustomEndpoint() throws MalformedURLException {
        // Arrange
        final URL customEndpoint = new URL("https://myappservice.windows.net");
        final String anotherFakeHostname = "hostname.fake";
        final ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS,
            SslDomain.VerifyMode.VERIFY_PEER_NAME, PRODUCT, CLIENT_VERSION, customEndpoint.getHost(),
            customEndpoint.getDefaultPort());

        final Connection connection = mock(Connection.class);
        when(connection.getRemoteState()).thenReturn(EndpointState.UNINITIALIZED);

        final TransportImpl transport = mock(TransportImpl.class);

        final Event event = mock(Event.class);
        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connection);
        when(connection.getHostname()).thenReturn(anotherFakeHostname);

        final ConnectionHandler connectionHandler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Act
        connectionHandler.onConnectionBound(event);

        // Assert
        verify(transport).ssl(
            any(SslDomain.class),
            argThat(peerDetails -> customEndpoint.getHost().equals(peerDetails.getHostname())
                && customEndpoint.getDefaultPort() == peerDetails.getPort()));
    }

    /**
     * Verify that in the normal case, the peer details are for the AMQP message broker.
     */
    @EnumSource(AmqpTransportType.class)
    @ParameterizedTest
    public void correctPeerDetails(AmqpTransportType transportType) {
        // Arrange
        final String anotherFakeHostname = "hostname.fake";
        final ConnectionOptions connectionOptions = new ConnectionOptions(HOSTNAME, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", transportType, new AmqpRetryOptions(),
            ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, SslDomain.VerifyMode.VERIFY_PEER_NAME, PRODUCT,
            CLIENT_VERSION);

        final Connection connection = mock(Connection.class);
        when(connection.getRemoteState()).thenReturn(EndpointState.UNINITIALIZED);

        final TransportImpl transport = mock(TransportImpl.class);

        final Event event = mock(Event.class);
        when(event.getTransport()).thenReturn(transport);
        when(event.getConnection()).thenReturn(connection);
        when(connection.getHostname()).thenReturn(anotherFakeHostname);

        final ConnectionHandler connectionHandler = provider.createConnectionHandler(CONNECTION_ID, connectionOptions);

        // Act
        connectionHandler.onConnectionBound(event);

        // Assert
        verify(transport).ssl(
            any(SslDomain.class),
            argThat(peerDetails -> {
                if (!HOSTNAME.equals(peerDetails.getHostname())) {
                    return false;
                }

                if (transportType == AmqpTransportType.AMQP) {
                    return peerDetails.getPort() == ConnectionHandler.AMQPS_PORT;
                } else {
                    return peerDetails.getPort() == WebSocketsConnectionHandler.HTTPS_PORT;
                }
            }));
    }
}
