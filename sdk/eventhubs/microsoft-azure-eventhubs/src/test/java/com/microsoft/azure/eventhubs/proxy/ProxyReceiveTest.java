// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.jproxy.ProxyServer;
import com.microsoft.azure.eventhubs.lib.SasTokenTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import com.microsoft.azure.eventhubs.sendrecv.ReceiveTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class ProxyReceiveTest extends SasTokenTestBase {
    private static final int PROXY_PORT = 8899;

    private static ProxyServer proxyServer;
    private static ReceiveTest receiveTest;
    private static ProxySelector defaultProxySelector;

    @BeforeClass
    public static void initialize() throws Exception {
        proxyServer = ProxyServer.create("localhost", PROXY_PORT);
        proxyServer.start(t -> {
        });

        defaultProxySelector = ProxySelector.getDefault();
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                LinkedList<Proxy> proxies = new LinkedList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", PROXY_PORT)));
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

        receiveTest = new ReceiveTest();
        ConnectionStringBuilder connectionString = TestContext.getConnectionString();
        connectionString.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        ReceiveTest.initializeEventHub(connectionString);
    }

    @AfterClass()
    public static void cleanup() throws Exception {
        ReceiveTest.cleanup();

        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Test()
    public void testReceiverStartOfStreamFilters() throws EventHubException {
        receiveTest.testReceiverStartOfStreamFilters();
    }

    @After
    public void testCleanup() throws EventHubException {
        receiveTest.testCleanup();
    }
}
