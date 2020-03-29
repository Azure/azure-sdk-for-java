// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceBusClientBuilderTest {
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENDPOINT_FORMAT = "sb://%s.%s";
    private static final String QUEUE_NAME = "queueName";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getUri(ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";

    private static final String NAMESPACE_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);
    private static final String CORRECT_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, QUEUE_NAME);
    private static final Proxy PROXY_ADDRESS = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    @Test
    public void missingConnectionString() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
            builder.buildSenderClientBuilder()
                .entityName(QUEUE_NAME)
                .buildAsyncClient();
        });
    }

    @Test
    public void defaultProxyConfigurationBuilder() {
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        final ServiceBusSenderAsyncClient client = builder.connectionString(NAMESPACE_CONNECTION_STRING)
            .buildSenderClientBuilder()
            .entityName(QUEUE_NAME)
            .buildAsyncClient();

        assertNotNull(client);
    }

    @Test
    public void customNoneProxyConfigurationBuilder() {
        // Arrange
        final ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.NONE, PROXY_ADDRESS,
            null, null);

        // Act
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .proxyOptions(proxyConfig)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        // Assert
        assertNotNull(builder.buildSenderClientBuilder().buildAsyncClient());
    }

    @Test
    public void throwsWithProxyWhenTransportTypeNotChanged() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Arrange
            final ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.BASIC, PROXY_ADDRESS,
                null, null);

            // Act
            final ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
                .connectionString(CORRECT_CONNECTION_STRING)
                .proxyOptions(proxyConfig);

            // Assert
            assertNotNull(builder.buildSenderClientBuilder().buildAsyncClient());
        });
    }

    private static URI getUri(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }

}
