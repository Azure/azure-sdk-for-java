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
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ReactorHandlerProvider}.
 */
public class ReactorHandlerProviderTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String FULLY_QUALIFIED_DOMAIN_NAME = "my-hostname.windows.com";
    private static final String HOSTNAME = "my fake hostname";
    private static final int PORT = 1003;
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER;
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();

    @Mock
    private Reactor reactor;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private Scheduler scheduler;

    private ReactorHandlerProvider provider;
    private ProxySelector originalProxySelector;
    private ProxySelector proxySelector;

    public static Stream<ProxyOptions> getProxyConfigurations() {
        return Stream.of(ProxyOptions.SYSTEM_DEFAULTS,
            new ProxyOptions(ProxyAuthenticationType.BASIC, null, "some username", "some password"),
            null
        );
    }

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(reactorProvider.createReactor(eq(CONNECTION_ID), anyInt())).thenReturn(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);

        provider = new ReactorHandlerProvider(reactorProvider);

        originalProxySelector = ProxySelector.getDefault();

        proxySelector = mock(ProxySelector.class, Mockito.CALLS_REAL_METHODS);
        ProxySelector.setDefault(proxySelector);
    }

    @AfterEach
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        ProxySelector.setDefault(originalProxySelector);
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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), null, scheduler, CLIENT_OPTIONS, VERIFY_MODE);

        // Act
        assertThrows(NullPointerException.class,
            () -> provider.createConnectionHandler(null, HOSTNAME, CLIENT_VERSION, connectionOptions));
        assertThrows(NullPointerException.class,
            () -> provider.createConnectionHandler(CONNECTION_ID, null, CLIENT_VERSION, connectionOptions));
        assertThrows(NullPointerException.class,
            () -> provider.createConnectionHandler(CONNECTION_ID, HOSTNAME, null, connectionOptions));
        assertThrows(NullPointerException.class,
            () -> provider.createConnectionHandler(CONNECTION_ID, HOSTNAME, CLIENT_VERSION, null));
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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP,
            new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, VERIFY_MODE, hostname,
            port);

        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, PRODUCT, CLIENT_VERSION,
            connectionOptions);

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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), configuration, scheduler, CLIENT_OPTIONS, VERIFY_MODE);

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, PRODUCT, CLIENT_VERSION,
            connectionOptions);

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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), configuration, scheduler, CLIENT_OPTIONS, VERIFY_MODE);

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, PRODUCT, CLIENT_VERSION,
            connectionOptions);

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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), null, scheduler, CLIENT_OPTIONS, VERIFY_MODE, hostname, port);

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
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, PRODUCT, CLIENT_VERSION,
            connectionOptions);

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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP_WEB_SOCKETS,
            new AmqpRetryOptions(), configuration, scheduler, CLIENT_OPTIONS, VERIFY_MODE);

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, PRODUCT, CLIENT_VERSION,
            connectionOptions);

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
}
