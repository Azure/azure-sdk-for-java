// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.PartitionSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 */
public class ProducerAsyncClient implements EventHubProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerAsyncClient.class);
    private final EventHubProducerAsyncClient client;

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
        CreateBatchOptions options = buildCreateBatchOptions(partitionSupplier);

        return client.createBatch(options).flatMap(batch -> {
            for (EventData event : events.collectList().block()) {
                try {
                    batch.tryAdd(event);
                } catch (AmqpException e) {
                    LOGGER.error("Event is larger than maximum allowed size. Exception: " + e);
                }
            }
            return client.send(batch);
        });
    }

    @Override
    public void close() {
        this.client.close();
    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null);
    }
}
