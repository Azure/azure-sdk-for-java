// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.BatchSendOperation;
import com.azure.spring.messaging.core.SendOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A template for executing sending operations asynchronously to Event Hubs.
 */
public class EventHubsTemplate implements SendOperation, BatchSendOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsTemplate.class);

    private final EventHubsProducerFactory producerFactory;
    private EventHubsMessageConverter messageConverter = new EventHubsMessageConverter();

    /**
     * Create an instance using the supplied producer factory.
     * @param producerFactory the producer factory.
     */
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
            try {
                if (batch.tryAdd(event)) {
                    return Mono.empty();
                } else {
                    LOGGER.warn("EventDataBatch is full in the collect process or the first event is "
                        + "too large to fit in an empty batch! Max size: {}", batch.getMaxSizeInBytes());
                }
            } catch (AmqpException e) {
                LOGGER.error("Event is larger than maximum allowed size.", e);
                return Mono.empty();
            }

            return Mono.when(
                producer.send(batch),
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);
                    // Add the event that did not fit in the previous batch.
                    try {
                        if (!newBatch.tryAdd(event)) {
                            LOGGER.error(
                                "Event was too large to fit in an empty batch. Max size:{} ",
                                newBatch.getMaxSizeInBytes());
                        }
                    } catch (AmqpException e) {
                        LOGGER.error("Event was too large to fit in an empty batch. Max size:{}",
                            newBatch.getMaxSizeInBytes(), e);
                    }

                    return newBatch;
                }));
        })
        .then()
        .block();

        final EventDataBatch batch = currentBatch.getAndSet(null);
        return producer.send(batch);
    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null);
    }

    /**
     * Set the message converter.
     * @param messageConverter the message converter.
     */
    public void setMessageConverter(EventHubsMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

}
