// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.jproxy.ProxyServer;
import com.azure.messaging.eventhubs.jproxy.SimpleProxy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    @BeforeAll
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

    @AfterAll
    public static void cleanupClient() throws Exception {
        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Override
    protected void beforeTest() {
        builder = new EventHubClientBuilder()
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
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
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final Flux<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, messageId);
        final Instant sendTime = Instant.now();
        final EventHubConsumerAsyncClient consumer = builder
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        try {
            // Act
            StepVerifier.create(producer.send(events, options))
                .verifyComplete();

            // Assert
            StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(sendTime))
                .filter(x -> TestUtils.isMatchingEvent(x, messageId)).take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .verifyComplete();
        } finally {
            dispose(producer, consumer);
        }
    }
}
