package com.microsoft.azure.servicebus;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.TransportType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProxySelectorTests {


    @Test
    public void proxySelectorConnectFailedInvokeTest() throws Exception
    {
        // set up proxy selector with a bad address in order to check that the connectFailed() method is invoked
        int noProxyPort = 8888;
        final CompletableFuture<Void> connectFailedTask = new CompletableFuture<>();

        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                List<Proxy> proxies = new LinkedList<>();
                proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", noProxyPort)));
                return proxies;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                connectFailedTask.complete(null);
            }
        });

        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(TestUtils.getNamespaceConnectionString());
        connectionStringBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS);
        connectionStringBuilder.setOperationTimeout(Duration.ofSeconds(10));

        try {
            QueueClient sendClient = new QueueClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
        } catch (ServiceBusException ex) {
            Assert.assertEquals(
               "Error{condition=amqp:connection:framing-error, description='connection aborted', info=null}",
                ex.getLocalizedMessage());
        }

        connectFailedTask.get(2, TimeUnit.SECONDS);
    }
}
