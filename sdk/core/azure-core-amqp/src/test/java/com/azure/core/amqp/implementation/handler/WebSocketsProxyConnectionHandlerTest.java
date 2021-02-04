// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.ClientOptions;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class WebSocketsProxyConnectionHandlerTest {
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions();
    private static final String CONNECTION_ID = "some-connection-id";
    private static final String HOSTNAME = "event-hubs.windows.core.net";
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final ProxyOptions PROXY_CONFIGURATION =
        new ProxyOptions(ProxyAuthenticationType.DIGEST, PROXY, USERNAME, PASSWORD);
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;

    private ProxySelector originalProxySelector;
    private ProxySelector proxySelector;

    /**
     * Creates mocks of the proxy selector and authenticator and sets them as defaults.
     */
    @BeforeEach
    public void setup() {
        originalProxySelector = ProxySelector.getDefault();

        proxySelector = mock(ProxySelector.class, Mockito.CALLS_REAL_METHODS);
        ProxySelector.setDefault(proxySelector);
    }

    @AfterEach
    public void teardown() {
        ProxySelector.setDefault(originalProxySelector);
        Mockito.framework().clearInlineMocks();
    }

    @Test
    public void nullProxyConfiguration() {
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(CONNECTION_ID, HOSTNAME,
            null, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS));
    }

    @Test
    public void nullHostname() {
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(CONNECTION_ID, null,
            PROXY_CONFIGURATION, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS));
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
            PROXY_CONFIGURATION, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS);

        // Act and Assert
        Assertions.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assertions.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());
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
            ProxyOptions.SYSTEM_DEFAULTS, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS);

        // Act and Assert
        Assertions.assertEquals(PROXY_ADDRESS.getHostName(), handler.getHostname());
        Assertions.assertEquals(PROXY_ADDRESS.getPort(), handler.getProtocolPort());

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
        final ProxyOptions configuration = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME, PASSWORD);
        final String host = "foo.eventhubs.azure.com";

        when(proxySelector.select(any())).thenReturn(Collections.singletonList(PROXY));

        final WebSocketsProxyConnectionHandler handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, host,
            configuration, PRODUCT, CLIENT_VERSION, VERIFY_MODE, CLIENT_OPTIONS);

        // Act and Assert
        Assertions.assertEquals(address.getHostName(), handler.getHostname());
        Assertions.assertEquals(address.getPort(), handler.getProtocolPort());

        verifyNoInteractions(proxySelector);
    }

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
