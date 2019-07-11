// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.ApiTestBase;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EventHubProducerIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "1";

    private EventHubClient client;

    public EventHubProducerIntegrationTest() {
        super(new ClientLogger(EventHubProducerIntegrationTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(getReactorProvider());
        final ConnectionStringProperties properties = new ConnectionStringProperties(getConnectionString());
        final ConnectionOptions connectionOptions = new ConnectionOptions(properties.endpoint().getHost(),
            properties.eventHubPath(), getTokenCredential(), getAuthorizationType(), TIMEOUT, TransportType.AMQP,
            Retry.getNoRetry(), ProxyConfiguration.SYSTEM_DEFAULTS, Schedulers.newSingle("single-threaded"));

        client = new EventHubClient(connectionOptions, getReactorProvider(), handlerProvider);
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Verifies that we can create and send a message to an Event Hub partition.
     */
    @Test
    public void sendMessageToPartition() throws IOException {
        skipIfNotRecordMode();

        // Arrange
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(PARTITION_ID);
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer(producerOptions)) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }

    /**
     * Verifies that we can create an {@link EventHubProducer} that does not care about partitions and lets the service
     * distribute the events.
     */
    @Test
    public void sendMessage() throws IOException {
        skipIfNotRecordMode();

        // Arrange
        final List<EventData> events = Arrays.asList(
            new EventData("Event 1".getBytes(UTF_8)),
            new EventData("Event 2".getBytes(UTF_8)),
            new EventData("Event 3".getBytes(UTF_8)));

        // Act & Assert
        try (EventHubProducer producer = client.createProducer()) {
            StepVerifier.create(producer.send(events))
                .verifyComplete();
        }
    }
}
