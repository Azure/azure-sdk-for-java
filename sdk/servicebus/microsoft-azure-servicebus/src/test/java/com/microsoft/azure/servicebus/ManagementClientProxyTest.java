package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.management.ManagementClient;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ManagementClientProxyTest {
    @Test
    public void managementClientWithProxy() throws InterruptedException, ServiceBusException {
        String proxyHostName = "127.0.0.1";
        int proxyPort = 8888;
        final ProxySelector systemDefaultSelector = ProxySelector.getDefault();

        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (uri != null
                    && uri.getHost() != null
//                    && uri.getHost().equalsIgnoreCase(proxyHostName)
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
        String queueName = "proxy" + UUID.randomUUID().toString().substring(0, 8);
        QueueDescription q = new QueueDescription(queueName);
        QueueDescription qCreated = managementClient.createQueue(q);
        Assert.assertEquals(q, qCreated);
    }
}
