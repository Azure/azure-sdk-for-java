// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.experimental.util.tracing.LoggingTracerProvider;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.ProxyOptions.PROXY_PASSWORD;
import static com.azure.core.amqp.ProxyOptions.PROXY_USERNAME;

/**
 * Test base for running integration tests.
 */
public abstract class IntegrationTestBase extends TestBase {
    // The number of partitions we create in test-resources.json.
    // Partitions 0 and 1 are used for consume-only operations. 2, 3, and 4 are used to publish or consume events.
    protected static final int NUMBER_OF_PARTITIONS = 5;
    protected static final List<String> EXPECTED_PARTITION_IDS = IntStream.range(0, NUMBER_OF_PARTITIONS)
        .mapToObj(String::valueOf)
        .collect(Collectors.toList());
    protected static final Duration TIMEOUT = Duration.ofMinutes(1);

    // Tests use timeouts of 20-60 seconds to verify something has happened
    // We need a short try timeout so that if transient issue happens we have a chance to retry it before overall test timeout.
    // This is a good idea to do in any production application as well - no point in waiting too long
    protected static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(3));

    protected final ClientLogger logger;

    private static final String PROXY_AUTHENTICATION_TYPE = "PROXY_AUTHENTICATION_TYPE";

    private static final String AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME = "AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME";
    private static final String AZURE_EVENTHUBS_EVENT_HUB_NAME = "AZURE_EVENTHUBS_EVENT_HUB_NAME";
    private static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();
    private static final ClientOptions OPTIONS_WITH_TRACING = new ClientOptions().setTracingOptions(new LoggingTracerProvider.LoggingTracingOptions());

    private static Scheduler scheduler;
    private static Map<String, IntegrationTestEventData> testEventData;
    private List<AutoCloseable> toClose = new ArrayList<>();
    protected String testName;

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    @BeforeAll
    public static void beforeAll() {
        scheduler = Schedulers.newParallel("eh-integration");
    }

    @AfterAll
    public static void afterAll() {
        scheduler.dispose();
    }

    @BeforeEach
    @Override
    public void setupTest(TestContextManager testContextManager) {
        logger.info("----- {}: Performing integration test set-up. -----",
            testContextManager.getTestPlaybackRecordingName());

        testName = testContextManager.getTrackerTestName();
        skipIfNotRecordMode();
        toClose = new ArrayList<>();
        beforeTest();
    }

    protected <T extends AutoCloseable> T toClose(T closeable) {
        toClose.add(closeable);
        return closeable;
    }

    protected Disposable toClose(Disposable closeable) {
        toClose.add(() -> closeable.dispose());
        return closeable;
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    public void teardownTest() {
        logger.info("----- {}: Performing test clean-up. -----", testName);
        afterTest();

        logger.info("Disposing of subscriptions, consumers and clients.");
        dispose();

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Gets the configured ProxyConfiguration from environment variables.
     */
    protected ProxyOptions getProxyConfiguration() {
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

    protected static String getFullyQualifiedDomainName() {
        return GLOBAL_CONFIGURATION.get(AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME);
    }

    protected static String getEventHubName() {
        return GLOBAL_CONFIGURATION.get(AZURE_EVENTHUBS_EVENT_HUB_NAME);
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate.
     */
    protected static EventHubClientBuilder createBuilder() {
        return createBuilder(false);
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate if {@code useCredentials} is false. Otherwise, uses a service principal through
     * {@link com.azure.identity.ClientSecretCredential}.
     */
    protected static EventHubClientBuilder createBuilder(boolean useCredentials) {
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retry(RETRY_OPTIONS)
            .clientOptions(OPTIONS_WITH_TRACING)
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler);

        if (useCredentials) {
            final ConnectionStringProperties properties = TestUtils.getConnectionStringProperties();
            final String fqdn = properties.getEndpoint().getHost();
            final String eventHubName = properties.getEntityPath();

            Assumptions.assumeTrue(fqdn != null && !fqdn.isEmpty(), AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME + " variable needs to be set when using credentials.");
            Assumptions.assumeTrue(eventHubName != null && !eventHubName.isEmpty(), AZURE_EVENTHUBS_EVENT_HUB_NAME + " variable needs to be set when using credentials.");

            final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(GLOBAL_CONFIGURATION.get("AZURE_CLIENT_ID"))
                .clientSecret(GLOBAL_CONFIGURATION.get("AZURE_CLIENT_SECRET"))
                .tenantId(GLOBAL_CONFIGURATION.get("AZURE_TENANT_ID"))
                .build();

            return builder.credential(fqdn, eventHubName, clientSecretCredential);
        } else {
            return builder.connectionString(TestUtils.getConnectionString());
        }
    }

    /**
     * Gets or creates the integration test data.
     */
    protected synchronized Map<String, IntegrationTestEventData> getTestData() {
        if (testEventData != null) {
            return testEventData;
        }

        logger.info("--> Adding events to Event Hubs.");
        final Map<String, IntegrationTestEventData> integrationData = new HashMap<>();

        try (EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(TestUtils.getConnectionString())
            .retryOptions(RETRY_OPTIONS)
            .clientOptions(OPTIONS_WITH_TRACING)
            .buildProducerClient()) {

            producer.getPartitionIds().forEach(partitionId -> {
                logger.info("--> Adding events to partition: " + partitionId);
                final PartitionProperties partitionProperties = producer.getPartitionProperties(partitionId);
                final String messageId = UUID.randomUUID().toString();
                final int numberOfEvents = 15;
                final List<EventData> events = TestUtils.getEvents(numberOfEvents, messageId);
                final SendOptions options = new SendOptions().setPartitionId(partitionId);

                producer.send(events, options);

                integrationData.put(partitionId,
                    new IntegrationTestEventData(partitionId, partitionProperties, messageId, events));
            });

            if (integrationData.size() != NUMBER_OF_PARTITIONS) {
                logger.warning("--> WARNING: Number of partitions is different. Expected: {}. Actual {}",
                    NUMBER_OF_PARTITIONS, integrationData.size());
            }

            testEventData = Collections.unmodifiableMap(integrationData);
        }

        Assertions.assertNotNull(testEventData, "'testEventData' should have been set.");
        Assertions.assertFalse(testEventData.isEmpty(), "'testEventData' should not be empty.");
        return testEventData;
    }

    /**
     * Disposes of any {@link Closeable} resources.
     *
     * @param closeables The closeables to dispose of. If a closeable is {@code null}, it is skipped.
     */
    protected void dispose(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        for (final Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }

            try {
                closeable.close();
            } catch (IOException error) {
                logger.error("[{}]: {} didn't close properly.", testName, closeable.getClass().getSimpleName(), error);
            }
        }
    }

    /**
     * Disposes of registered with {@code toClose} method resources.
     */
    protected void dispose() {
        dispose(toClose.toArray(new Closeable[0]));
        toClose.clear();
    }

    private void skipIfNotRecordMode() {
        Assumptions.assumeTrue(getTestMode() != TestMode.PLAYBACK, "Is not in RECORD/LIVE mode.");
    }
}
