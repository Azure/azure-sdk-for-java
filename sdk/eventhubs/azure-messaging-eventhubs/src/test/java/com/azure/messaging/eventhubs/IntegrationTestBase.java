// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
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
    protected static final Duration TIMEOUT = Duration.ofSeconds(30);
    protected static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(TIMEOUT);

    protected final ClientLogger logger;

    private static final String PROXY_AUTHENTICATION_TYPE = "PROXY_AUTHENTICATION_TYPE";
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String EVENT_HUB_CONNECTION_STRING_WITH_SAS = "AZURE_EVENTHUBS_CONNECTION_STRING_WITH_SAS";

    private static final String AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME = "AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME";
    private static final String AZURE_EVENTHUBS_EVENT_HUB_NAME = "AZURE_EVENTHUBS_EVENT_HUB_NAME";
    private static final Object LOCK = new Object();

    private static Scheduler scheduler;
    private static Map<String, IntegrationTestEventData> testEventData;

    private String testName;

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    @BeforeAll
    public static void beforeAll() {
        scheduler = Schedulers.newParallel("eh-integration");
        StepVerifier.setDefaultTimeout(TIMEOUT);
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
        scheduler.dispose();
    }

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        System.out.printf("----- [%s]: Performing integration test set-up. -----%n", testInfo.getDisplayName());

        testName = testInfo.getDisplayName();
        skipIfNotRecordMode();

        beforeTest();
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @AfterEach
    public void teardownTest(TestInfo testInfo) {
        System.out.printf("----- [%s]: Performing test clean-up. -----%n", testInfo.getDisplayName());
        afterTest();

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMocks();
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

    static String getConnectionString() {
        return getConnectionString(false);
    }

    static String getConnectionString(boolean withSas) {
        if (withSas) {
            return System.getenv(EVENT_HUB_CONNECTION_STRING_WITH_SAS);
        }
        return System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);
    }

    /**
     * Gets the configured ProxyConfiguration from environment variables.
     */
    protected ProxyOptions getProxyConfiguration() {
        final String address = System.getenv(Configuration.PROPERTY_HTTP_PROXY);

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

        final String username = System.getenv(PROXY_USERNAME);

        if (username == null) {
            logger.info("Environment variable '{}' is not set. No authentication used.");
            return new ProxyOptions(ProxyAuthenticationType.NONE, proxy, null, null);
        }

        final String password = System.getenv(PROXY_PASSWORD);
        final String authentication = System.getenv(PROXY_AUTHENTICATION_TYPE);

        final ProxyAuthenticationType authenticationType = CoreUtils.isNullOrEmpty(authentication)
            ? ProxyAuthenticationType.NONE
            : ProxyAuthenticationType.valueOf(authentication);

        return new ProxyOptions(authenticationType, proxy, username, password);
    }

    protected static String getFullyQualifiedDomainName() {
        return System.getenv(AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME);
    }

    protected static String getEventHubName() {
        return System.getenv(AZURE_EVENTHUBS_EVENT_HUB_NAME);
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
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler);

        if (useCredentials) {
            final String fqdn = getFullyQualifiedDomainName();
            final String eventHubName = getEventHubName();

            Assumptions.assumeTrue(fqdn != null && !fqdn.isEmpty(), AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME + " variable needs to be set when using credentials.");
            Assumptions.assumeTrue(eventHubName != null && !eventHubName.isEmpty(), AZURE_EVENTHUBS_EVENT_HUB_NAME + " variable needs to be set when using credentials.");

            final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(System.getenv("AZURE_CLIENT_ID"))
                .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
                .tenantId(System.getenv("AZURE_TENANT_ID"))
                .build();

            return builder.credential(fqdn, eventHubName, clientSecretCredential);
        } else {
            return builder.connectionString(getConnectionString());
        }
    }

    protected static ConnectionStringProperties getConnectionStringProperties() {
        return new ConnectionStringProperties(getConnectionString());
    }

    /**
     * Gets or creates the integration test data.
     */
    protected static Map<String, IntegrationTestEventData> getTestData() {
        if (testEventData != null) {
            return testEventData;
        }

        synchronized (LOCK) {
            if (testEventData != null) {
                return testEventData;
            }

            System.out.println("--> Adding events to Event Hubs.");
            final Map<String, IntegrationTestEventData> integrationData = new HashMap<>();

            try (EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(getConnectionString())
                .buildProducerClient()) {

                producer.getPartitionIds().forEach(partitionId -> {
                    System.out.printf("--> Adding events to partition: %s%n", partitionId);
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
                    System.out.printf("--> WARNING: Number of partitions is different. Expected: %s. Actual %s%n",
                        NUMBER_OF_PARTITIONS, integrationData.size());
                }

                testEventData = Collections.unmodifiableMap(integrationData);
            }
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
                logger.error(String.format("[%s]: %s didn't close properly.", testName,
                    closeable.getClass().getSimpleName()), error);
            }
        }
    }

    private void skipIfNotRecordMode() {
        Assumptions.assumeTrue(getTestMode() != TestMode.PLAYBACK);
    }
}
