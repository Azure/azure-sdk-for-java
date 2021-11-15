// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
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
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class EventHubsTemplate implements SendOperation, BatchSendOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsTemplate.class);

    private final EventHubProducerFactory producerFactory;
    private EventHubMessageConverter messageConverter = new EventHubMessageConverter();

    public EventHubsTemplate(EventHubProducerFactory producerFactory) {
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

        return producer.createBatch(options).flatMap(batch -> {
            for (EventData event : events) {
                try {
                    batch.tryAdd(event);
                } catch (AmqpException e) {
                    LOGGER.error("Event is larger than maximum allowed size. Exception: " + e);
                }
            }
            return producer.send(batch);
        });
    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null);
    }

    public void setMessageConverter(EventHubMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
