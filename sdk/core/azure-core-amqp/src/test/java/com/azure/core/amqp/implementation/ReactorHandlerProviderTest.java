// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsProxyConnectionHandler;
import com.azure.core.amqp.models.ProxyAuthenticationType;
import com.azure.core.amqp.models.ProxyConfiguration;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(value = Theories.class)
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


    @DataPoints("configurations")
    public static ProxyConfiguration[] getProxyConfigurations() {
        return new ProxyConfiguration[]{
            ProxyConfiguration.SYSTEM_DEFAULTS,
            new ProxyConfiguration(ProxyAuthenticationType.BASIC, null, "some username", "some password"),
            null
        };
    }

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(reactorProvider.createReactor(eq(CONNECTION_ID), anyInt())).thenReturn(reactor);
        when(reactorProvider.getReactor()).thenReturn(reactor);

        provider = new ReactorHandlerProvider(reactorProvider);

        originalProxySelector = ProxySelector.getDefault();

        proxySelector = mock(ProxySelector.class, Mockito.CALLS_REAL_METHODS);
        ProxySelector.setDefault(proxySelector);
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        ProxySelector.setDefault(originalProxySelector);
    }

    @Test
    public void getsConnectionHandlerAMQP() {
        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, HOSTNAME, TransportType.AMQP, null);

        // Assert
        Assert.assertNotNull(handler);
        Assert.assertEquals(5671, handler.getProtocolPort());
    }

    /**
     * Verify that if the user has not provided a proxy, and asks for websockets, we pass the correct handler.
     */
    @Theory
    public void getsConnectionHandlerWebSockets(@FromDataPoints("configurations") ProxyConfiguration configuration) {
        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, HOSTNAME,
            TransportType.AMQP_WEB_SOCKETS, configuration);

        // Assert
        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof WebSocketsConnectionHandler);
        Assert.assertEquals(443, handler.getProtocolPort());
    }

    /**
     * Verify if user provides a proxy address, we return the correct proxy properties.
     */
    @Test
    public void getsConnectionHandlerProxy() {
        // Arrange
        final InetSocketAddress address = InetSocketAddress.createUnresolved("my-new.proxy.com", 8888);
        final Proxy newProxy = new Proxy(Proxy.Type.HTTP, address);
        final ProxyConfiguration configuration = new ProxyConfiguration(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);
        final String hostname = "foo.eventhubs.azure.com";

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, hostname,
            TransportType.AMQP_WEB_SOCKETS, configuration);

        // Assert
        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof WebSocketsProxyConnectionHandler);
        Assert.assertEquals(address.getHostName(), handler.getHostname());
        Assert.assertEquals(address.getPort(), handler.getProtocolPort());

        verifyZeroInteractions(proxySelector);
    }

    /**
     * Verifies that if no proxy configuration is set, then it will use the system configured proxy.
     */
    @Theory
    public void noProxySelected(@FromDataPoints("configurations") ProxyConfiguration configuration) {
        // Arrange
        final String hostname = "foo.eventhubs.azure.com";
        when(proxySelector.select(argThat(u -> u.getHost().equals(hostname))))
            .thenReturn(Collections.singletonList(PROXY));

        // Act
        final ConnectionHandler handler = provider.createConnectionHandler(CONNECTION_ID, hostname,
            TransportType.AMQP_WEB_SOCKETS, configuration);

        // Act and Assert
        Assert.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assert.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());
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
