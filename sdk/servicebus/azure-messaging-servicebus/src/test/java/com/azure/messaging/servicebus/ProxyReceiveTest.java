// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.jproxy.ProxyServer;
import com.azure.messaging.servicebus.jproxy.SimpleProxy;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
import java.util.UUID;

/**
 * Verify we can use jproxy hosted locally to receive messages.
 */
public class ProxyReceiveTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 9340;
    private static final int NUMBER_OF_EVENTS = 25;

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;

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

    @Test
    public void testReceiverStartOfStreamFilters() {
        // Arrange
        final String queueName = getQueueName();

        Assertions.assertNotNull(queueName, "'queueName' is not set in environment variable.");

        final String messageTracking = UUID.randomUUID().toString();
        final ServiceBusClientBuilder builder = new ServiceBusClientBuilder()
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .connectionString(getConnectionString());

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(NUMBER_OF_EVENTS, messageTracking);
        final ServiceBusSenderAsyncClient sender = builder.buildSenderClientBuilder()
            .entityName(queueName)
            .buildAsyncClient();

        final ServiceBusReceiverAsyncClient receiver = builder.buildReceiverClientBuilder()
            .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
            .queueName(queueName)
            .isAutoComplete(false)
            .buildAsyncClient();

        // Act & Assert
        try {
            StepVerifier.create(sender.createBatch()
                .flatMap(batch -> {
                    for (int i = 0; i < messages.size(); i++) {
                        Assertions.assertTrue(batch.tryAdd(messages.get(i)), "Unable to add message: " + i);
                    }

                    return sender.send(batch);
                }))
                .verifyComplete();

            StepVerifier.create(receiver.receive().take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(sender, receiver);
        }
    }
}
