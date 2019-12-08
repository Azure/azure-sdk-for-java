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
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Verify we can use jproxy hosted locally to receive messages.
 */
public class ProxyReceiveTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 8899;
    private static final AtomicBoolean HAS_PUSHED_EVENTS = new AtomicBoolean();
    private static final String PARTITION_ID = "0";
    private static final int NUMBER_OF_EVENTS = 25;

    private static IntegrationTestEventData testData;
    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;

    private EventHubConsumerAsyncClient consumer;

    public ProxyReceiveTest() {
        super(new ClientLogger(ProxyReceiveTest.class));
    }

    @BeforeAll
    public static void setup() throws IOException {
        proxyServer = new SimpleProxy(PROXY_PORT);
        proxyServer.start(null);

        defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                List<Proxy> proxies = new ArrayList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, proxyServer.getHost()));
                return proxies;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // no-op
            }
        });
    }

    @AfterAll()
    public static void cleanup() throws Exception {
        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Override
    protected void beforeTest() {
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .shareConnection()
            .connectionString(getConnectionString());

        if (HAS_PUSHED_EVENTS.getAndSet(true)) {
            logger.info("Already pushed events to partition. Skipping.");
        } else {
            final SendOptions options = new SendOptions().setPartitionId(PARTITION_ID);
            testData = setupEventTestData(builder.buildAsyncProducerClient(), NUMBER_OF_EVENTS, options);
        }

        consumer = builder.consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
    }

    @Override
    protected void afterTest() {
        dispose(consumer);
    }

    @Test
    public void testReceiverStartOfStreamFilters() {
        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(PARTITION_ID, EventPosition.fromEnqueuedTime(testData.getEnqueuedTime()))
            .take(NUMBER_OF_EVENTS))
            .expectNextCount(NUMBER_OF_EVENTS)
            .verifyComplete();
    }
}
