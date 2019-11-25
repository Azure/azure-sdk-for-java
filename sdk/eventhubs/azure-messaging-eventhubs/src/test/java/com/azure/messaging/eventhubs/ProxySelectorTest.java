// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
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
    private static final int PROXY_PORT = 8899;
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

    @Disabled("Exceptions are not propagated to the consumer. https://github.com/Azure/azure-sdk-for-java/issues/4663")
    @Test
    public void proxySelectorConnectFailedInvokeTest() throws InterruptedException {
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

        final EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .retry(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10)))
            .buildAsyncConsumerClient();

        try {
            StepVerifier.create(consumer.receiveFromPartition("1", EventPosition.earliest()).take(1))
                .expectErrorSatisfies(error -> {
                    // The message can vary because it is returned from proton-j, so we don't want to compare against that.
                    // This is a transient error from ExceptionUtil.java: line 67.
                    System.out.println("Error: " + error);
                })
                .verify();
        } finally {
            dispose(consumer);
        }

        final boolean awaited = countDownLatch.await(2, TimeUnit.SECONDS);
        Assertions.assertTrue(awaited);
    }
}
