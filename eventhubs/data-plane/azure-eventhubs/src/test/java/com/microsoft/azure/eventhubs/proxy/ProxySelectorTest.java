/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProxySelectorTest extends ApiTestBase {

    @Test
    public void proxySelectorConnectFailedInvokeTest() throws Exception {
        // doesn't start proxy server and verifies that the connectFailed callback is invoked.
        int proxyPort = 8899;
        final CompletableFuture<Void> connectFailedTask = new CompletableFuture<>();
        final ProxySelector defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                LinkedList<Proxy> proxies = new LinkedList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyPort)));
                return proxies;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                connectFailedTask.complete(null);
            }
        });

        try {
            ConnectionStringBuilder builder = new ConnectionStringBuilder(TestContext.getConnectionString().toString());
            builder.setTransportType(TransportType.AMQP_WEB_SOCKETS);
            builder.setOperationTimeout(Duration.ofSeconds(10));

            try {
                EventHubClient.createSync(builder.toString(), TestContext.EXECUTOR_SERVICE);
                Assert.assertTrue(false); // shouldn't reach here
            } catch (EventHubException ex) {
                Assert.assertEquals("connection aborted", ex.getMessage());
            }

            connectFailedTask.get(2, TimeUnit.SECONDS);
        } finally {
            ProxySelector.setDefault(defaultProxySelector);
        }
    }
}
