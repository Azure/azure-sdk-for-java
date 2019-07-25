// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.connstrbuilder;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.impl.ConnectionHandler;
import com.microsoft.azure.eventhubs.impl.EventHubClientImpl;
import com.microsoft.azure.eventhubs.impl.MessagingFactory;
import com.microsoft.azure.eventhubs.jproxy.ProxyServer;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class TransportTypeTest extends ApiTestBase {
    private volatile boolean isProxySelectorInvoked = false;

    @Test
    public void transportTypeAmqpCreatesConnectionWithPort5671() throws Exception {
        ConnectionStringBuilder builder = new ConnectionStringBuilder(TestContext.getConnectionString().toString());
        builder.setTransportType(TransportType.AMQP);

        EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(builder.toString(), TestContext.EXECUTOR_SERVICE);
        try {
            EventHubClientImpl eventHubClientImpl = (EventHubClientImpl) ehClient;
            final Field factoryField = EventHubClientImpl.class.getDeclaredField("underlyingFactory");
            factoryField.setAccessible(true);
            final MessagingFactory underlyingFactory = (MessagingFactory) factoryField.get(eventHubClientImpl);

            final Field connectionHandlerField = MessagingFactory.class.getDeclaredField("connectionHandler");
            connectionHandlerField.setAccessible(true);
            final ConnectionHandler connectionHandler = (ConnectionHandler) connectionHandlerField.get(underlyingFactory);

            final Method outboundSocketPort = ConnectionHandler.class.getDeclaredMethod("getRemotePort");
            outboundSocketPort.setAccessible(true);

            final Method protocolPort = ConnectionHandler.class.getDeclaredMethod("getProtocolPort");
            protocolPort.setAccessible(true);

            Assert.assertEquals(5671, outboundSocketPort.invoke(connectionHandler));
            Assert.assertEquals(5671, protocolPort.invoke(connectionHandler));
        } finally {
            ehClient.closeSync();
        }
    }

    @Test
    public void transportTypeAmqpWebSocketsCreatesConnectionWithPort443() throws Exception {
        ConnectionStringBuilder builder = new ConnectionStringBuilder(TestContext.getConnectionString().toString());
        builder.setTransportType(TransportType.AMQP_WEB_SOCKETS);

        EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(builder.toString(), TestContext.EXECUTOR_SERVICE);
        try {
            EventHubClientImpl eventHubClientImpl = (EventHubClientImpl) ehClient;
            final Field factoryField = EventHubClientImpl.class.getDeclaredField("underlyingFactory");
            factoryField.setAccessible(true);
            final MessagingFactory underlyingFactory = (MessagingFactory) factoryField.get(eventHubClientImpl);

            final Field connectionHandlerField = MessagingFactory.class.getDeclaredField("connectionHandler");
            connectionHandlerField.setAccessible(true);
            final ConnectionHandler connectionHandler = (ConnectionHandler) connectionHandlerField.get(underlyingFactory);

            final Method outboundSocketPort = ConnectionHandler.class.getDeclaredMethod("getRemotePort");
            outboundSocketPort.setAccessible(true);

            final Method protocolPort = ConnectionHandler.class.getDeclaredMethod("getProtocolPort");
            protocolPort.setAccessible(true);

            Assert.assertEquals(443, outboundSocketPort.invoke(connectionHandler));
            Assert.assertEquals(443, protocolPort.invoke(connectionHandler));
        } finally {
            ehClient.closeSync();
        }
    }

    @Test
    public void transportTypeAmqpWebSocketsWithProxyCreatesConnectionWithCorrectPorts() throws Exception {
        int proxyPort = 8899;
        ProxyServer proxyServer = ProxyServer.create("localhost", proxyPort);
        proxyServer.start(throwable -> {
        });

        ProxySelector defaultProxySelector = ProxySelector.getDefault();
        this.isProxySelectorInvoked = false;
        try {
            ProxySelector.setDefault(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    LinkedList<Proxy> proxies = new LinkedList<>();
                    proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", proxyPort)));
                    isProxySelectorInvoked = true;
                    return proxies;
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                    // no-op
                }
            });

            ConnectionStringBuilder builder = new ConnectionStringBuilder(TestContext.getConnectionString().toString());
            builder.setTransportType(TransportType.AMQP_WEB_SOCKETS);

            EventHubClient ehClient = EventHubClient.createFromConnectionStringSync(builder.toString(), TestContext.EXECUTOR_SERVICE);
            try {
                EventHubClientImpl eventHubClientImpl = (EventHubClientImpl) ehClient;
                final Field factoryField = EventHubClientImpl.class.getDeclaredField("underlyingFactory");
                factoryField.setAccessible(true);
                final MessagingFactory underlyingFactory = (MessagingFactory) factoryField.get(eventHubClientImpl);

                final Field connectionHandlerField = MessagingFactory.class.getDeclaredField("connectionHandler");
                connectionHandlerField.setAccessible(true);
                final ConnectionHandler connectionHandler = (ConnectionHandler) connectionHandlerField.get(underlyingFactory);

                final Method outboundSocketPort = ConnectionHandler.class.getDeclaredMethod("getRemotePort");
                outboundSocketPort.setAccessible(true);

                final Method protocolPort = ConnectionHandler.class.getDeclaredMethod("getProtocolPort");
                protocolPort.setAccessible(true);

                Assert.assertEquals(proxyPort, outboundSocketPort.invoke(connectionHandler));
                Assert.assertEquals(443, protocolPort.invoke(connectionHandler));

                Assert.assertTrue(isProxySelectorInvoked);
            } finally {
                ehClient.closeSync();
                ProxySelector.setDefault(defaultProxySelector);
            }
        } finally {
            proxyServer.stop();
        }
    }
}
