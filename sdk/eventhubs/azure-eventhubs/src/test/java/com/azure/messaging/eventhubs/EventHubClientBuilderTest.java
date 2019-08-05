// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.models.ProxyAuthenticationType;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class EventHubClientBuilderTest {
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";

    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";

    private static final String CORRECT_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, EVENT_HUB_NAME);
    private static final Proxy PROXY_ADDRESS = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    @Test(expected = IllegalArgumentException.class)
    public void missingConnectionString() {
        final EventHubClientBuilder builder = new EventHubClientBuilder();
        builder.buildAsyncClient();
    }

    @Test
    public void defaultProxyConfigurationBuilder() {
        final EventHubClientBuilder builder = new EventHubClientBuilder();
        final EventHubAsyncClient client = builder.connectionString(CORRECT_CONNECTION_STRING).buildAsyncClient();

        Assert.assertNotNull(client);
    }

    @Test
    public void customNoneProxyConfigurationBuilder() {
        // Arrange
        final ProxyConfiguration proxyConfig = new ProxyConfiguration(ProxyAuthenticationType.NONE, PROXY_ADDRESS,
            null, null);

        // Act
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .proxyConfiguration(proxyConfig)
            .transportType(TransportType.AMQP_WEB_SOCKETS);

        // Assert
        Assert.assertNotNull(builder.buildAsyncClient());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWithProxyWhenTransportTypeNotChanged() {
        // Arrange
        final ProxyConfiguration proxyConfig = new ProxyConfiguration(ProxyAuthenticationType.BASIC, PROXY_ADDRESS,
            null, null);

        // Act
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .proxyConfiguration(proxyConfig);

        // Assert
        Assert.assertNotNull(builder.buildAsyncClient());
    }

    private static URI getURI(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }

    // TODO: add test for retry(), scheduler(), timeout(), transportType()
}
