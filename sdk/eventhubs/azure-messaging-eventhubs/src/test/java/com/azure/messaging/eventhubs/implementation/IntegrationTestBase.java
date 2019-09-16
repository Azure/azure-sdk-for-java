// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncProducer;
import com.azure.messaging.eventhubs.EventHubClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducer;
import com.azure.messaging.eventhubs.TestUtils;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Test base for running integration tests.
 */
public abstract class IntegrationTestBase extends TestBase {
    protected static final Duration TIMEOUT = Duration.ofSeconds(30);
    protected static final RetryOptions RETRY_OPTIONS = new RetryOptions().setTryTimeout(TIMEOUT);
    protected final ClientLogger logger;

    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    private ConnectionStringProperties properties;
    private Scheduler scheduler;

    protected IntegrationTestBase(ClientLogger logger) {
        this.logger = logger;
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @Before
    public void setupTest() {
        logger.info("[{}]: Performing integration test set-up.", getTestName());

        skipIfNotRecordMode();

        scheduler = Schedulers.newParallel("AMQPConnection");
        properties = new ConnectionStringProperties(getConnectionString());

        beforeTest();
    }

    // These are overridden because we don't use the Interceptor Manager.
    @Override
    @After
    public void teardownTest() {
        logger.info("[{}]: Performing test clean-up.", getTestName());
        afterTest();

        if (scheduler != null) {
            scheduler.dispose();
        }

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

        return ImplUtils.isNullOrEmpty(CONNECTION_STRING) ? TestMode.PLAYBACK : TestMode.RECORD;
    }

    protected String getConnectionString() {
        return CONNECTION_STRING;
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder} with the default integration test settings.
     */
    protected EventHubClientBuilder createBuilder() {
        return new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .proxyConfiguration(ProxyConfiguration.SYSTEM_DEFAULTS)
            .scheduler(scheduler)
            .retry(RETRY_OPTIONS)
            .transportType(TransportType.AMQP);
    }

    protected ConnectionStringProperties getConnectionStringProperties() {
        return properties;
    }

    /**
     * Pushes a set of {@link EventData} to Event Hubs.
     */
    protected IntegrationTestEventData setupEventTestData(EventHubAsyncClient client, int numberOfEvents,
                                                          EventHubProducerOptions options) {
        final String messageId = UUID.randomUUID().toString();

        logger.info("Pushing events to partition. Message tracking value: {}", messageId);

        final EventHubAsyncProducer producer = client.createProducer(options);
        final List<EventData> events = TestUtils.getEvents(numberOfEvents, messageId).collectList().block();
        final Instant datePushed = Instant.now();

        try {
            producer.send(events).block(TIMEOUT);
        } finally {
            dispose(producer);
        }

        return new IntegrationTestEventData(options.getPartitionId(), messageId, datePushed, events);
    }

    /**
     * Pushes a set of {@link EventData} to Event Hubs.
     */
    protected IntegrationTestEventData setupEventTestData(EventHubClient client, int numberOfEvents,
                                                          EventHubProducerOptions options) {
        final String messageId = UUID.randomUUID().toString();

        logger.info("Pushing events to partition. Message tracking value: {}", messageId);

        final EventHubProducer producer = client.createProducer(options);
        final List<EventData> events = TestUtils.getEvents(numberOfEvents, messageId).collectList().block();
        final Instant datePushed = Instant.now();

        try {
            producer.send(events);
        } finally {
            dispose(producer);
        }

        return new IntegrationTestEventData(options.getPartitionId(), messageId, datePushed, events);
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
                logger.error(String.format("[%s]: %s didn't close properly.",
                    getTestName(), closeable.getClass().getSimpleName()), error);
            }
        }
    }

    private void skipIfNotRecordMode() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);
    }
}
