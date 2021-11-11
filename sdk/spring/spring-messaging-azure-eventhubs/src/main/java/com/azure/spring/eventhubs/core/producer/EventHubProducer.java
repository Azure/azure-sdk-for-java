// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.spring.messaging.PartitionSupplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Producer async client interface for sending events in batches.
 */
public interface EventHubProducer {


    Mono<Void> send(Flux<EventData> events);

    Mono<Void> send(Flux<EventData> events, PartitionSupplier partitionSupplier);

    Mono<Void> send(EventDataBatch batch);

    void close();

}
