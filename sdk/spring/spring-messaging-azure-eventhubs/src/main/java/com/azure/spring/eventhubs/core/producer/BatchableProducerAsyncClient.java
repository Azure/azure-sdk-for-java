// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.PartitionSupplier;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Batchable producer async client for sending events in batches.
 */
public class BatchableProducerAsyncClient implements EventHubProducer {

    private EventHubProducerAsyncClient client;
    private final int maxBatchInBytes;
    private final Duration maxWaitTime;
    private AtomicReference<EventDataBatch> currentBatch;
    private final AtomicReference<LocalDateTime> lastSendTime = new AtomicReference<>(LocalDateTime.now());;

    public BatchableProducerAsyncClient(EventHubProducerAsyncClient client, int maxBatchInBytes, Duration maxWaitTime) {
        this.client = client;
        this.maxBatchInBytes = maxBatchInBytes;
        this.maxWaitTime = maxWaitTime;
    }

    @Override
    public Mono<Void> send(Flux<EventData> events) {
        return send(events, null);
    }

    @Override
    public Mono<Void> send(Flux<EventData> events, PartitionSupplier partitionSupplier) {
        CreateBatchOptions options = buildCreateBatchOptions(partitionSupplier, maxBatchInBytes);

        if (currentBatch == null) {
            currentBatch = new AtomicReference<>(client.createBatch(options).block());
        }

        return events.flatMap(event -> {
                         final EventDataBatch batch = currentBatch.get();
                         if (batch.tryAdd(event)) {
                             return Mono.empty();
                         }
                         // The batch is full, so we create a new batch and send the batch. Mono.when completes when
                         // both operations
                         // have completed.
                         lastSendTime.set(LocalDateTime.now());
                         return Mono.when(
                             client.send(batch),
                             client.createBatch(options).map(newBatch -> {
                                 currentBatch.set(newBatch);
                                 // Add that event that we couldn't before.
                                 if (!newBatch.tryAdd(event)) {
                                     throw Exceptions.propagate(new IllegalArgumentException(String.format(
                                         "Event is too large for an empty batch. Max size: %s. Event: %s",
                                         newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                                 }
                                 return newBatch;
                             }));
                     })
                     .then(Mono.just(""))
                     .flatMap(s -> {
                         final EventDataBatch batch = currentBatch.get();
                         if (batch != null && Duration.between(this.lastSendTime.get(), LocalDateTime.now())
                                                      .compareTo(maxWaitTime) > 0) {
                             lastSendTime.set(LocalDateTime.now());
                             return Mono.when(
                                 client.send(batch),
                                 client.createBatch(options).map(newBatch -> {
                                     currentBatch.set(newBatch);
                                     return newBatch;
                                 }));
                         }
                         return Mono.empty();
                     });
    }

    public Mono<Void> send(EventDataBatch batch) {
        return this.client.send(batch);
    }

    @Override
    public void close() {
        this.client.close();
    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier, int maxSizeInBytes) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null)
            .setMaximumSizeInBytes(maxSizeInBytes);
    }

    public Mono<EventHubProperties> getEventHubProperties() {
        return this.client.getEventHubProperties();
    }
}
