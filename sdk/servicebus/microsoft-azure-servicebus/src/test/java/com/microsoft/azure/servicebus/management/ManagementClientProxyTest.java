package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.TestUtils;
import com.microsoft.azure.servicebus.management.ManagementClient;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.TransportType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagementClientProxyTest {
    @Ignore
    @Test
    public void managementClientWithProxy() throws Exception {
        String proxyHostName = "127.0.0.1";
        int proxyPort = 8888;
        final ProxySelector systemDefaultSelector = ProxySelector.getDefault();

        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (uri != null
                    && uri.getHost() != null
                ) {
                    List<Proxy> proxies = new LinkedList<>();
                    proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostName, proxyPort)));
                    return proxies;
                }
                return systemDefaultSelector.select(uri);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe){
                if (uri == null || sa == null || ioe == null) {
                    throw new IllegalArgumentException("Arguments can't be null.");
                }
                systemDefaultSelector.connectFailed(uri, sa, ioe);
            }
        });

        URI namespaceEndpointURI = TestUtils.getNamespaceEndpointURI();
        ClientSettings managementClientSettings = TestUtils.getManagementClientSettings();

        ManagementClient managementClient = new ManagementClient(namespaceEndpointURI, managementClientSettings);
        String queueName = "test" + UUID.randomUUID().toString().substring(0, 8);
        QueueDescription q = new QueueDescription(queueName);
        QueueDescription qCreated = managementClient.createQueue(q);
        Assert.assertEquals(q, qCreated);

        // send message
        String connectionString = TestUtils.getNamespaceConnectionString();
        ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(connectionString, queueName);
        connStrBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS);

        QueueClient sendClient = new QueueClient(connStrBuilder, ReceiveMode.PEEKLOCK);
        Message message = new Message("hello");
        sendClient.sendAsync(message).thenRunAsync(() -> sendClient.closeAsync());
        waitForEnter(10);
    }

    private void waitForEnter(int seconds) {
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            executor.invokeAny(Arrays.asList(() -> {
                System.in.read();
                return 0;
            }, () -> {
                Thread.sleep(seconds * 1000);
                return 0;
            }));
        } catch (Exception e) {
            // absorb
        }
    }
}
