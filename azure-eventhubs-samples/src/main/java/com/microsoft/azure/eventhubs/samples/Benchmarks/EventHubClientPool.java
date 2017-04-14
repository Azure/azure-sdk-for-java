/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.samples.Benchmarks;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.servicebus.ServiceBusException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


public final class EventHubClientPool {

    private final int poolSize;
    private final String connectionString;
    private final Object previouslySentLock = new Object();
    private final EventHubClient[] clients;

    private int previouslySent = 0;

    EventHubClientPool(final int poolSize, final String connectionString) {
        this.poolSize = poolSize;
        this.connectionString = connectionString;
        this.clients = new EventHubClient[this.poolSize];
    }

    public CompletableFuture<Void> initialize() throws IOException, ServiceBusException {
        final CompletableFuture[] createSenders = new CompletableFuture[this.poolSize];
        for (int count = 0; count < poolSize; count++) {
            final int clientsIndex = count;
            createSenders[count] = EventHubClient.createFromConnectionString(this.connectionString).thenAccept(new Consumer<EventHubClient>() {
                @Override
                public void accept(EventHubClient eventHubClient) {
                    clients[clientsIndex] = eventHubClient;
                }
            });
        }

        return CompletableFuture.allOf(createSenders);
    }

    public CompletableFuture<Void> send(Iterable<EventData> events) {
        final int poolIndex;
        synchronized (this.previouslySentLock) {
            poolIndex = this.previouslySent++ % poolSize;
        }

        return clients[poolIndex].send(events);
    }

    public CompletableFuture<Void> close() {
        final CompletableFuture[] closers = new CompletableFuture[this.poolSize];
        for (int count = 0; count < poolSize; count++) {
            closers[count] = this.clients[count].close();
        }

        return CompletableFuture.allOf(closers);
    }
}
