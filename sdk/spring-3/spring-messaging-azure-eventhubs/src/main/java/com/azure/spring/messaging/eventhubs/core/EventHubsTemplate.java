// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.core.SendOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.azure.spring.messaging.AzureHeaders.PARTITION_ID;
import static com.azure.spring.messaging.AzureHeaders.PARTITION_KEY;

/**
 * A template for executing sending operations asynchronously to Event Hubs.
 */
public class EventHubsTemplate implements SendOperation {

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

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier asynchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     * @return Mono Void
     */
    public <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages,
                                    PartitionSupplier partitionSupplier) {
        List<EventData> eventData = messages.stream()
                                            .map(m -> messageConverter.fromMessage(m, EventData.class))
                                            .collect(Collectors.toList());
        return doSend(destination, eventData, partitionSupplier);
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination asynchronously.
     * @param destination destination
     * @param messages message set
     * @param <T> payload type in message
     * @return Mono Void
     */
    public <T> Mono<Void> sendAsync(String destination, Collection<Message<T>> messages) {
        return sendAsync(destination, messages, null);
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination with a given partition supplier synchronously.
     * @param destination destination
     * @param messages message set
     * @param partitionSupplier partition supplier
     * @param <T> payload type in message
     */
    public <T> void send(String destination, Collection<Message<T>> messages, PartitionSupplier partitionSupplier) {
        sendAsync(destination, messages, partitionSupplier).block();
    }

    /**
     * Send a {@link Collection}&lt;{@link Message}&gt; to the given destination synchronously.
     * @param destination destination
     * @param messages message set
     * @param <T> payload type in message
     */
    public <T> void send(String destination, Collection<Message<T>> messages) {
        send(destination, messages, null);
    }

    @Override
    public <T> Mono<Void> sendAsync(String destination, Message<T> message) {
        return sendAsync(destination, Collections.singleton(message), buildPartitionSupplier(message));
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

    <T> PartitionSupplier buildPartitionSupplier(Message<T> message) {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        Optional.ofNullable(message.getHeaders().get(PARTITION_KEY)).ifPresent(s -> partitionSupplier.setPartitionKey(String.valueOf(s)));
        Optional.ofNullable(message.getHeaders().get(PARTITION_ID)).ifPresent(s -> partitionSupplier.setPartitionId(String.valueOf(s)));
        return partitionSupplier;
    }

    /**
     * Set the message converter.
     * @param messageConverter the message converter.
     */
    public void setMessageConverter(EventHubsMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

}
