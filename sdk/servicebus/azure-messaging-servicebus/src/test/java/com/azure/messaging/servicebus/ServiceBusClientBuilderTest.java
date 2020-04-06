// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSenderClientBuilder;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceBusClientBuilderTest {
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENDPOINT_FORMAT = "sb://%s.%s";
    private static final String QUEUE_NAME = "test-queue-name";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getUri(ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";

    private static final String NAMESPACE_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);
    private static final String ENTITY_PATH_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, QUEUE_NAME);
    private static final Proxy PROXY_ADDRESS = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    @Test
    void missingConnectionString() {
        assertThrows(IllegalArgumentException.class, () -> {
            final ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
            builder.sender()
                .queueName(QUEUE_NAME)
                .buildAsyncClient();
        });
    }

    @Test
    void defaultProxyConfigurationBuilder() {
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        final ServiceBusSenderAsyncClient client = builder.connectionString(NAMESPACE_CONNECTION_STRING)
            .sender()
            .queueName(QUEUE_NAME)
            .buildAsyncClient();

        assertNotNull(client);
    }

    @Test
    void customNoneProxyConfigurationBuilder() {
        // Arrange
        final ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.NONE, PROXY_ADDRESS,
            null, null);

        // Act
        final ServiceBusSenderClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(ENTITY_PATH_CONNECTION_STRING)
            .proxyOptions(proxyConfig)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .sender();

        // Assert
        assertNotNull(builder.buildAsyncClient());
    }

    @Test
    void throwsWithProxyWhenTransportTypeNotChanged() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Arrange
            final ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.BASIC, PROXY_ADDRESS,
                null, null);

            // Act
            final ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
                .connectionString(ENTITY_PATH_CONNECTION_STRING)
                .proxyOptions(proxyConfig);

            // Assert
            assertNotNull(builder.sender().buildAsyncClient());
        });
    }

    private static Stream<Arguments> invalidEntityPathConfigurations() {
        return Stream.of(
            // When no queue or topic name is set for a sender or receiver.
            Arguments.of(NAMESPACE_CONNECTION_STRING, null, null, null),

            // When both queue or topic name is set for a sender or receiver.
            Arguments.of(NAMESPACE_CONNECTION_STRING, "baz", "bar", "foo"),

            // When queue name does not match an entity path connection string.
            Arguments.of(ENTITY_PATH_CONNECTION_STRING, "baz", null, null),

            // When topic name does not match an entity path connection string.
            Arguments.of(ENTITY_PATH_CONNECTION_STRING, null, "bar", "foo"));
    }

    /**
     * Tests different invalid entity path scenarios.
     */
    @ParameterizedTest
    @MethodSource
    void invalidEntityPathConfigurations(String connectionString, String topicName, String queueName,
        String subscriptionName) {

        // Arrange
        final ServiceBusSenderClientBuilder senderBuilder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .sender();
        final ServiceBusReceiverClientBuilder receiverBuilder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver();

        // Act & Assert
        assertThrows(IllegalStateException.class, senderBuilder::buildAsyncClient);
        assertThrows(IllegalStateException.class, receiverBuilder::buildAsyncClient);
    }

    /**
     * Throws when topic name is set for receiver, but no subscription name is set.
     */
    @Test
    void throwsWhenSubscriptionNameNotSet() {
        // Arrange
        final ServiceBusReceiverClientBuilder receiverBuilder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver()
            .topicName("baz");

        // Act & Assert
        assertThrows(IllegalStateException.class, receiverBuilder::buildAsyncClient);
    }

    /**
     * Throws when the prefetch is less than 1.
     */
    @Test
    void invalidPrefetch() {
        // Arrange
        final ServiceBusReceiverClientBuilder receiverBuilder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver()
            .topicName("baz").subscriptionName("bar")
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .prefetchCount(0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, receiverBuilder::buildAsyncClient);
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
