// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSenderClientBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import org.junit.jupiter.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static java.nio.charset.StandardCharsets.UTF_8;

class ServiceBusClientBuilderTest extends IntegrationTestBase {
    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final String DEFAULT_DOMAIN_NAME = "servicebus.windows.net/";
    private static final String ENDPOINT_FORMAT = "sb://%s.%s";
    private static final String QUEUE_NAME = "test-queue-name";
    private static final String VIA_QUEUE_NAME = "test-via-queue-name";
    private static final String TOPIC_NAME = "test-topic-name";
    private static final String VIA_TOPIC_NAME = "test-via-queue-name";
    private static final String SHARED_ACCESS_KEY_NAME = "dummySasKeyName";
    private static final String SHARED_ACCESS_KEY = "dummySasKey";
    private static final String ENDPOINT = getUri(ENDPOINT_FORMAT, NAMESPACE_NAME, DEFAULT_DOMAIN_NAME).toString();
    private static final String DEAD_LETTER_QUEUE_NAME_SUFFIX = "/$deadletterqueue";
    private static final String TRANSFER_DEAD_LETTER_QUEUE_NAME_SUFFIX = "/$Transfer/$deadletterqueue";

    private static final String PROXY_HOST = "127.0.0.1";
    private static final String PROXY_PORT = "3128";

    private static final String NAMESPACE_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY);
    private static final String ENTITY_PATH_CONNECTION_STRING = String.format("Endpoint=%s;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s",
        ENDPOINT, SHARED_ACCESS_KEY_NAME, SHARED_ACCESS_KEY, QUEUE_NAME);
    private static final Proxy PROXY_ADDRESS = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));

    private static final String TEST_MESSAGE = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \nUt sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \nAenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \nAenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \nDonec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";

    ServiceBusClientBuilderTest() {
        super(new ClientLogger(ServiceBusClientBuilderTest.class));
    }

    @Test
    void deadLetterqueueClient() {
        // Arrange
        final ServiceBusReceiverClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver()
            .queueName(QUEUE_NAME)
            .subQueue(SubQueue.DEAD_LETTER_QUEUE);

        // Act
        final ServiceBusReceiverAsyncClient client = builder.buildAsyncClient();

        // Assert
        assertTrue(client.getEntityPath().endsWith(DEAD_LETTER_QUEUE_NAME_SUFFIX));
    }

    @Test
    void transferDeadLetterqueueClient() {
        // Arrange
        final ServiceBusReceiverClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(NAMESPACE_CONNECTION_STRING)
            .receiver()
            .queueName(QUEUE_NAME)
            .subQueue(SubQueue.TRANSFER_DEAD_LETTER_QUEUE);

        // Act
        final ServiceBusReceiverAsyncClient client = builder.buildAsyncClient();

        // Assert
        assertTrue(client.getEntityPath().endsWith(TRANSFER_DEAD_LETTER_QUEUE_NAME_SUFFIX));
    }

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
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> receiverBuilder.prefetchCount(-1));
    }

    @MethodSource("getProxyConfigurations")
    @ParameterizedTest
    public void testProxyOptionsConfiguration(String proxyConfiguration, boolean expectedClientCreation) {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        configuration = configuration.put(Configuration.PROPERTY_HTTP_PROXY, proxyConfiguration);
        boolean clientCreated = false;
        try {
            ServiceBusReceiverClient syncClient = new ServiceBusClientBuilder()
                .connectionString(NAMESPACE_CONNECTION_STRING)
                .configuration(configuration)
                .receiver()
                .topicName("baz").subscriptionName("bar")
                .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                .buildClient();

            clientCreated = true;
        } catch (Exception ex) {
        }

        Assertions.assertEquals(expectedClientCreation, clientCreated);
    }

    @Test
    public void testConnectionStringWithSas() {
        String connectionStringWithEntityPath = "Endpoint=sb://sb-name.servicebus.windows.net/;"
            + "SharedAccessSignature=SharedAccessSignature test-value;EntityPath=sb-name";
        assertNotNull(new ServiceBusClientBuilder()
            .connectionString(connectionStringWithEntityPath));

        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .connectionString("SharedAccessSignature=SharedAccessSignature test-value;EntityPath=sb-name"));

        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .connectionString("Endpoint=sb://sb-name.servicebus.windows.net/;EntityPath=sb-name"));
    }

    @Test
    public void testBatchSendEventByAzureNameKeyCredential() {
        ConnectionStringProperties properties = getConnectionStringProperties();
        String fullyQualifiedNamespace = getFullyQualifiedDomainName();
        String sharedAccessKeyName = properties.getSharedAccessKeyName();
        String sharedAccessKey = properties.getSharedAccessKey();
        String queueName = getQueueName(0);

        final ServiceBusMessage testData = new ServiceBusMessage(TEST_MESSAGE.getBytes(UTF_8));

        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey))
            .sender()
            .queueName(queueName)
            .buildAsyncClient();
        try {
            StepVerifier.create(
                senderAsyncClient.createMessageBatch().flatMap(batch -> {
                    assertTrue(batch.tryAddMessage(testData));
                    return senderAsyncClient.sendMessages(batch);
                })
            ).verifyComplete();
        } finally {
            senderAsyncClient.close();
        }
    }


    @Test
    public void testBatchSendEventByAzureSasCredential() {
        ConnectionStringProperties properties = getConnectionStringProperties(true);
        String fullyQualifiedNamespace = getFullyQualifiedDomainName();
        String sharedAccessSignature = properties.getSharedAccessSignature();
        String queueName = getQueueName(0);

        final ServiceBusMessage testData = new ServiceBusMessage(TEST_MESSAGE.getBytes(UTF_8));

        ServiceBusSenderAsyncClient senderAsyncClient = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace,
                new AzureSasCredential(sharedAccessSignature))
            .sender()
            .queueName(queueName)
            .buildAsyncClient();
        try {
            StepVerifier.create(
                senderAsyncClient.createMessageBatch().flatMap(batch -> {
                    assertTrue(batch.tryAddMessage(testData));
                    return senderAsyncClient.sendMessages(batch);
                })
            ).verifyComplete();
        } finally {
            senderAsyncClient.close();
        }
    }

    @Test
    public void testConnectionWithAzureNameKeyCredential() {
        String fullyQualifiedNamespace = "sb-name.servicebus.windows.net";
        String sharedAccessKeyName = "SharedAccessKeyName test-value";
        String sharedAccessKey = "SharedAccessKey test-value";

        assertThrows(NullPointerException.class, () -> new ServiceBusClientBuilder()
            .credential(null,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(IllegalArgumentException.class, () -> new ServiceBusClientBuilder()
            .credential("",
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(IllegalArgumentException.class, () -> new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace,
                new AzureNamedKeyCredential(sharedAccessKeyName, sharedAccessKey)));

        assertThrows(NullPointerException.class, () -> new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, (AzureNamedKeyCredential) null));

    }

    @Test
    public void testConnectionWithAzureSasCredential() {
        String fullyQualifiedNamespace = "sb-name.servicebus.windows.net";
        String sharedAccessSignature = "SharedAccessSignature test-value";

        assertThrows(NullPointerException.class, () -> new ServiceBusClientBuilder()
            .credential(null, new AzureSasCredential(sharedAccessSignature)));

        assertThrows(IllegalArgumentException.class, () -> new ServiceBusClientBuilder()
            .credential("", new AzureSasCredential(sharedAccessSignature)));

        assertThrows(IllegalArgumentException.class, () -> new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new AzureSasCredential(sharedAccessSignature)));

        assertThrows(NullPointerException.class, () -> new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, (AzureSasCredential) null));

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

    private static URI getUri(String endpointFormat, String namespace, String domainName) {
        try {
            return new URI(String.format(Locale.US, endpointFormat, namespace, domainName));
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Invalid namespace name: %s", namespace), exception);
        }
    }
}
