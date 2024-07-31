// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.experimental.util.tracing.LoggingTracerProvider;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    protected String testName;

    private static final ClientOptions OPTIONS_WITH_TRACING = new ClientOptions().setTracingOptions(new LoggingTracerProvider.LoggingTracingOptions());

    private Scheduler scheduler;
    private static Map<String, IntegrationTestEventData> testEventData;
    private List<AutoCloseable> toClose = new ArrayList<>();

    private final AtomicReference<TokenCredential> credentialCached = new AtomicReference<>();

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    @BeforeEach
    @Override
    public void setupTest(TestContextManager testContextManager) {
        logger.info("----- {}: Performing integration test set-up. -----",
            testContextManager.getTestPlaybackRecordingName());

        testName = testContextManager.getTrackerTestName();
        skipIfNotRecordMode();
        toClose = new ArrayList<>();

        scheduler = Schedulers.newParallel("eh-integration");
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
        scheduler.dispose();
        logger.info("Disposing of subscriptions, consumers and clients.");
        dispose();

        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate.
     */
    protected EventHubClientBuilder createBuilder() {
        return createBuilder(false);
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings and uses a
     * connection string to authenticate if {@code useCredentials} is false. Otherwise, uses a service principal through
     * {@link com.azure.identity.ClientSecretCredential}.
     */
    protected EventHubClientBuilder createBuilder(boolean shareConnection) {
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .proxyOptions(ProxyOptions.SYSTEM_DEFAULTS)
            .retryOptions(RETRY_OPTIONS)
            .clientOptions(OPTIONS_WITH_TRACING)
            .transportType(AmqpTransportType.AMQP)
            .scheduler(scheduler);

        final String fullyQualifiedDomainName = TestUtils.getFullyQualifiedDomainName();
        final String eventHubName = TestUtils.getEventHubName();

        if (shareConnection) {
            builder.shareConnection();
        }

        switch (getTestMode()) {
            case PLAYBACK:
                Assumptions.assumeTrue(false, "Integration tests are not enabled in playback mode.");
                return null;

            case LIVE:
                Assumptions.assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");
                Assumptions.assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "EventHubName is not set.");

                final TokenCredential credential = TestUtils.getPipelineCredential(credentialCached);
                return builder.credential(fullyQualifiedDomainName, eventHubName, credential);

            case RECORD:
                final String connectionString = TestUtils.getConnectionString(false);

                Assumptions.assumeTrue(!CoreUtils.isNullOrEmpty(eventHubName), "EventHubName is not set.");
                Assumptions.assumeTrue(!CoreUtils.isNullOrEmpty(fullyQualifiedDomainName), "FullyQualifiedDomainName is not set.");

                if (CoreUtils.isNullOrEmpty(connectionString)) {
                    final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
                    return builder.credential(fullyQualifiedDomainName, eventHubName, tokenCredential);
                } else {
                    return builder
                        .eventHubName(eventHubName)
                        .connectionString(connectionString);
                }
            default:
                return null;
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

        try (EventHubProducerClient producer = createBuilder().buildProducerClient()) {

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
        if (closeables == null) {
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
