// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Tests simple receive and send scenarios through proxy. Requires that {@link Configuration#PROPERTY_HTTP_PROXY}
 * is set.
 */
public class ProxyIntegrationTest extends IntegrationTestBase {
    private static final String PARTITION_ID = "0";

    private EventHubProducerClient sender;
    private SendOptions sendOptions;

    public ProxyIntegrationTest() {
        super(new ClientLogger(ProxyIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final ProxyOptions proxyOptions = getProxyConfiguration();

        Assumptions.assumeTrue(proxyOptions != null, "Cannot run proxy integration tests without setting proxy configuration.");

        sender = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(new RetryOptions().setMaxRetries(0))
            .proxyConfiguration(proxyOptions)
            .transportType(TransportType.AMQP_WEB_SOCKETS)
            .buildProducer();

        sendOptions = new SendOptions().setPartitionId(PARTITION_ID);
    }

    @Override
    protected void afterTest() {
        dispose(sender);
    }

    /**
     * Verifies we can send events through the proxy.
     */
    @Test
    public void send() {
        sender.send(new EventData("Hello".getBytes(UTF_8)), sendOptions);
    }

    /**
     * Verifies we can receive events through the proxy.
     */
    @Test
    public void receive() {
        // Arrange
        final int numberOfEvents = 15;
        final String messageId = UUID.randomUUID().toString();
        final EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(getConnectionString()).buildAsyncProducer();
        final EventHubConsumerClient receiver = new EventHubClientBuilder()
                .connectionString(getConnectionString())
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .startingPosition(EventPosition.earliest())
                .buildConsumer();

        producer.send(TestUtils.getEvents(numberOfEvents, messageId), sendOptions).block();

        // Act
        final IterableStream<PartitionEvent> receive = receiver.receive(PARTITION_ID, 15, Duration.ofSeconds(30));

        // Assert
        Assertions.assertNotNull(receive);
        final List<PartitionEvent> results = receive.stream().collect(Collectors.toList());
        Assertions.assertEquals(numberOfEvents, results.size());
    }
}
