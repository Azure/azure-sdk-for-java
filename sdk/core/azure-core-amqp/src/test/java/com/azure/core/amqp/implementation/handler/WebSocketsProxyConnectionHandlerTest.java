// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.models.ProxyAuthenticationType;
import com.azure.core.amqp.models.ProxyConfiguration;
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class WebSocketsProxyConnectionHandlerTest {
    private static final String CONNECTION_ID = "some-connection-id";
    private static final String HOSTNAME = "event-hubs.windows.core.net";
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final ProxyConfiguration PROXY_CONFIGURATION =
        new ProxyConfiguration(ProxyAuthenticationType.DIGEST, PROXY, USERNAME, PASSWORD);

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

    @Test(expected = NullPointerException.class)
    public void nullProxyConfiguration() {
        new WebSocketsProxyConnectionHandler(CONNECTION_ID, HOSTNAME, null);
    }

    @Test(expected = NullPointerException.class)
    public void nullHostname() {
        new WebSocketsProxyConnectionHandler(CONNECTION_ID, null, PROXY_CONFIGURATION);
    }

    /**
     * Verifies that if no proxy configuration is set, then it will use the system configured proxy.
     */
    @Test
    public void noProxySelected() {
        // Arrange
        when(proxySelector.select(argThat(u -> u.getHost().equals(HOSTNAME))))
            .thenReturn(Collections.singletonList(PROXY));

        final WebSocketsProxyConnectionHandler handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, HOSTNAME,
            PROXY_CONFIGURATION);

        // Act and Assert
        Assert.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assert.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());
    }

    /**
     * Verifies that if we use the system proxy configuration, then it will use the system configured proxy.
     */
    @Test
    public void systemProxyConfigurationSelected() {
        // Arrange
        when(proxySelector.select(argThat(u -> u.getHost().equals(HOSTNAME))))
            .thenReturn(Collections.singletonList(PROXY));

        final WebSocketsProxyConnectionHandler handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, HOSTNAME,
            ProxyConfiguration.SYSTEM_DEFAULTS);

        // Act and Assert
        Assert.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assert.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());

        verify(proxySelector, times(2))
            .select(argThat(u -> u.getHost().equals(HOSTNAME)));
    }

    /**
     * Verifies that if we use the proxy configuration.
     */
    @Test
    public void proxyConfigurationSelected() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyConfiguration configuration = new ProxyConfiguration(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);
        final String host = "foo.eventhubs.azure.com";

        when(proxySelector.select(any())).thenReturn(Collections.singletonList(PROXY));

        final WebSocketsProxyConnectionHandler handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, host, configuration);

        // Act and Assert
        Assert.assertEquals(address.getHostName(), handler.getHostname());
        Assert.assertEquals(address.getPort(), handler.getProtocolPort());

        verifyZeroInteractions(proxySelector);
    }

    @Test
    public void shouldUseProxyNoLegalProxyAddress() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";

        when(proxySelector.select(argThat(u -> u.getHost().equals(host))))
            .thenReturn(Collections.emptyList());

        // Act and Assert
        Assert.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(host));
    }

    @Test(expected = NullPointerException.class)
    public void shouldUseProxyHostNull() {
        WebSocketsProxyConnectionHandler.shouldUseProxy(null);
    }

    @Test
    public void shouldUseProxyNullProxySelector() {
        // Arrange
        final String host = "foo.eventhubs.azure.com";
        ProxySelector.setDefault(null);

        // Act and Assert
        Assert.assertFalse(WebSocketsProxyConnectionHandler.shouldUseProxy(host));
    }
}
