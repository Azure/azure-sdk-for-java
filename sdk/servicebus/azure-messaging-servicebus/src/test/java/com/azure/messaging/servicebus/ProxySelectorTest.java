// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ProxySelectorTest extends IntegrationTestBase {
    private static final int PROXY_PORT = 9002;
    private static final InetSocketAddress SIMPLE_PROXY_ADDRESS = new InetSocketAddress("localhost", PROXY_PORT);
    private ProxySelector defaultProxySelector;

    public ProxySelectorTest() {
        super(new ClientLogger(ProxySelectorTest.class));
    }

    @Override
    public void beforeTest() {
        defaultProxySelector = ProxySelector.getDefault();
    }

    @Override
    public void afterTest() {
        ProxySelector.setDefault(defaultProxySelector);
    }

    @Disabled("Fix when proxy error is propagated back up to receiver.")
    @Test
    public void proxySelectorConnectFailedInvokeTest() throws InterruptedException {
        final String queueName = getQueueName(9);

        Assertions.assertNotNull(queueName, "'queueName' is not set in environment variable.");

        // doesn't start proxy server and verifies that the connectFailed callback is invoked.
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, SIMPLE_PROXY_ADDRESS));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                countDownLatch.countDown();
            }
        });

        final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromString("Hello"));
        final ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10)))
            .sender()
            .queueName(queueName)
            .buildAsyncClient();

        try {
            StepVerifier.create(sender.sendMessage(message))
                .expectErrorSatisfies(error -> {
                    // The message can vary because it is returned from proton-j, so we don't want to compare against that.
                    // This is a transient error from ExceptionUtil.java: line 67.
                    System.out.println("Error: " + error);
                })
                .verify();
        } finally {
            dispose(sender);
        }

        final boolean awaited = countDownLatch.await(2, TimeUnit.SECONDS);
        Assertions.assertTrue(awaited);
    }
}
