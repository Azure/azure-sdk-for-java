// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsProxyConnectionHandler;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ReactorHandlerProviderTest {
    private static final String CONNECTION_ID = "test-connection-id";
    private static final String HOSTNAME = "my-hostname.windows.com";
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";

    @Mock
    private Reactor reactor;
    @Mock
    private ReactorProvider reactorProvider;

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
    public void getsConnectionHandlerAMQP() {
        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, HOSTNAME, AmqpTransportType.AMQP, null);

        // Assert
        Assertions.assertNotNull(handler);
        Assertions.assertEquals(5671, handler.getProtocolPort());
    }

    /**
     * Verify that if the user has not provided a proxy, and asks for websockets, we pass the correct handler.
     */
    @ParameterizedTest
    @MethodSource("getProxyConfigurations")
    public void getsConnectionHandlerWebSockets(ProxyOptions configuration) {
        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, HOSTNAME,
            AmqpTransportType.AMQP_WEB_SOCKETS, configuration);

        // Assert
        Assertions.assertNotNull(handler);
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
        final ProxyOptions configuration = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);
        final String hostname = "foo.eventhubs.azure.com";

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, hostname,
            AmqpTransportType.AMQP_WEB_SOCKETS, configuration);

        // Assert
        Assertions.assertNotNull(handler);
        Assertions.assertTrue(handler instanceof WebSocketsProxyConnectionHandler);
        Assertions.assertEquals(address.getHostName(), handler.getHostname());
        Assertions.assertEquals(address.getPort(), handler.getProtocolPort());

        verifyZeroInteractions(proxySelector);
    }

    /**
     * Verifies that if no proxy configuration is set, then it will use the system configured proxy.
     */
    @ParameterizedTest
    @MethodSource("getProxyConfigurations")
    public void noProxySelected(ProxyOptions configuration) {
        // Arrange
        final String hostname = "foo.eventhubs.azure.com";
        when(proxySelector.select(argThat(u -> u.getHost().equals(hostname))))
            .thenReturn(Collections.singletonList(PROXY));

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, hostname,
            AmqpTransportType.AMQP_WEB_SOCKETS, configuration);

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
        final String host = "foo.eventhubs.azure.com";

        when(proxySelector.select(argThat(u -> u.getHost().equals(host))))
            .thenReturn(Collections.emptyList());

        // Act and Assert
        Assertions.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(host));
    }

    @Test
    public void shouldUseProxyHostNull() {
        assertThrows(NullPointerException.class, () -> WebSocketsProxyConnectionHandler.shouldUseProxy(null));
    }

    @Test
    public void shouldUseProxyNullProxySelector() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        ProxySelector.setDefault(null);

        // Act and Assert
        Assertions.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(host));
    }
}
