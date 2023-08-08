// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
        final int proxyPort = 8899;
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
                EventHubClient.createFromConnectionStringSync(builder.toString(), TestContext.EXECUTOR_SERVICE);
                Assert.fail();
            } catch (EventHubException ex) {
                // The message can vary because it is returned from proton-j, so we don't want to compare against that.
                // This is a transient error from ExceptionUtil.java: line 67.
                Assert.assertTrue(ex.getIsTransient());
            }

            connectFailedTask.get(2, TimeUnit.SECONDS);
        } finally {
            ProxySelector.setDefault(defaultProxySelector);
        }
    }
}
