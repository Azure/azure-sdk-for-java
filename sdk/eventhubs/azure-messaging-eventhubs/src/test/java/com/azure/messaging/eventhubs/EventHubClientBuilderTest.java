// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.scheduler.Scheduler;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHubClientBuilderTest {
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String ENDPOINT_SUFFIX = Configuration.getGlobalConfiguration()
        .get("AZURE_EVENTHUBS_ENDPOINT_SUFFIX", ".servicebus.windows.net");
    private static final String DEFAULT_DOMAIN_NAME = ENDPOINT_SUFFIX.substring(1) + "/";
    private static final String EVENT_HUB_NAME = "eventHubName";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getURI(ClientConstants.ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();
    private static final TokenCredential TOKEN_CREDENTIAL = new BasicAuthenticationCredential("foo", "bar");

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";

    private static final String CONNECTION_STRING_NAMESPACE_FORMAT = "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s";
    private static final String CONNECTION_STRING_WITH_ENTITY_FORMAT = "Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s";
    private static final String CORRECT_CONNECTION_STRING = String.format(CONNECTION_STRING_WITH_ENTITY_FORMAT,
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, EVENT_HUB_NAME);
    private static final Proxy PROXY_ADDRESS = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));
    public static final String JAVA_NET_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";

    @Mock
    private Scheduler scheduler;

    @Mock
    private TokenCredential tokenCredential;
    private AutoCloseable closeable;

    @BeforeEach
    public void beforeEach() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

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

        String connectionStringWithNoEntityPath = String.format("Endpoint=sb://eh-name%s/;"
            + "SharedAccessSignature=SharedAccessSignature test-value", ENDPOINT_SUFFIX);
        String connectionStringWithEntityPath = String.format("Endpoint=sb://eh-name%s/;"
            + "SharedAccessSignature=SharedAccessSignature test-value;EntityPath=eh-name", ENDPOINT_SUFFIX);

        assertNotNull(new EventHubClientBuilder()
            .connectionString(connectionStringWithNoEntityPath, "eh-name"));
        assertNotNull(new EventHubClientBuilder()
            .connectionString(connectionStringWithEntityPath));
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .connectionString(connectionStringWithEntityPath, "eh-name-mismatch"));
    }

    @MethodSource("getProxyConfigurations")
    @ParameterizedTest
    public void testProxyOptionsConfiguration(String proxyConfiguration) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(Configuration.PROPERTY_HTTP_PROXY, proxyConfiguration);
        configuration = configuration.put(JAVA_NET_USE_SYSTEM_PROXIES, "true");

        // Client creation should not fail with incorrect proxy configurations
        EventHubConsumerAsyncClient asyncClient = new EventHubClientBuilder()
            .connectionString(CORRECT_CONNECTION_STRING)
            .configuration(configuration)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .buildAsyncConsumerClient();
    }

    @Test
    public void testConnectionWithAzureNameKeyCredential() {
        String fullyQualifiedNamespace = String.format("sb-name%s", ENDPOINT_SUFFIX);
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
        String fullyQualifiedNamespace = String.format("sb-name%s", ENDPOINT_SUFFIX);
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

    @Test
    public void testCreatesClientWithTokenCredential() {
        EventHubClient eventHubClient = new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME)
            .eventHubName(EVENT_HUB_NAME)
            .buildClient();
        EventHubProducerClient eventHubProducerClient = new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME)
            .eventHubName(EVENT_HUB_NAME)
            .buildProducerClient();
        EventHubConsumerClient eventHubConsumerClient = new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME)
            .eventHubName(EVENT_HUB_NAME)
            .consumerGroup("foo")
            .buildConsumerClient();

        // Assert
        assertNotNull(eventHubClient);
        assertNotNull(eventHubProducerClient);
        assertNotNull(eventHubConsumerClient);

        assertEquals(EVENT_HUB_NAME, eventHubProducerClient.getEventHubName());
        assertEquals(NAMESPACE_NAME, eventHubProducerClient.getFullyQualifiedNamespace());

        assertEquals(EVENT_HUB_NAME, eventHubConsumerClient.getEventHubName());
        assertEquals(NAMESPACE_NAME, eventHubConsumerClient.getFullyQualifiedNamespace());
    }

    @Test
    public void testThrowsIfAttemptsToCreateClientWithTokenCredentialWithoutFullyQualifiedName() {
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .eventHubName(EVENT_HUB_NAME)
            .buildClient());
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .eventHubName(EVENT_HUB_NAME)
            .buildProducerClient());
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .eventHubName(EVENT_HUB_NAME)
            .consumerGroup("foo")
            .buildConsumerClient());
    }

    @Test
    public void testThrowsIfAttemptsToCreateClientWithTokenCredentialWithoutEventHubName() {
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME)
            .buildClient());
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME)
            .buildProducerClient());
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .credential(TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME)
            .consumerGroup("foo")
            .buildConsumerClient());
    }

    /**
     * Verifies that we can pass an Event Hub namespace connection string and event hub name to create a client.
     */
    @Test
    public void namespaceConnectionStringAndName() {
        // Arrange
        final String namespaceConnectionString = String.format(CONNECTION_STRING_NAMESPACE_FORMAT,
            ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);
        final String fullyQualifiedDomainName = NAMESPACE_NAME + ENDPOINT_SUFFIX;

        // Act
        final EventHubProducerAsyncClient client = new EventHubClientBuilder()
            .connectionString(namespaceConnectionString)
            .eventHubName(EVENT_HUB_NAME)
            .buildAsyncProducerClient();

        // Assert
        assertTrue(fullyQualifiedDomainName.equalsIgnoreCase(client.getFullyQualifiedNamespace()),
            String.format("Expected: %s. Actual: %s%n", fullyQualifiedDomainName,
                client.getFullyQualifiedNamespace()));

        assertEquals(EVENT_HUB_NAME, client.getEventHubName());
    }

    /**
     * Verifies that an exception is thrown when we try to construct a client without setting the event hub name.
     */
    @Test
    public void namespaceConnectionStringThrowsNoEventHubName() {
        // Arrange
        final String namespaceConnectionString = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
            ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .connectionString(namespaceConnectionString)
            .buildAsyncProducerClient());
        assertThrows(IllegalArgumentException.class, () -> new EventHubClientBuilder()
            .connectionString(namespaceConnectionString)
            .buildAsyncConsumerClient());
    }

    @Test
    public void getsCorrectEndpoint() {
        // Arrange
        final String fqdn = "test.foo.com";
        final String eventHubName = "my-event-hub";
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .retryOptions(new AmqpRetryOptions())
            .scheduler(scheduler);

        // Act
        builder.credential(fqdn, eventHubName, tokenCredential);

        final ConnectionOptions actual = builder.getConnectionOptions();

        // Assert
        assertEquals(fqdn, actual.getFullyQualifiedNamespace());
        assertEquals(fqdn, actual.getHostname());
        assertEquals(ConnectionHandler.AMQPS_PORT, actual.getPort());
        assertEquals(AmqpTransportType.AMQP, actual.getTransportType());
    }

    @Test
    public void getsCorrectEndpointCustomEndpoint() {
        // Arrange
        final String fqdn = "test.foo.com";
        final String eventHubName = "my-event-hub";

        final String customHostname = "my.local.endpoint";
        final int customPort = 4542;
        final String customEndpoint = "sb://" + customHostname + ":" + customPort;
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .retryOptions(new AmqpRetryOptions())
            .scheduler(scheduler);
        // Act
        builder.credential(fqdn, eventHubName, tokenCredential)
            .customEndpointAddress(customEndpoint);

        final ConnectionOptions actual = builder.getConnectionOptions();

        // Assert
        assertEquals(fqdn, actual.getFullyQualifiedNamespace());
        assertEquals(customHostname, actual.getHostname());
        assertEquals(customPort, actual.getPort());
        assertEquals(AmqpTransportType.AMQP, actual.getTransportType());
    }


    public static Stream<Arguments> getsCorrectEndpointConnectionString() {
        final String fqdn = "test.foo.com";
        final String fqdnEndpoint = "sb://" + fqdn;
        final String eventHubName = "my-event-hub";
        final String connectionString = String.format(CONNECTION_STRING_WITH_ENTITY_FORMAT, fqdnEndpoint,
            "shared-value-name-key", "shared-value-name", eventHubName);

        final String hostname = "test.local";
        final int port = 13454;
        final String customEndpoint = "sb://" + hostname + ":" + port;
        final String entityConnectionString = String.format(CONNECTION_STRING_WITH_ENTITY_FORMAT,
             customEndpoint, "shared-value-name-key", "shared-value-name", eventHubName);

        return Stream.of(
            Arguments.of(connectionString, fqdn, ConnectionHandler.AMQPS_PORT),
            Arguments.of(entityConnectionString, hostname, port)
        );
    }

    @MethodSource
    @ParameterizedTest
    public void getsCorrectEndpointConnectionString(String connectionString, String expectedHostname,
        int expectedPort) {
        // Arrange
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .retryOptions(new AmqpRetryOptions())
            .scheduler(scheduler);

        // Act
        builder.connectionString(connectionString);

        final ConnectionOptions actual = builder.getConnectionOptions();

        // Assert
        assertEquals(expectedHostname, actual.getHostname());
        assertEquals(expectedPort, actual.getPort());
    }

    private static Stream<Arguments> getProxyConfigurations() {
        return Stream.of(
            Arguments.of("http://localhost:8080"),
            Arguments.of("localhost:8080"),
            Arguments.of("localhost_8080"),
            Arguments.of("http://example.com:8080"),
            Arguments.of("http://sub.example.com:8080"),
            Arguments.of(":8080"),
            Arguments.of("http://localhost"),
            Arguments.of("sub.example.com:8080"),
            Arguments.of("https://username:password@sub.example.com:8080"),
            Arguments.of("https://username:password@sub.example.com")
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
}
