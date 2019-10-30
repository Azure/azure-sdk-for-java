// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.proxy;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.implementation.IntegrationTestBase;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
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

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String getTestName() {
        return testName.getMethodName();
    }

    @Override
    public void setupTest() {
        defaultProxySelector = ProxySelector.getDefault();
    }

    @Override
    public void teardownTest() {
        ProxySelector.setDefault(defaultProxySelector);
    }

    @Ignore("Exceptions are not propagated to the consumer. https://github.com/Azure/azure-sdk-for-java/issues/4663")
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

        final EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(getConnectionString())
            .transportType(TransportType.AMQP_WEB_SOCKETS)
            .retry(new RetryOptions().setTryTimeout(Duration.ofSeconds(10)))
            .buildAsyncClient();

        final EventHubConsumerAsyncClient consumer = client.createConsumer(EventHubAsyncClient.DEFAULT_CONSUMER_GROUP_NAME,
            "1", EventPosition.earliest());

        StepVerifier.create(consumer.receive().take(1))
            .expectErrorSatisfies(error -> {
                // The message can vary because it is returned from proton-j, so we don't want to compare against that.
                // This is a transient error from ExceptionUtil.java: line 67.
                System.out.println("Error: " + error);
            })
            .verify();

        final boolean awaited = countDownLatch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(awaited);
    }
}
