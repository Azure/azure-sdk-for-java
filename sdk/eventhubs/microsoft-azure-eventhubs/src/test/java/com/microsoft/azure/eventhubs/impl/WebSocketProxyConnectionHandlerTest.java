// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.ProxyConfiguration;
import com.microsoft.azure.eventhubs.ProxyConfiguration.ProxyAuthenticationType;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class WebSocketProxyConnectionHandlerTest {
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final String CONNECTION_ID = "web-socket-connection-id";
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;

    private ProxySelector originalProxySelector;
    private ProxySelector proxySelector;

    /**
     * Creates mocks of the proxy selector and authenticator and sets them as defaults.
     */
    @Before
    public void setup() {
        originalProxySelector = ProxySelector.getDefault();

        proxySelector = mock(ProxySelector.class, Mockito.CALLS_REAL_METHODS);
        ProxySelector.setDefault(proxySelector);
    }

    @After
    public void teardown() {
        ProxySelector.setDefault(originalProxySelector);
    }

    /**
     * Verifies that if no proxy configuration is set, then it will use the system configured proxy.
     */
    @Test
    public void noProxySelected() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        final AmqpConnection connection = mock(AmqpConnection.class);

        when(connection.getHostName()).thenReturn(host);
        when(proxySelector.select(argThat(u -> u.getHost().equals(host))))
            .thenReturn(Collections.singletonList(PROXY));

        final WebSocketProxyConnectionHandler handler = new WebSocketProxyConnectionHandler(connection, CONNECTION_ID,
            VERIFY_MODE, null);

        // Act and Assert
        Assert.assertEquals(PROXY_ADDRESS.getHostName(), handler.getRemoteHostName());
        Assert.assertEquals(PROXY_ADDRESS.getPort(), handler.getRemotePort());
    }

    /**
     * Verifies that if we use the system proxy configuration, then it will use the system configured proxy.
     */
    @Test
    public void systemProxyConfigurationSelected() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        final AmqpConnection connection = mock(AmqpConnection.class);

        when(connection.getHostName()).thenReturn(host);
        when(proxySelector.select(argThat(u -> u.getHost().equals(host))))
            .thenReturn(Collections.singletonList(PROXY));

        final WebSocketProxyConnectionHandler handler = new WebSocketProxyConnectionHandler(connection, CONNECTION_ID,
            VERIFY_MODE, ProxyConfiguration.SYSTEM_DEFAULTS);

        // Act and Assert
        Assert.assertEquals(PROXY_ADDRESS.getHostName(), handler.getRemoteHostName());
        Assert.assertEquals(PROXY_ADDRESS.getPort(), handler.getRemotePort());

        verify(proxySelector, times(2))
            .select(argThat(u -> u.getHost().equals(host)));
    }

    /**
     * Verifies that if we use the proxy configuration.
     */
    @Test
    public void proxyConfigurationSelected() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyConfiguration configuration = new ProxyConfiguration(ProxyAuthenticationType.BASIC, newProxy,
            USERNAME, PASSWORD);
        final String host = "foo.eventhubs.azure.com";
        final AmqpConnection connection = mock(AmqpConnection.class);

        when(connection.getHostName()).thenReturn(host);
        when(proxySelector.select(any())).thenReturn(Collections.singletonList(PROXY));

        final WebSocketProxyConnectionHandler handler = new WebSocketProxyConnectionHandler(connection, CONNECTION_ID,
            VERIFY_MODE, configuration);

        // Act and Assert
        Assert.assertEquals(address.getHostName(), handler.getRemoteHostName());
        Assert.assertEquals(address.getPort(), handler.getRemotePort());

        verifyNoInteractions(proxySelector);
    }

    @Test
    public void shouldUseProxyNoLegalProxyAddress() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";

        when(proxySelector.select(argThat(u -> u.getHost().equals(host))))
            .thenReturn(Collections.emptyList());

        // Act and Assert
        Assert.assertFalse(WebSocketProxyConnectionHandler.shouldUseProxy(host));
    }

    @Test(expected = NullPointerException.class)
    public void shouldUseProxyHostNull() {
        WebSocketProxyConnectionHandler.shouldUseProxy(null);
    }

    @Test
    public void shouldUseProxyNullProxySelector() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        ProxySelector.setDefault(null);

        // Act and Assert
        Assert.assertFalse(WebSocketProxyConnectionHandler.shouldUseProxy(host));
    }
}
