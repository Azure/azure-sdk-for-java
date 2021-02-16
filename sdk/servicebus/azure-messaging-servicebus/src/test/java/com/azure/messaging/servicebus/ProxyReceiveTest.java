// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.jproxy.ProxyServer;
import com.azure.messaging.servicebus.jproxy.SimpleProxy;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.engine.SslDomain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

/**
 * Verify we can use jproxy hosted locally to receive messages.
 */
public class ProxyReceiveTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 9102;
    private static final int NUMBER_OF_EVENTS = 10;

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;

    public ProxyReceiveTest() {
        super(new ClientLogger(ProxyReceiveTest.class));
    }

    @BeforeEach
    public void setup() throws IOException {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

        proxyServer = new SimpleProxy(PROXY_PORT);
        proxyServer.start(error -> logger.error("Exception occurred in proxy.", error));

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

    @AfterEach()
    public void cleanup() throws Exception {
        StepVerifier.resetDefaultTimeout();

        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Test
    public void receiveMessage() {
        // Arrange
        final String queueName = getQueueName(0);

        Assertions.assertNotNull(queueName, "'queueName' is not set in environment variable.");

        final String messageTracking = UUID.randomUUID().toString();

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(NUMBER_OF_EVENTS, messageTracking);
        final ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER)
            .connectionString(getConnectionString())

            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        final ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER)
            .connectionString(getConnectionString())
            .receiver()
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .queueName(queueName)
            .buildAsyncClient();

        // Act & Assert
        try {
            StepVerifier.create(sender.createMessageBatch()
                .flatMap(batch -> {
                    for (int i = 0; i < messages.size(); i++) {
                        Assertions.assertTrue(batch.tryAddMessage(messages.get(i)), "Unable to add message: " + i);
                    }

                    return sender.sendMessages(batch);
                }))
                .verifyComplete();

            StepVerifier.create(receiver.receiveMessages().take(NUMBER_OF_EVENTS))
                .expectNextCount(NUMBER_OF_EVENTS)
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            dispose(sender, receiver);
        }
    }
}
