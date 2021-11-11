// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.PartitionSupplier;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class ProducerAsyncClient implements EventHubProducer {

    private EventHubProducerAsyncClient client;

    public ProducerAsyncClient(EventHubProducerAsyncClient client) {
        this.client = client;
    }

    public Mono<Void> send(EventDataBatch batch) {
        return this.client.send(batch);
    }

    @Override
    public Mono<Void> send(Flux<EventData> events) {
        return send(events, null);
    }

    @Override
    public Mono<Void> send(Flux<EventData> events, PartitionSupplier partitionSupplier) {
        return null;
    }

    @Override
    public void close() {
        this.client.close();
    }

}
