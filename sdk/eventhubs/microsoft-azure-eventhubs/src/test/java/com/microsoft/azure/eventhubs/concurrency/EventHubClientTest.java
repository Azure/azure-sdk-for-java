// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.concurrency;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventPosition;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.eventhubs.lib.ApiTestBase;
import com.microsoft.azure.eventhubs.lib.TestBase;
import com.microsoft.azure.eventhubs.lib.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class EventHubClientTest extends ApiTestBase {

    @Test()
    public void testParallelEventHubClients() throws Exception {
        final String consumerGroupName = TestContext.getConsumerGroupName();
        final String partitionId = "0";
        final int noOfClients = 4;
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        @SuppressWarnings("unchecked")
        final CompletableFuture<EventHubClient>[] createFutures = new CompletableFuture[noOfClients];

        try {
            ConnectionStringBuilder connectionString = TestContext.getConnectionString();
            for (int i = 0; i < noOfClients; i++) {
                createFutures[i] = EventHubClient.createFromConnectionString(connectionString.toString(), executorService);
            }

            CompletableFuture.allOf(createFutures).get();
            boolean firstOne = true;
            for (CompletableFuture<EventHubClient> createFuture : createFutures) {
                final EventHubClient ehClient = createFuture.join();
                if (firstOne) {
                    TestBase.pushEventsToPartition(ehClient, partitionId, 10).get();
                    firstOne = false;
                }

                PartitionReceiver receiver = ehClient.createReceiverSync(consumerGroupName, partitionId, EventPosition.fromStartOfStream());
                try {
                    Assert.assertTrue(receiver.receiveSync(100).iterator().hasNext());
                } finally {
                    receiver.closeSync();
                }
            }
        } finally {
            for (CompletableFuture<EventHubClient> createFuture : createFutures) {
                // There's a possibility that an exception was thrown while creating EventHubClients, so we can't assume
                // that they were all initialised yet.
                if (createFuture == null) {
                    continue;
                }

                if (!createFuture.isCancelled() || !createFuture.isCompletedExceptionally()) {
                    EventHubClient ehClient = createFuture.join();
                    ehClient.closeSync();
                }
            }

            executorService.shutdown();
        }
    }

}
