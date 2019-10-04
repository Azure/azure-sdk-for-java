// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.proxy;

import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncConsumer;
import com.azure.messaging.eventhubs.EventHubAsyncProducer;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.TestUtils;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.jproxy.ProxyServer;
import com.azure.messaging.eventhubs.jproxy.SimpleProxy;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    private static final InetSocketAddress SIMPLE_PROXY_ADDRESS = new InetSocketAddress("localhost", PROXY_PORT);
    private static final String PARTITION_ID = "1";
    private static final int NUMBER_OF_EVENTS = 25;

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;
    private EventHubAsyncClient client;

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
        proxyServer = new SimpleProxy(SIMPLE_PROXY_ADDRESS.getHostName(), SIMPLE_PROXY_ADDRESS.getPort());
        proxyServer.start(t -> {
        });

        defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, SIMPLE_PROXY_ADDRESS));
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
        client = new EventHubClientBuilder()
            .transportType(TransportType.AMQP_WEB_SOCKETS)
            .connectionString(getConnectionString())
            .buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(client);
    }

    /**
     * Verifies that we can send some number of events.
     */
    @Ignore("SimpleProxy is creating multiple proxy negotiation handlers, so it is returning garbage."
        + "https://github.com/Azure/azure-sdk-for-java/issues/5694")
    @Test
    public void sendEvents() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final EventHubProducerOptions options = new EventHubProducerOptions().setPartitionId(PARTITION_ID);
        final EventHubAsyncProducer producer = client.createProducer(options);
        final Flux<EventData> events = TestUtils.getEvents(NUMBER_OF_EVENTS, messageId);
        final Instant sendTime = Instant.now();

        // Act
        StepVerifier.create(producer.send(events))
            .verifyComplete();

        // Assert
        final EventHubAsyncConsumer consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME,
            PARTITION_ID, EventPosition.fromEnqueuedTime(sendTime));

        StepVerifier.create(consumer.receive().filter(x -> TestUtils.isMatchingEvent(x, messageId)).take(NUMBER_OF_EVENTS))
            .expectNextCount(NUMBER_OF_EVENTS)
            .verifyComplete();
    }
}
