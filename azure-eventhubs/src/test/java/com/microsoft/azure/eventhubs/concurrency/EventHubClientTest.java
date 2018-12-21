/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
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
        CompletableFuture<EventHubClient>[] createFutures = new CompletableFuture[noOfClients];
        try {
            ConnectionStringBuilder connectionString = TestContext.getConnectionString();
            for (int i = 0; i < noOfClients; i++) {
                createFutures[i] = EventHubClient.create(connectionString.toString(), executorService);
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
            if (createFutures != null) {
                for (CompletableFuture<EventHubClient> createFuture : createFutures) {
                    if (!createFuture.isCancelled() || !createFuture.isCompletedExceptionally()) {
                        EventHubClient ehClient = createFuture.join();
                        ehClient.closeSync();
                    }
                }
            }

            executorService.shutdown();
        }
    }

}
