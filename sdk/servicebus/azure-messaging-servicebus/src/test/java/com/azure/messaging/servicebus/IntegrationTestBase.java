// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.credential.TokenCredential;
import com.azure.core.experimental.util.tracing.LoggingTracerProvider;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSenderClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.amqp.ProxyOptions.PROXY_PASSWORD;
import static com.azure.core.amqp.ProxyOptions.PROXY_USERNAME;
import static com.azure.messaging.servicebus.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.servicebus.TestUtils.getEntityName;
import static com.azure.messaging.servicebus.TestUtils.getQueueBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSessionQueueBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSessionSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public abstract class IntegrationTestBase extends TestBase {
    protected static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);
    protected static final Duration TIMEOUT = Duration.ofSeconds(60);
    protected static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(TIMEOUT);
    protected final ClientLogger logger;
    protected ClientOptions optionsWithTracing;
    private static final String PROXY_AUTHENTICATION_TYPE = "PROXY_AUTHENTICATION_TYPE";
    private static final Configuration GLOBAL_CONFIGURATION = TestUtils.getGlobalConfiguration();
    private List<AutoCloseable> toClose = new ArrayList<>();
    private String testName;
    private final Scheduler scheduler = Schedulers.parallel();

    protected static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    protected String sessionId;

    ServiceBusClientBuilder sharedBuilder;

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        Method testMethod = testInfo.getTestMethod().orElseGet(null);
        testName = String.format("%s-%s",
            testMethod == null ? "unknown" : testMethod.getName(),
            testInfo.getDisplayName());

        logger.info("========= SET-UP [{}] =========", testName);

        assumeTrue(getTestMode() == TestMode.RECORD);

        StepVerifier.setDefaultTimeout(TIMEOUT);
        toClose = new ArrayList<>();
        optionsWithTracing = new ClientOptions().setTracingOptions(new LoggingTracerProvider.LoggingTracingOptions());
        beforeTest();
    }

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
        logger.info("========= TEARDOWN [{}] =========", testName);
        StepVerifier.resetDefaultTimeout();
        afterTest();

        logger.info("Disposing of subscriptions, consumers and clients.");
        dispose();
    }

    /**
     * Gets the test mode for this API test. If AZURE_TEST_MODE equals {@link TestMode#RECORD} and Event Hubs connection
     * string is set, then we return {@link TestMode#RECORD}. Otherwise, {@link TestMode#PLAYBACK} is returned.
     */
    @Override
    public TestMode getTestMode() {
        if (super.getTestMode() == TestMode.PLAYBACK) {
            return TestMode.PLAYBACK;
        }

        return CoreUtils.isNullOrEmpty(getConnectionString()) ? TestMode.PLAYBACK : TestMode.RECORD;
    }

    public static String getConnectionString() {
        return TestUtils.getConnectionString(false);
    }

    public static String getConnectionString(boolean withSas) {
        return TestUtils.getConnectionString(withSas);
    }

    protected static ConnectionStringProperties getConnectionStringProperties() {
        return new ConnectionStringProperties(getConnectionString(false));
    }

    protected static ConnectionStringProperties getConnectionStringProperties(boolean withSas) {
        return new ConnectionStringProperties(getConnectionString(withSas));
    }

    public String getFullyQualifiedDomainName() {
        return TestUtils.getFullyQualifiedDomainName();
    }

    /**
     * Gets the name of the queue.
     *
     * @param index Index of the queue.
     *
     * @return Name of the queue.
     */
    public String getQueueName(int index) {
        return getEntityName(getQueueBaseName(), index);
    }

    public String getSessionQueueName(int index) {
        return getEntityName(getSessionQueueBaseName(), index);
    }

    /**
     * Gets the name of the topic.
     *
     * @return Name of the topic.
     */
    public String getTopicName(int index) {
        return getEntityName(TestUtils.getTopicBaseName(), index);
    }

    /**
     * Gets the configured ProxyConfiguration from environment variables.
     */
    public ProxyOptions getProxyConfiguration() {
        final String address = GLOBAL_CONFIGURATION.get(Configuration.PROPERTY_HTTP_PROXY);

        if (address == null) {
            return null;
        }

        final String[] host = address.split(":");
        if (host.length < 2) {
            logger.warning("Environment variable '{}' cannot be parsed into a proxy. Value: {}",
                Configuration.PROPERTY_HTTP_PROXY, address);
            return null;
        }

        final String hostname = host[0];
        final int port = Integer.parseInt(host[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));

        final String username = GLOBAL_CONFIGURATION.get(PROXY_USERNAME);

        if (username == null) {
            logger.info("Environment variable '{}' is not set. No authentication used.");
            return new ProxyOptions(ProxyAuthenticationType.NONE, proxy, null, null);
        }

        final String password = GLOBAL_CONFIGURATION.get(PROXY_PASSWORD);
        final String authentication = GLOBAL_CONFIGURATION.get(PROXY_AUTHENTICATION_TYPE);

        final ProxyAuthenticationType authenticationType = CoreUtils.isNullOrEmpty(authentication)
            ? ProxyAuthenticationType.NONE
            : ProxyAuthenticationType.valueOf(authentication);

        return new ProxyOptions(authenticationType, proxy, username, password);
    }

    /**
     * Creates a new instance of {@link ServiceBusClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate.
     */
    protected ServiceBusClientBuilder getBuilder() {
        return getBuilder(false);
    }

    /**
     * Creates a new instance of {@link ServiceBusClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate if {@code useCredentials} is false. Otherwise, uses a service principal through
     * {@link com.azure.identity.ClientSecretCredential}.
     */
    protected ServiceBusClientBuilder getBuilder(boolean useCredentials) {
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retryOptions(RETRY_OPTIONS)
            .clientOptions(optionsWithTracing)
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler);

        logger.info("Getting Builder using credentials : [{}] ", useCredentials);
        if (useCredentials) {
            final String fullyQualifiedDomainName = getFullyQualifiedDomainName();

            assumeTrue(fullyQualifiedDomainName != null && !fullyQualifiedDomainName.isEmpty(),
                "AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME variable needs to be set when using credentials.");

            final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

            return builder.credential(fullyQualifiedDomainName, tokenCredential);
        } else {
            return builder.connectionString(getConnectionString());
        }
    }

    protected ServiceBusSenderClientBuilder getSenderBuilder(boolean useCredentials, MessagingEntityType entityType,
        int entityIndex, boolean isSessionAware, boolean sharedConnection) {

        ServiceBusClientBuilder builder = getBuilder(useCredentials, sharedConnection);
        switch (entityType) {
            case QUEUE:
                final String queueName = isSessionAware ? getSessionQueueName(entityIndex) : getQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");
                return builder.sender()
                    .queueName(queueName);
            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                assertNotNull(topicName, "'topicName' cannot be null.");

                return builder.sender().topicName(topicName);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    protected ServiceBusReceiverClientBuilder getReceiverBuilder(boolean useCredentials, MessagingEntityType entityType,
        int entityIndex, boolean sharedConnection) {

        ServiceBusClientBuilder builder = getBuilder(useCredentials, sharedConnection);
        switch (entityType) {
            case QUEUE:
                final String queueName = getQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");

                return builder.receiver().receiveMode(ServiceBusReceiveMode.PEEK_LOCK).queueName(queueName);
            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = getSubscriptionBaseName();
                assertNotNull(topicName, "'topicName' cannot be null.");
                assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                return builder.receiver().receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                    .topicName(topicName).subscriptionName(subscriptionName);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    protected ServiceBusSessionReceiverClientBuilder getSessionReceiverBuilder(boolean useCredentials,
        MessagingEntityType entityType, int entityIndex, boolean sharedConnection, AmqpRetryOptions retryOptions) {

        ServiceBusClientBuilder builder = getBuilder(useCredentials, sharedConnection);

        switch (entityType) {
            case QUEUE:
                final String queueName = getSessionQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");
                return builder
                    .retryOptions(retryOptions)
                    .sessionReceiver()
                    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                    .queueName(queueName);

            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = getSessionSubscriptionBaseName();
                assertNotNull(topicName, "'topicName' cannot be null.");
                assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");
                return builder
                    .retryOptions(retryOptions)
                    .sessionReceiver()
                    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                    .topicName(topicName).subscriptionName(subscriptionName);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    protected static Stream<Arguments> messagingEntityProvider() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE),
            Arguments.of(MessagingEntityType.SUBSCRIPTION)
        );
    }

    protected static Stream<Arguments> messagingEntityWithSessions() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE, false),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, false),
            Arguments.of(MessagingEntityType.QUEUE, true),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, true)
        );
    }

    protected static Stream<Arguments> receiveDeferredMessageBySequenceNumber() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE, DispositionStatus.COMPLETED),
            Arguments.of(MessagingEntityType.QUEUE, DispositionStatus.ABANDONED),
            Arguments.of(MessagingEntityType.QUEUE, DispositionStatus.SUSPENDED),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, DispositionStatus.ABANDONED),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, DispositionStatus.COMPLETED),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, DispositionStatus.SUSPENDED)
        );
    }

    protected <T extends AutoCloseable> T toClose(T closeable) {
        toClose.add(closeable);
        return closeable;
    }

    protected Disposable toClose(Disposable closeable) {
        toClose.add(() -> closeable.dispose());
        return closeable;
    }

    /**
     * Disposes of registered with {@code toClose} method resources.
     */
    protected void dispose() {
        dispose(toClose.toArray(new AutoCloseable[0]));
        toClose.clear();
    }

    /**
     * Disposes of any {@link Closeable} resources.
     *
     * @param closeables The closeables to dispose of. If a closeable is {@code null}, it is skipped.
     */
    protected void dispose(AutoCloseable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        final List<Mono<Void>> closeableMonos = new ArrayList<>();

        for (final AutoCloseable closeable : closeables) {
            if (closeable == null) {
                continue;
            }

            if (closeable instanceof AsyncCloseable) {
                final Mono<Void> voidMono = ((AsyncCloseable) closeable).closeAsync();
                closeableMonos.add(voidMono);

                voidMono.subscribe();
            }

            try {
                closeable.close();
            } catch (Exception error) {
                logger.error("[{}]: {} didn't close properly.", testName, closeable.getClass().getSimpleName(), error);
            }
        }

        Mono.when(closeableMonos).block(TIMEOUT);
    }

    protected ServiceBusMessage getMessage(String messageId, boolean isSessionEnabled, AmqpMessageBody amqpMessageBody) {
        final ServiceBusMessage message = new ServiceBusMessage(amqpMessageBody);
        message.setMessageId(messageId);
        return isSessionEnabled ? message.setSessionId(sessionId) : message;
    }

    protected ServiceBusMessage getMessage(String messageId, boolean isSessionEnabled) {
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);
        message.setMessageId(messageId);
        return isSessionEnabled ? message.setSessionId(sessionId) : message;
    }

    protected void logMessage(ServiceBusMessage message, String entity, String description) {
        logMessage(message.getMessageId(), -1, message.getApplicationProperties().get(MESSAGE_POSITION_ID), entity, description);
    }

    protected void logMessage(ServiceBusReceivedMessage message, String entity, String description) {
        if (message == null) {
            logMessage(null, -1, entity, null, description);
        } else {
            logMessage(message.getMessageId(), message.getSequenceNumber(), message.getApplicationProperties().get(MESSAGE_POSITION_ID), entity, description);
        }
    }

    private void logMessage(String id, long seqNo, Object positionId, String entity, String description) {
        logger.atInfo()
            .addKeyValue("test", testName)
            .addKeyValue("entity", entity)
            .addKeyValue("sequenceNo", seqNo)
            .addKeyValue("messageId", id)
            .addKeyValue("positionId", positionId)
            .log(description == null ? "logging messages" : description);
    }

    protected void logMessages(List<ServiceBusMessage> messages, String entity, String description) {
        messages.forEach(m -> logMessage(m.getMessageId(), -1, m.getApplicationProperties().get(MESSAGE_POSITION_ID), entity, description));
    }

    protected List<ServiceBusReceivedMessage> logReceivedMessages(IterableStream<ServiceBusReceivedMessage> messages, String entity, String description) {
        List<ServiceBusReceivedMessage> list = messages.stream().collect(Collectors.toList());
        list.forEach(m -> logMessage(m.getMessageId(), m.getSequenceNumber(), m.getApplicationProperties().get(MESSAGE_POSITION_ID), entity, description));
        return list;
    }

    protected void assertMessageEquals(ServiceBusReceivedMessage message, String messageId, boolean isSessionEnabled) {
        assertNotNull(message, "'message' cannot be null.");
        assertArrayEquals(CONTENTS_BYTES, message.getBody().toBytes());

        // Disabling message ID assertion. Since we do multiple operations on the same queue/topic, it's possible
        // the queue or topic contains messages from previous test cases.
        assertNotNull(message.getMessageId(), "'messageId' cannot be null.");
        //assertEquals(messageId, message.getMessageId());

        if (isSessionEnabled) {
            assertNotNull(message.getSessionId(), "'sessionId' cannot be null.");
            // Disabling session ID exact match assertion. Since we do multiple operations on the same queue/topic, it's possible
            // the queue or topic contains messages from previous test cases.
            // assertEquals(sessionId, message.getSessionId());
        }
    }

    protected ServiceBusClientBuilder getBuilder(boolean useCredentials, boolean sharedConnection) {
        ServiceBusClientBuilder builder;
        if (sharedConnection && sharedBuilder == null) {
            sharedBuilder = getBuilder(useCredentials);
            builder = sharedBuilder;
        } else if (sharedConnection) {
            builder = sharedBuilder;
        } else {
            builder = getBuilder(useCredentials);
        }
        return builder;
    }
}
