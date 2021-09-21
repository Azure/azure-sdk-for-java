// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class WebSocketsProxyConnectionHandlerTest {
    private static final String CONNECTION_ID = "some-connection-id";
    private static final String HOSTNAME = "event-hubs.windows.core.net";
    private static final InetSocketAddress PROXY_ADDRESS = InetSocketAddress.createUnresolved("foo.proxy.com", 3138);
    private static final Proxy PROXY = new Proxy(Proxy.Type.HTTP, PROXY_ADDRESS);
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final ProxyOptions PROXY_OPTIONS =
        new ProxyOptions(ProxyAuthenticationType.DIGEST, PROXY, USERNAME, PASSWORD);
    private static final SslDomain.VerifyMode VERIFY_MODE = SslDomain.VerifyMode.VERIFY_PEER_NAME;
    private static final String PRODUCT = "test";
    private static final String CLIENT_VERSION = "1.0.0-test";
    private static final List<Header> HEADER_LIST = Collections.singletonList(
        new Header("foo-bar", "some-values"));
    private static final ClientOptions CLIENT_OPTIONS = new ClientOptions()
        .setHeaders(HEADER_LIST);

    private final SslPeerDetails peerDetails = Proton.sslPeerDetails(HOSTNAME, 2192);

    private ProxySelector originalProxySelector;
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
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, "scope", AmqpTransportType.AMQP,
            new AmqpRetryOptions(), ProxyOptions.SYSTEM_DEFAULTS, scheduler, CLIENT_OPTIONS, VERIFY_MODE, PRODUCT,
            CLIENT_VERSION);

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
            PROXY_OPTIONS, peerDetails));
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(CONNECTION_ID, null,
            PROXY_OPTIONS, peerDetails));
        assertThrows(NullPointerException.class, () -> new WebSocketsProxyConnectionHandler(CONNECTION_ID,
            connectionOptions, PROXY_OPTIONS, null));
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
            peerDetails);

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
            ProxyOptions.SYSTEM_DEFAULTS, peerDetails);

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
        final ProxyOptions proxyOptions = new ProxyOptions(ProxyAuthenticationType.BASIC, newProxy, USERNAME,
            PASSWORD);

        when(proxySelector.select(any())).thenReturn(Collections.singletonList(PROXY));

        this.handler = new WebSocketsProxyConnectionHandler(CONNECTION_ID, connectionOptions, proxyOptions,
            peerDetails);

        // Act and Assert
        Assertions.assertEquals(address.getHostName(), handler.getHostname());
        Assertions.assertEquals(address.getPort(), handler.getProtocolPort());

        verifyNoInteractions(proxySelector);
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
}
