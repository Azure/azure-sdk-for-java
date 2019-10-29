// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.proxy;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConnection;
import com.azure.messaging.eventhubs.EventHubConsumer;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.TestUtils;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.messaging.eventhubs.EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME;
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

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final ProxyConfiguration proxyConfiguration = getProxyConfiguration();

        Assume.assumeTrue("Cannot run proxy integration tests without setting proxy configuration.",
            proxyConfiguration != null);

        sender = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .retry(new RetryOptions().setMaxRetries(0))
            .proxyConfiguration(proxyConfiguration)
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
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(getConnectionString());
        final EventHubConnection connection = builder.buildConnection();
        builder.connection(connection);

        final EventHubProducerAsyncClient producer = builder.buildAsyncProducer();

        final EventHubConsumer receiver = builder.consumerGroup(DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.earliest())
            .partitionId(PARTITION_ID)
            .buildConsumer();

        producer.send(TestUtils.getEvents(numberOfEvents, messageId), sendOptions).block();

        // Act
        final IterableStream<EventData> receive = receiver.receive(15, Duration.ofSeconds(30));

        // Assert
        Assert.assertNotNull(receive);
        final List<EventData> results = receive.stream().collect(Collectors.toList());
        Assert.assertEquals(numberOfEvents, results.size());
    }
}
