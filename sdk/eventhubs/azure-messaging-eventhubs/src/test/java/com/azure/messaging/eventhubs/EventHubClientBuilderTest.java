// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import org.junit.jupiter.api.Assertions;
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
    public static final String JAVA_NET_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";
    private ClientLogger logger = new ClientLogger(EventHubClientBuilderTest.class);

    @Test
    public void missingConnectionString() {
        final EventHubClientBuilder builder = new EventHubClientBuilder();

        assertThrows(IllegalArgumentException.class, () -> builder.buildAsyncClient());
    }

    @Test
    public void defaultProxyConfigurationBuilder() {
        final EventHubClientBuilder builder = new EventHubClientBuilder();
        final EventHubAsyncClient client = builder.connectionString(CORRECT_CONNECTION_STRING).buildAsyncClient();

        assertNotNull(client);
    }

    @Test
    public void customNoneProxyConfigurationBuilder() {
        // Arrange
        final ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.NONE, PROXY_ADDRESS,
            null, null);

        // Act
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .proxyOptions(proxyConfig)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS);

        // Assert
        assertNotNull(builder.buildAsyncClient());
    }

    @Test
    public void throwsWithProxyWhenTransportTypeNotChanged() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Arrange
            final ProxyOptions proxyConfig = new ProxyOptions(ProxyAuthenticationType.BASIC, PROXY_ADDRESS,
                null, null);

            // Act
            final EventHubClientBuilder builder = new EventHubClientBuilder()
                .connectionString(CORRECT_CONNECTION_STRING)
                .proxyOptions(proxyConfig);

            // Assert
            assertNotNull(builder.buildAsyncClient());
        });
    }

    @Test
    public void testConnectionStringWithSas() {

        String connectionStringWithNoEntityPath = "Endpoint=sb://eh-name.servicebus.windows.net/;"
            + "SharedAccessSignature=SharedAccessSignature test-value";
        String connectionStringWithEntityPath = "Endpoint=sb://eh-name.servicebus.windows.net/;"
            + "SharedAccessSignature=SharedAccessSignature test-value;EntityPath=eh-name";

        assertNotNull(new EventHubClientBuilder()
            .connectionString(connectionStringWithNoEntityPath, "eh-name"));
        assertNotNull(new EventHubClientBuilder()
            .connectionString(connectionStringWithEntityPath));
        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .connectionString(connectionStringWithNoEntityPath));
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .connectionString(connectionStringWithEntityPath, "eh-name-mismatch"));
    }

    @MethodSource("getProxyConfigurations")
    @ParameterizedTest
    public void testProxyOptionsConfiguration(String proxyConfiguration, boolean expectedClientCreation) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(Configuration.PROPERTY_HTTP_PROXY, proxyConfiguration);
        configuration = configuration.put(JAVA_NET_USE_SYSTEM_PROXIES, "true");

        boolean clientCreated = false;
        try {
            EventHubConsumerAsyncClient asyncClient = new EventHubClientBuilder()
                .connectionString(CORRECT_CONNECTION_STRING)
                .configuration(configuration)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
                .buildAsyncConsumerClient();
            clientCreated = true;
        } catch (Exception ex) {
        }

        Assertions.assertEquals(expectedClientCreation, clientCreated);
    }

    @Test
    public void testConnectionWithAzureNameKeyCredential() {
        String fullyQualifiedNamespace = "sb-name.servicebus.windows.net";
        String sharedAccessKeyName = "SharedAccessKeyName test-value";
        String sharedAccessKey = "SharedAccessKey test-value";
        String eventHubName = "test-event-hub-name";

        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .credential(null, eventHubName,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, null,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential("", eventHubName,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, "",
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName, (AzureNamedKeyCredential) null));

    }

    @Test
    public void testConnectionWithAzureSasCredential() {
        String fullyQualifiedNamespace = "sb-name.servicebus.windows.net";
        String sharedAccessSignature = "SharedAccessSignature test-value";
        String eventHubName = "test-event-hub-name";

        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .credential(null, eventHubName, new AzureSasCredential(sharedAccessSignature)));

        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, null, new AzureSasCredential(sharedAccessSignature)));

        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential("", eventHubName, new AzureSasCredential(sharedAccessSignature)));

        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, "", new AzureSasCredential(sharedAccessSignature)));

        assertThrows(NullPointerException.class, () -> new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName, (AzureSasCredential) null));

    }

    private static Stream<Arguments> getProxyConfigurations() {
        return Stream.of(
            Arguments.of("http://localhost:8080", true),
            Arguments.of("localhost:8080", true),
            Arguments.of("localhost_8080", false),
            Arguments.of("http://example.com:8080", true),
            Arguments.of("http://sub.example.com:8080", true),
            Arguments.of(":8080", false),
            Arguments.of("http://localhost", true),
            Arguments.of("sub.example.com:8080", true),
            Arguments.of("https://username:password@sub.example.com:8080", true),
            Arguments.of("https://username:password@sub.example.com", true)
        );
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
