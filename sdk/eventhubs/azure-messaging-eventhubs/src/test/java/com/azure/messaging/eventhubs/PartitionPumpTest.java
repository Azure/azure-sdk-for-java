// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link PartitionPump}.
 */
public class PartitionPumpTest {
    /**
     * Verifies that we dispose of the client and scheduler when disposed.
     */
    @Test
    public void disposesResources() {
        // Arrange
        AtomicInteger closeCount = new AtomicInteger();
        EventHubConsumerAsyncClient consumerAsyncClient
            = new EventHubConsumerAsyncClient(null, null, null, null, null, 0, false, null, null, null) {
                @Override
                public void close() {
                    closeCount.incrementAndGet();
                }
            };

        AtomicInteger disposeGracefullyCount = new AtomicInteger();
        Scheduler scheduler = new Scheduler() {
            @Override
            public Disposable schedule(Runnable runnable) {
                return null;
            }

            @Override
            public Worker createWorker() {
                return null;
            }

            @Override
            public Mono<Void> disposeGracefully() {
                disposeGracefullyCount.incrementAndGet();
                return Mono.empty();
            }
        };

        final String partitionId = "1";
        final PartitionPump partitionPump = new PartitionPump(partitionId, consumerAsyncClient, scheduler);

        // Act
        partitionPump.close();

        // Assert
        assertEquals(1, disposeGracefullyCount.get());
        assertEquals(1, closeCount.get());
    }

    /**
     * Tests getter.
     */
    @Test
    public void getClient() {
        // Arrange
        EventHubConsumerAsyncClient consumerAsyncClient
            = new EventHubConsumerAsyncClient(null, null, null, null, null, 0, false, null, null, null);
        final PartitionPump partitionPump = new PartitionPump("1", consumerAsyncClient, Schedulers.boundedElastic());

        // Act and Assert
        assertEquals(consumerAsyncClient, partitionPump.getClient());
    }
}
