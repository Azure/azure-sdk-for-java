// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHubClientBuilderTest extends IntegrationTestBase {
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

    private static final String TEST_CONTENTS = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \nUt sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \nAenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \nAenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \nDonec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";

    EventHubClientBuilderTest() {
        super(new ClientLogger(EventHubClientBuilderTest.class));
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
    public synchronized void testProxyOptionsConfiguration(String proxyConfiguration, boolean expectedClientCreation) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(Configuration.PROPERTY_HTTP_PROXY, proxyConfiguration);
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
    public void sendAndReceiveEventByAzureNameKeyCredential() {
        ConnectionStringProperties properties = getConnectionStringProperties();
        String fullyQualifiedNamespace = getFullyQualifiedDomainName();
        String sharedAccessKeyName = properties.getSharedAccessKeyName();
        String sharedAccessKey = properties.getSharedAccessKey();
        String eventHubName = getEventHubName();

        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));

        EventHubProducerAsyncClient asyncProducerClient = new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey))
            .buildAsyncProducerClient();
        try {
            StepVerifier.create(
                asyncProducerClient.createBatch().flatMap(batch -> {
                    assertTrue(batch.tryAdd(testData));
                    return asyncProducerClient.send(batch);
                })
            ).verifyComplete();
        } finally {
            asyncProducerClient.close();
        }
    }

    @Test
    public void sendAndReceiveEventByAzureSasCredential() {
        Assumptions.assumeTrue(getConnectionString(true) != null,
            "SAS was not set. Can't run test scenario.");

        ConnectionStringProperties properties = getConnectionStringProperties(true);
        String fullyQualifiedNamespace = getFullyQualifiedDomainName();
        String sharedAccessSignature = properties.getSharedAccessSignature();
        String eventHubName = getEventHubName();

        final EventData testData = new EventData(TEST_CONTENTS.getBytes(UTF_8));

        EventHubProducerAsyncClient asyncProducerClient = new EventHubClientBuilder()
            .credential(fullyQualifiedNamespace, eventHubName,
                new AzureSasCredential(sharedAccessSignature))
            .buildAsyncProducerClient();
        try {
            StepVerifier.create(
                asyncProducerClient.createBatch().flatMap(batch -> {
                    assertTrue(batch.tryAdd(testData));
                    return asyncProducerClient.send(batch);
                })
            ).verifyComplete();
        } finally {
            asyncProducerClient.close();
        }
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
