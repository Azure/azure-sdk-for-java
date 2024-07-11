// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.credential.TokenCredential;
import com.azure.core.experimental.util.tracing.LoggingTracerProvider;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
    // Tests use timeouts of 20-60 seconds to verify something has happened
    // We need a short try timeout so that if transient issue happens we have a chance to retry it before overall test timeout.
    // This is a good idea to do in any production application as well - no point in waiting too long
    protected static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(3));
    protected final ClientLogger logger;
    protected ClientOptions optionsWithTracing;
    private static final String PROXY_AUTHENTICATION_TYPE = "PROXY_AUTHENTICATION_TYPE";
    private static final Configuration GLOBAL_CONFIGURATION = TestUtils.getGlobalConfiguration();
    private List<AutoCloseable> toClose = new ArrayList<>();
    private String testName;
    private final Scheduler scheduler = Schedulers.parallel();
    private final AtomicReference<TokenCredential> credentialCached = new AtomicReference<>();

    protected static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    protected String sessionId;

    ServiceBusClientBuilder sharedBuilder;

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    @BeforeEach
    @Override
    public void setupTest(TestContextManager testContextManager) {
        this.testContextManager = testContextManager;
        testName = testContextManager.getTrackerTestName();

        logger.info("========= SET-UP [{}] =========", testName);

        assertRunnable();

        toClose = new ArrayList<>();
        optionsWithTracing = new ClientOptions().setTracingOptions(new LoggingTracerProvider.LoggingTracingOptions());
        beforeTest();
    }

    // These are overridden because we don't use the Interceptor Manager.
    @AfterEach
    @Override
    public void teardownTest() {
        logger.info("========= TEARDOWN [{}] =========", testName);
        afterTest();

        logger.info("Disposing of subscriptions, consumers and clients.");
        dispose();
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
     * Creates a new instance of {@link ServiceBusClientBuilder} with authentication set up in {@link TestMode#LIVE} and
     * {@link TestMode#RECORD} modes.
     *
     * @return the builder with authentication set up.
     * @throws org.opentest4j.TestAbortedException if the test mode is {@link TestMode#PLAYBACK}.
     */
    protected ServiceBusClientBuilder getAuthenticatedBuilder() {
        final TestMode mode = super.getTestMode();
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        if (mode == TestMode.LIVE) {
            final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName(false);
            assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");
            final TokenCredential credential = TestUtils.getPipelineCredential(credentialCached);
            return builder.credential(fullyQualifiedDomainName, credential);
        } else if (mode == TestMode.RECORD) {
            final String connectionString = TestUtils.getConnectionString(false);
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName(false);
                assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");
                final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                return builder.credential(fullyQualifiedDomainName, credential);
            } else {
                return builder.connectionString(connectionString);
            }
        } else {
            // Throws org.opentest4j.TestAbortedException exception.
            assumeTrue(false, "Integration tests are not enabled in playback mode.");
            return null;
        }
    }

    /**
     * Creates a new instance of {@link ServiceBusClientBuilder} with the default integration test settings.
     */
    protected ServiceBusClientBuilder getBuilder() {
        return getAuthenticatedBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retryOptions(RETRY_OPTIONS)
            .clientOptions(optionsWithTracing)
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler)
            .configuration(v1OrV2(true));
    }

    protected ServiceBusClientBuilder getBuilder(boolean sharedConnection) {
        ServiceBusClientBuilder builder;
        if (sharedConnection && sharedBuilder == null) {
            sharedBuilder = getBuilder();
            builder = sharedBuilder;
        } else if (sharedConnection) {
            builder = sharedBuilder;
        } else {
            builder = getBuilder();
        }
        return builder;
    }

    protected ServiceBusSenderClientBuilder getSenderBuilder(MessagingEntityType entityType,
        int entityIndex, boolean isSessionAware, boolean sharedConnection) {

        ServiceBusClientBuilder builder = getBuilder(sharedConnection);
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

    protected ServiceBusReceiverClientBuilder getReceiverBuilder(MessagingEntityType entityType,
        int entityIndex, boolean sharedConnection) {

        ServiceBusClientBuilder builder = getBuilder(sharedConnection);
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

    protected ServiceBusSessionReceiverClientBuilder getSessionReceiverBuilder(MessagingEntityType entityType,
        int entityIndex, boolean sharedConnection, AmqpRetryOptions retryOptions) {

        ServiceBusClientBuilder builder = getBuilder(sharedConnection);

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

    protected final Configuration v1OrV2(boolean isV2) {
        final TestConfigurationSource configSource = new TestConfigurationSource();
        if (isV2) {
            configSource.put("com.azure.messaging.servicebus.nonSession.asyncReceive.v2", "true");
            configSource.put("com.azure.messaging.servicebus.nonSession.syncReceive.v2", "true");
            configSource.put("com.azure.messaging.servicebus.session.processor.asyncReceive.v2", "true");
            configSource.put("com.azure.messaging.servicebus.session.reactor.asyncReceive.v2", "true");
            configSource.put("com.azure.messaging.servicebus.session.syncReceive.v2", "true");
            configSource.put("com.azure.messaging.servicebus.sendAndManageRules.v2", "true");
        } else {
            configSource.put("com.azure.messaging.servicebus.nonSession.asyncReceive.v2", "false");
            configSource.put("com.azure.messaging.servicebus.nonSession.syncReceive.v2", "false");
            configSource.put("com.azure.messaging.servicebus.session.processor.asyncReceive.v2", "false");
            configSource.put("com.azure.messaging.servicebus.session.reactor.asyncReceive.v2", "false");
            configSource.put("com.azure.messaging.servicebus.session.syncReceive.v2", "false");
            configSource.put("com.azure.messaging.servicebus.sendAndManageRules.v2", "false");
        }
        return new ConfigurationBuilder(configSource)
            .build();
    }

    /**
     * Asserts that if the integration tests can be run. This method is expected to be called at the beginning of each
     * test run.
     *
     * @throws org.opentest4j.TestAbortedException if the integration tests cannot be run.
     */
    protected void assertRunnable() {
        final TestMode mode = super.getTestMode();
        if (mode == TestMode.PLAYBACK) {
            // AMQP traffic never gets recorded so there is no PLAYBACK supported.
            assumeTrue(false, "Skipping integration tests in playback mode.");
            return;
        }

        if (mode == TestMode.RECORD) {
            // RECORD mode used in SDK-dev setup not on CI pipeline.
            if (!CoreUtils.isNullOrEmpty(TestUtils.getConnectionString(false))) {
                // integration tests are runnable using the connection string.
                return;
            }
            if (!CoreUtils.isNullOrEmpty(TestUtils.getFullyQualifiedDomainName(false))) {
                // best effort check:
                // integration tests are potentially runnable using DefaultAzureCredential. Here we're assuming that
                // in RECORD mode with FullyQualifiedDomainName set, the dev environment is also set up for one of the
                // token credential type in DefaultAzureCredential (all token credentials requires FullyQualifiedDomainName).
                return;
            }
            assumeTrue(false, "Not running integration in record mode (missing authentication set up).");
            return;
        }
        // The CI pipeline is expected to have federated identity configured, so tests are runnable.
        assert mode == TestMode.LIVE;
    }
}
