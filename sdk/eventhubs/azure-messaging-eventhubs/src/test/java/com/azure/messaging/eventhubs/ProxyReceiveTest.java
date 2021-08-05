// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.jproxy.ProxyServer;
import com.azure.messaging.eventhubs.jproxy.SimpleProxy;
import com.azure.messaging.eventhubs.models.EventPosition;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Verify we can use jproxy hosted locally to receive messages.
 */
@Tag(TestUtils.INTEGRATION)
public class ProxyReceiveTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 8899;

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;

    public ProxyReceiveTest() {
        super(new ClientLogger(ProxyReceiveTest.class));
    }

    @BeforeAll
    public static void setup() throws IOException {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

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
        try {
            if (proxyServer != null) {
                proxyServer.stop();
            }
        } finally {
            ProxySelector.setDefault(defaultProxySelector);
            StepVerifier.resetDefaultTimeout();
        }
    }

    @Test
    public void testReceiverStartOfStreamFilters() {
        final EventHubConsumerAsyncClient consumer = createBuilder()
            .verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
        final String partitionId = "3";
        final IntegrationTestEventData integrationTestEventData = getTestData().get(partitionId);
        final PartitionProperties properties = integrationTestEventData.getPartitionProperties();

        final EventPosition position = EventPosition.fromSequenceNumber(properties.getLastEnqueuedSequenceNumber());

        // Act & Assert
        StepVerifier.create(consumer.receiveFromPartition(partitionId, position)
            .take(integrationTestEventData.getEvents().size()))
            .expectNextCount(integrationTestEventData.getEvents().size())
            .expectComplete()
            .verify(TIMEOUT);
    }
}
