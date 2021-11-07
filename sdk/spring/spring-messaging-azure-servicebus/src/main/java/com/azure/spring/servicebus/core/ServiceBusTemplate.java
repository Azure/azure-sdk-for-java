// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.QUEUE;
import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Azure Service Bus template to support send {@link Message} asynchronously.
 */
public class ServiceBusTemplate implements SendOperation {

    private static final ServiceBusMessageConverter DEFAULT_CONVERTER = new ServiceBusMessageConverter();
    private final ServiceBusProducerFactory producerFactory;
    private ServiceBusMessageConverter messageConverter = DEFAULT_CONVERTER;
    private ServiceBusEntityType defaultEntityType;

    public ServiceBusTemplate(@NonNull ServiceBusProducerFactory producerFactory) {
        this(producerFactory, null);
    }

    public ServiceBusTemplate(@NonNull ServiceBusProducerFactory producerFactory,
                              ServiceBusEntityType defaultEntityType) {
        this.producerFactory = producerFactory;
        this.defaultEntityType = defaultEntityType;
    }

    @Override
    public <U> Mono<Void> sendAsync(String destination,
                                    Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        return doSend(destination, this.defaultEntityType, message, partitionSupplier);

    }

    public <U> Mono<Void> sendAsync(String destination,
                                    ServiceBusEntityType entityType,
                                    Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        Assert.notNull(entityType, "Entity type cannot be null.");
        if (QUEUE.equals(entityType)) {
            return doSend(destination, QUEUE, message, partitionSupplier);
        } else {
            return doSend(destination, TOPIC, message, partitionSupplier);
        }
    }

    public <U> Mono<Void> sendToQueueAsync(String destination,
                                           Message<U> message,
                                           PartitionSupplier partitionSupplier) {
        return doSend(destination, QUEUE, message, partitionSupplier);
    }

    public <U> Mono<Void> sendToTopicAsync(String destination,
                                           Message<U> message,
                                           PartitionSupplier partitionSupplier) {
        return doSend(destination, TOPIC, message, partitionSupplier);
    }

    private <U> Mono<Void> doSend(String destination,
                                  @Nullable ServiceBusEntityType entityType,
                                  Message<U> message,
                                  PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusSenderAsyncClient senderAsyncClient;
        if (entityType == null) {
            senderAsyncClient = this.producerFactory.createProducer(destination);
        } else {
            senderAsyncClient = this.producerFactory.createProducer(destination, entityType);
        }
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);

        if (Objects.nonNull(serviceBusMessage) && !StringUtils.hasText(serviceBusMessage.getPartitionKey())) {
            String partitionKey = getPartitionKey(partitionSupplier);
            serviceBusMessage.setPartitionKey(partitionKey);
        }
        return senderAsyncClient.sendMessage(serviceBusMessage);
    }

    public void setMessageConverter(ServiceBusMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public ServiceBusMessageConverter getMessageConverter() {
        return messageConverter;
    }

    private String getPartitionKey(PartitionSupplier partitionSupplier) {
        if (partitionSupplier == null) {
            return "";
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionKey())) {
            return partitionSupplier.getPartitionKey();
        }

        if (StringUtils.hasText(partitionSupplier.getPartitionId())) {
            return partitionSupplier.getPartitionId();
        }

        return "";
    }

    public ServiceBusEntityType getDefaultEntityType() {
        return defaultEntityType;
    }

    public void setDefaultEntityType(ServiceBusEntityType defaultEntityType) {
        this.defaultEntityType = defaultEntityType;
    }
}
