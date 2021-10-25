// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;

@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class)
public final class EventHubBufferedProducerAsyncClient implements Closeable {
    private final EventHubProducerAsyncClient client;

    EventHubBufferedProducerAsyncClient(EventHubProducerAsyncClient client) {
        this.client = client;
    }

    public String getEventHubName() {
        return client.getEventHubName();
    }

    public Mono<EventHubProperties> getEventHubProperties() {
         return client.getEventHubProperties();
    }

    public Flux<String> getPartitionIds() {
        return client.getPartitionIds();
    }

    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        return client.getPartitionProperties(partitionId);
    }

    public int getBufferedEventCount() {
        return 0;
    }

    public int getBufferedEventCount(String partitionId) {
        return 0;
    }

    public Mono<Void> enqueueEvent(EventData eventData) {
        return null;
    }

    public Mono<Void> enqueueEvent(EventData eventData, SendOptions options) {
        return null;
    }

    public Mono<Void> enqueueEvents(Iterable<EventData> events) {
        return null;
    }

    public Mono<Void> enqueueEvents(Iterable<EventData> events, SendOptions options) {
        return null;
    }

    public Mono<Void> flush() {
        return null;
    }

    @Override
    public void close() {
        client.close();
    }
}
