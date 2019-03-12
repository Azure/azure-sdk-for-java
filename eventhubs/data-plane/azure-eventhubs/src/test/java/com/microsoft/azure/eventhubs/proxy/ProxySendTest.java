/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.lib.SasTokenTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.eventhubs.sendrecv.SendTest;
import org.jutils.jproxy.ProxyServer;
import org.junit.*;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ProxySendTest extends SasTokenTestBase {

    private static int proxyPort = 8899;
    private static ProxyServer proxyServer;
    private static SendTest sendTest;
    private static ProxySelector defaultProxySelector;

    @BeforeClass
    public static void initialize() throws Exception {
        proxyServer = ProxyServer.create("localhost", proxyPort);
        proxyServer.start(t -> {});

        defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                LinkedList<Proxy> proxies = new LinkedList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyPort)));
                return proxies;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // no-op
            }
        });

        Assert.assertTrue(TestContext.getConnectionString().getSharedAccessSignature() != null
                && TestContext.getConnectionString().getSasKey() == null
                && TestContext.getConnectionString().getSasKeyName() == null);

        sendTest = new SendTest();

        ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        connectionString.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        SendTest.initializeEventHub(connectionString);
    }

    @AfterClass
    public static void cleanupClient() throws Exception {

        SendTest.cleanupClient();

        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Test
    public void sendBatchRetainsOrderWithinBatch() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {

        sendTest.sendBatchRetainsOrderWithinBatch();
    }

    @Test
    public void sendResultsInSysPropertiesWithPartitionKey() throws EventHubException, InterruptedException, ExecutionException, TimeoutException {

        sendTest.sendResultsInSysPropertiesWithPartitionKey();
    }

    @After
    public void cleanup() throws Exception {

        sendTest.cleanup();
    }
}
