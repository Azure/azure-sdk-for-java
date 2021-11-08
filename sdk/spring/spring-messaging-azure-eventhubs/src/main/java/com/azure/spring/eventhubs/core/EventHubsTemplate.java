// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
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
import java.util.ArrayList;
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
                                    PartitionSupplier partitionSupplier, int maximumSizeInBytes, Duration maxWaitTime) {
        List<EventData> eventData = messages.stream()
            .map(m -> messageConverter.fromMessage(m, EventData.class))
            .collect(Collectors.toList());
        return doSendBatch(destination, eventData, partitionSupplier, maximumSizeInBytes, maxWaitTime);
    }

    @Override
    public <T> Mono<Void> sendAsync(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        List<EventData> eventData = new ArrayList<>();
        eventData.add(messageConverter.fromMessage(message, EventData.class));
        return doSend(destination, eventData, partitionSupplier);
    }

    private Mono<Void> doSendBatch(String destination, List<EventData> events, PartitionSupplier partitionSupplier,
                                   int maximumSizeInBytes, Duration maxWaitTime) {
        EventHubProducerAsyncClient producer = producerFactory.createProducer(destination);
        CreateBatchOptions options = buildCreateBatchOptions(partitionSupplier, maximumSizeInBytes);
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
            return Mono.when(
                producer.send(batch).then().doFinally(signal -> lastSendTime.set(LocalDateTime.now())),
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
                    currentBatch.set(producer.createBatch(options).block());
                }
            });
    }

    private Mono<Void> doSend(String destination, List<EventData> events, PartitionSupplier partitionSupplier) {
        EventHubProducerAsyncClient producer = producerFactory.createProducer(destination);
        SendOptions options = buildSendOptions(partitionSupplier);
        return producer.send(events, options);

    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier, int maximumSizeInBytes) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null)
            .setMaximumSizeInBytes(maximumSizeInBytes);
    }

    private SendOptions buildSendOptions(PartitionSupplier partitionSupplier) {
        return new SendOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null);
    }

    public void setMessageConverter(EventHubMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
