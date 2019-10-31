// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.proxy;

import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.TestUtils;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.jproxy.ProxyServer;
import com.azure.messaging.eventhubs.jproxy.SimpleProxy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProxySendTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 8899;
    private static final String PARTITION_ID = "1";
    private static final int NUMBER_OF_EVENTS = 25;

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;
    private EventHubClientBuilder builder;

    public ProxySendTest() {
        super(new ClientLogger(ProxySendTest.class));
    }

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @BeforeClass
    public static void initialize() throws Exception {
        proxyServer = new SimpleProxy(PROXY_PORT);
        proxyServer.start(t -> {
        });

        defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, proxyServer.getHost()));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // no-op
            }
        });
    }

    @AfterClass
    public static void cleanupClient() throws Exception {
        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Override
    protected void beforeTest() {
        builder = new EventHubClientBuilder()
            .transportType(TransportType.AMQP_WEB_SOCKETS)
            .connectionString(getConnectionString());
    }

    @Override
    protected void afterTest() {

    }

    /**
     * Verifies that we can send some number of events.
     */
    @Test
    public void sendEvents() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducer();
        final Flux<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, messageId);
        final Instant sendTime = Instant.now();
        final EventHubConsumerAsyncClient consumer = builder
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.fromEnqueuedTime(sendTime))
            .buildAsyncConsumer();

        try {
            // Act
            StepVerifier.create(producer.send(events, options))
                .verifyComplete();

            // Assert
            StepVerifier.create(consumer.receive(PARTITION_ID).filter(x -> TestUtils.isMatchingEvent(x, messageId)).take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .verifyComplete();
        } finally {
            dispose(producer, consumer);
        }
    }
}
