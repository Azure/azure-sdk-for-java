// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.eventhubs.core.producer.EventHubProducerFactory;
import com.azure.spring.eventhubs.support.converter.EventHubMessageConverter;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.BatchSendOperation;
import com.azure.spring.messaging.core.SendOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
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

    private final EventHubProducerFactory producerFactory;
    private EventHubMessageConverter messageConverter = new EventHubMessageConverter();
    private AtomicReference<EventDataBatch> currentBatch;
    private final AtomicReference<LocalDateTime> lastSendTime = new AtomicReference<>(LocalDateTime.now());

    public EventHubsTemplate(EventHubProducerFactory producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Override
    public <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                                    PartitionSupplier partitionSupplier, int maxSizeInBytes, Duration maxWaitTime) {
        return doSend(destination, covertMessagesToList(messages), partitionSupplier, maxSizeInBytes, maxWaitTime);
    }

    @Override
    public <T> Mono<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        return sendAsync(destination, Collections.singleton(message), partitionSupplier, 0);
    }

    private Mono<Void> doSend(String destination, List<EventData> events, PartitionSupplier partitionSupplier,
                              int maxSizeInBytes, Duration maxWaitTime) {
        EventHubProducerAsyncClient producer = producerFactory.createProducer(destination);
        CreateBatchOptions options = buildCreateBatchOptions(partitionSupplier, maxSizeInBytes);
        Flux<EventData> eventDataFlux = Flux.fromIterable(events);

        if (currentBatch == null) {
            currentBatch = new AtomicReference<>(producer.createBatch(options).block());
        }

        return eventDataFlux.flatMap(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (batch.tryAdd(event)) {
                return Mono.empty();
            }

            // The batch is full, so we create a new batch and send the batch. Mono.when completes when both operations
            // have completed.
            lastSendTime.set(LocalDateTime.now());
            return Mono.when(
                producer.send(batch),
                producer.createBatch(options).map(newBatch -> {
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
            .then()
            .doFinally(signal -> {
                final EventDataBatch batch = currentBatch.get();
                if (batch != null && Duration.between(this.lastSendTime.get(), LocalDateTime.now())
                    .compareTo(maxWaitTime) > 0) {
                    producer.send(batch);
                    lastSendTime.set(LocalDateTime.now());
                    currentBatch.set(producer.createBatch(options).block());
                }
            });
    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier, int maxSizeInBytes) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null)
            .setMaximumSizeInBytes(maxSizeInBytes);
    }

    private <T> List<EventData> covertMessagesToList(Collection<Message<T>> messages) {
        return messages.stream()
                       .map(m -> messageConverter.fromMessage(m, EventData.class))
                       .collect(Collectors.toList());
    }

    public void setMessageConverter(EventHubMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
