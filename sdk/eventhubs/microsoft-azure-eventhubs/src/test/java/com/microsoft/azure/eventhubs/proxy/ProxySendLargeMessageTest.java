// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.exceptioncontracts.SendLargeMessageTest;
import com.microsoft.azure.eventhubs.jproxy.ProxyServer;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.AfterClass;
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
import java.util.concurrent.ExecutionException;

public class ProxySendLargeMessageTest extends ApiTestBase {
    private static int proxyPort = 8899;
    private static ProxyServer proxyServer;
    private static SendLargeMessageTest sendLargeMessageTest;
    private static ProxySelector defaultProxySelector;

    @BeforeClass
    public static void initialize() throws Exception {
        proxyServer = ProxyServer.create("localhost", proxyPort);
        proxyServer.start(t -> {
        });

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

        final ConnectionStringBuilder connectionStringBuilder = TestContext.getConnectionString();
        connectionStringBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        sendLargeMessageTest = new SendLargeMessageTest();
        SendLargeMessageTest.initializeEventHubClients(connectionStringBuilder);
    }

    @AfterClass()
    public static void cleanup() throws Exception {
        SendLargeMessageTest.cleanup();

        if (proxyServer != null) {
            proxyServer.stop();
        }

        ProxySelector.setDefault(defaultProxySelector);
    }

    @Test()
    public void sendMsgLargerThan64k() throws EventHubException, InterruptedException, ExecutionException, IOException {
        sendLargeMessageTest.sendMsgLargerThan64k();
    }

    @Test(expected = PayloadSizeExceededException.class)
    public void sendMsgLargerThan256K() throws EventHubException, InterruptedException, ExecutionException, IOException {
        sendLargeMessageTest.sendMsgLargerThan1024K();
    }

    @Test()
    public void sendMsgLargerThan128k() throws EventHubException, InterruptedException, ExecutionException, IOException {
        sendLargeMessageTest.sendMsgLargerThan128k();
    }
}
