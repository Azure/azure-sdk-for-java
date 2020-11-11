// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * all tests derive from this base - provides common functionality - provides a way to checkout EventHub for each test
 * to exclusively run with
 */
public abstract class TestBase {
    protected final Logger logger;

    protected TestBase() {
        this(LoggerFactory.getLogger(TestBase.class));
    }

    protected TestBase(Logger logger) {
        this.logger = logger;
    }

    public static CompletableFuture<Void> pushEventsToPartition(final EventHubClient ehClient, final String partitionId, final int noOfEvents)
        throws EventHubException {
        return ehClient.createPartitionSender(partitionId)
            .thenCompose(new Function<PartitionSender, CompletableFuture<Void>>() {
                @Override
                public CompletableFuture<Void> apply(PartitionSender pSender) {
                    @SuppressWarnings("unchecked") final CompletableFuture<Void>[] sends = new CompletableFuture[noOfEvents];
                    for (int count = 0; count < noOfEvents; count++) {
                        final EventData sendEvent = EventData.create("test string".getBytes());
                        sends[count] = pSender.send(sendEvent);
                    }

                    return CompletableFuture.allOf(sends);
                }
            });
    }
}
