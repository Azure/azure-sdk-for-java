// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.jproxy.ProxyServer;
import com.azure.messaging.eventhubs.jproxy.SimpleProxy;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies we can publish events through a proxy.
 */
@Tag(TestUtils.INTEGRATION)
class ProxySendTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 8999;
    private static final String PARTITION_ID = "3";
    private static final int NUMBER_OF_EVENTS = 25;
    private static final ClientLogger LOGGER = new ClientLogger(ProxySendTest.class);

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;

    private EventHubClientBuilder builder;

    ProxySendTest() {
        super(new ClientLogger(ProxySendTest.class));
    }

    @BeforeAll
    static void initialize() throws Exception {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

        proxyServer = new SimpleProxy(PROXY_PORT);
        proxyServer.start(t -> LOGGER.error("Starting proxy server failed.", t));

        defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                LOGGER.info("Returning proxy '{}' for URI: {}", proxyServer.getHost(), uri);
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, proxyServer.getHost()));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                LOGGER.error("Failed to connect. URI: {}. Address: {}", uri, sa, ioe);
            }
        });
    }

    @AfterAll
    static void cleanupClient() throws IOException {
        try {
            if (proxyServer != null) {
                proxyServer.stop();
            }
        } finally {
            ProxySelector.setDefault(defaultProxySelector);
            StepVerifier.resetDefaultTimeout();
        }
    }

    @Override
    protected void beforeTest() {
        builder = new EventHubClientBuilder()
            .verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .connectionString(getConnectionString());
    }

    /**
     * Verifies that we can send some number of events.
     */
    @Test
    void sendEvents() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final List<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, messageId);
        final PartitionProperties information = producer.getPartitionProperties(PARTITION_ID).block();

        assertNotNull(information, "Should receive partition information.");

        final EventPosition position = EventPosition.fromSequenceNumber(information.getLastEnqueuedSequenceNumber());
        final EventHubConsumerAsyncClient consumer = builder
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        try {
            // Act
            StepVerifier.create(producer.send(events, options))
                .expectComplete()
                .verify(TIMEOUT);

            // Assert
            logger.info("Waiting to receive events.");
            StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, position)
                .filter(x -> TestUtils.isMatchingEvent(x, messageId)).take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(producer, consumer);
        }
    }
}
