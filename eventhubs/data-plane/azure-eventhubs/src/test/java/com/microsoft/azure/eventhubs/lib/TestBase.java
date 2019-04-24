// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * all tests derive from this base - provides common functionality
 * - provides a way to checkout EventHub for each test to exclusively run with
 * - ******* Before running all Tests - fill data here *********
 */
public abstract class TestBase {
    public static final Logger TEST_LOGGER = Logger.getLogger("servicebus.test.trace");

    public static CompletableFuture<Void> pushEventsToPartition(final EventHubClient ehClient, final String partitionId, final int noOfEvents)
            throws EventHubException {
        return ehClient.createPartitionSender(partitionId)
                .thenComposeAsync(new Function<PartitionSender, CompletableFuture<Void>>() {
                    @Override
                    public CompletableFuture<Void> apply(PartitionSender pSender) {
                        @SuppressWarnings("unchecked")
                        final CompletableFuture<Void>[] sends = new CompletableFuture[noOfEvents];
                        for (int count = 0; count < noOfEvents; count++) {
                            final EventData sendEvent = EventData.create("test string".getBytes());
                            sends[count] = pSender.send(sendEvent);
                        }

                        return CompletableFuture.allOf(sends);
                    }
                });
    }
}
