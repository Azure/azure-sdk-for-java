// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.jproxy.ProxyServer;
import com.azure.messaging.servicebus.jproxy.SimpleProxy;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ProxySendTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 9101;
    private static final int NUMBER_OF_EVENTS = 10;

    private static ProxyServer proxyServer;
    private static ProxySelector defaultProxySelector;

    public ProxySendTest() {
        super(new ClientLogger(ProxySendTest.class));
    }

    @BeforeEach
    public void initialize() throws Exception {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));

        proxyServer = new SimpleProxy(PROXY_PORT);
        proxyServer.start(error -> logger.error("Exception occurred in proxy.", error));

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

    @AfterEach
    public void cleanup() throws Exception {
        StepVerifier.resetDefaultTimeout();

        ProxySelector.setDefault(defaultProxySelector);

        if (proxyServer != null) {
            proxyServer.stop();
        }
    }

    /**
     * Verifies that we can send some number of events.
     */
    @Test
    public void sendEvents() {
        // Arrange
        final String queueName = getQueueName(TestUtils.USE_CASE_PROXY);

        Assertions.assertNotNull(queueName, "'queueName' is not set in environment variable.");

        final String messageId = UUID.randomUUID().toString();

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(NUMBER_OF_EVENTS, messageId);
        final ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .verifyMode(SslDomain.VerifyMode.ANONYMOUS_PEER)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10)))
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        try {
            // Act & Assert
            StepVerifier.create(sender.createMessageBatch()
                .flatMap(batch -> {
                    for (int i = 0; i < messages.size(); i++) {
                        Assertions.assertTrue(batch.tryAddMessage(messages.get(i)), "Unable to add message: " + i);
                    }

                    return sender.sendMessages(batch);
                }))
                .verifyComplete();
        } finally {
            dispose(sender);
        }
    }
}
