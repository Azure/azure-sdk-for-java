// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.proxy;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.ProxyConfiguration;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ProxyIntegrationTest extends ApiTestBase {
    private static final String PARTITION_ID = "0";

    private EventHubClient client;
    private PartitionSender sender;

    @Before
    public void setup() throws IOException, EventHubException {
        final ProxyConfiguration proxyConfiguration = getProxyConfiguration();

        Assume.assumeTrue("Cannot run proxy integration tests without setting proxy configuration.",
            proxyConfiguration != null);

        final ConnectionStringBuilder connectionString = TestContext.getConnectionString()
            .setTransportType(TransportType.AMQP_WEB_SOCKETS);

        client = EventHubClient.createFromConnectionStringSync(connectionString.toString(), RetryPolicy.getNoRetry(),
            TestContext.EXECUTOR_SERVICE, proxyConfiguration);

        sender = client.createPartitionSenderSync(PARTITION_ID);
    }

    @After
    public void teardown() throws ExecutionException, InterruptedException {
        CompletableFuture.allOf(sender.close(), client.close()).get();
    }

    /**
     * Verifies we can send events through the proxy.
     */
    @Test
    public void send() throws EventHubException {
        sender.sendSync(EventData.create("Hello".getBytes(UTF_8)));
    }

    /**
     * Verifies we can receive events through the proxy.
     */
    @Test
    public void receive() throws EventHubException, ExecutionException, InterruptedException {
        // Arrange
        final int numberOfEvents = 15;
        final PartitionReceiver receiver = client.createReceiverSync(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
            PARTITION_ID, EventPosition.fromStartOfStream());
        pushEventsToPartition(client, PARTITION_ID, numberOfEvents).get();

        // Act
        final Iterable<EventData> received = receiver.receiveSync(15);

        // Assert
        Assert.assertNotNull(received);

        final ArrayList<EventData> list = new ArrayList<>();
        received.forEach(list::add);

        Assert.assertEquals(numberOfEvents, list.size());
    }
}
