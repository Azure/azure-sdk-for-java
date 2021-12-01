// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.eventhubs.core.producer.EventHubsProducerFactory;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.BatchSendOperation;
import com.azure.spring.messaging.core.SendOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 *
 */
public class EventHubsTemplate implements SendOperation, BatchSendOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsTemplate.class);

    private final EventHubsProducerFactory producerFactory;
    private EventHubsMessageConverter messageConverter = new EventHubsMessageConverter();

    public EventHubsTemplate(EventHubsProducerFactory producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Override
    public <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                                    PartitionSupplier partitionSupplier) {
        List<EventData> eventData = messages.stream()
            .map(m -> messageConverter.fromMessage(m, EventData.class))
            .collect(Collectors.toList());
        return doSend(destination, eventData, partitionSupplier);
    }

    @Override
    public <T> Mono<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        return sendAsync(destination, Collections.singleton(message), partitionSupplier);
    }

    private Mono<Void> doSend(String destination, List<EventData> events, PartitionSupplier partitionSupplier) {
        EventHubProducerAsyncClient producer = producerFactory.createProducer(destination);
        CreateBatchOptions options = buildCreateBatchOptions(partitionSupplier);

        AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
                producer.createBatch(options).block());

        Flux.fromIterable(events).flatMap(event -> {
                    final EventDataBatch batch = currentBatch.get();
                    if (batch.tryAdd(event)) {
                        return Mono.empty();
                    }

                    return Mono.when(
                            producer.send(batch),
                            producer.createBatch(options).map(newBatch -> {
                                currentBatch.set(newBatch);
                                // Add the event that did not fit in the previous batch.
                                if (!newBatch.tryAdd(event)) {
                                    throw Exceptions.propagate(new IllegalArgumentException(
                                            "Event was too large to fit in an empty batch. Max size: " + newBatch.getMaxSizeInBytes()));
                                }

                                return newBatch;
                            }));
                }).then()
                .doFinally(signal -> {
                    final EventDataBatch batch = currentBatch.getAndSet(null);
                    if (batch != null && batch.getCount() > 0) {
                        producer.send(batch).block();
                    }
                }).subscribe();

        return Mono.empty();
    }

    CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null);
    }

    public void setMessageConverter(EventHubsMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

}
