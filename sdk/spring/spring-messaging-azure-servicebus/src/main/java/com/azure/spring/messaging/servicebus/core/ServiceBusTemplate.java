// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.stream.StreamSupport;

/**
 * Azure Service Bus template to support send {@link Message} asynchronously.
 *
 * <p>
 * A {@link #defaultEntityType} is required when no entity type is specified in {@link ServiceBusProducerFactory}
 * via related {@link NamespaceProperties} or producer {@link PropertiesSupplier}.
 * </p>
 */
public class ServiceBusTemplate implements SendOperation {

    private static final ServiceBusMessageConverter DEFAULT_CONVERTER = new ServiceBusMessageConverter();
    private final ServiceBusProducerFactory producerFactory;
    private AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter = DEFAULT_CONVERTER;
    private ServiceBusEntityType defaultEntityType;

    /**
     * Create an instance using the supplied producer factory.
     * @param producerFactory the producer factory.
     */
    public ServiceBusTemplate(@NonNull ServiceBusProducerFactory producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Override
    public <U> Mono<Void> sendAsync(String destination, Message<U> message) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusSenderAsyncClient senderAsyncClient =
            this.producerFactory.createProducer(destination, defaultEntityType);
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);
        return senderAsyncClient.sendMessage(serviceBusMessage);
    }

    /**
     * Sends a scheduled message to the specific Azure Service Bus entity. The scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param <U> The type of the message payload.
     * @param destination topic or queue name.
     * @param entityType type of Service Bus entity.
     * @param message Message to be sent to the Service Bus entity.
     * @param scheduledEnqueueTime OffsetDateTime at which the message should appear in the Service Bus queue or topic.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws NullPointerException if {@code message} or {@code scheduledEnqueueTime} is {@code null}.
     * @throws ServiceBusException If the message could not be scheduled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public <U> Mono<Long> scheduleMessage(String destination,
                                          ServiceBusEntityType entityType,
                                          Message<U> message,
                                          OffsetDateTime scheduledEnqueueTime) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusEntityType currentEntityType = entityType;
        if (entityType == null && defaultEntityType != null) {
            currentEntityType = defaultEntityType;
        }
        ServiceBusSenderAsyncClient senderAsyncClient = this.producerFactory.createProducer(destination, currentEntityType);
        ServiceBusMessage serviceBusMessage = messageConverter.fromMessage(message, ServiceBusMessage.class);
        return senderAsyncClient.scheduleMessage(serviceBusMessage, scheduledEnqueueTime);
    }

    /**
     * Sends a batch of scheduled messages to the specific Azure Service Bus entity. The scheduled
     * messages are enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param <U> The type of the message payload.
     * @param destination topic or queue name.
     * @param entityType entity type of Service Bus entity.
     * @param messages messages to be sent to the Service Bus entity.
     * @param scheduledEnqueueTime OffsetDateTime at which the messages should appear in the Service Bus queue or topic.
     *
     * @return Sequence numbers of the scheduled messages which can be used to cancel the scheduling of the messages.
     *
     * @throws NullPointerException If {@code messages} or {@code scheduledEnqueueTime} is {@code null}.
     * @throws ServiceBusException If the messages could not be scheduled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public <U> Flux<Long> scheduleMessages(String destination,
                                           ServiceBusEntityType entityType,
                                           Iterable<Message<U>> messages,
                                           OffsetDateTime scheduledEnqueueTime) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusEntityType currentEntityType = entityType;
        if (entityType == null && defaultEntityType != null) {
            currentEntityType = defaultEntityType;
        }
        ServiceBusSenderAsyncClient senderAsyncClient = this.producerFactory.createProducer(destination, currentEntityType);
        Iterable<ServiceBusMessage> serviceBusMessages = StreamSupport.stream(messages.spliterator(), false)
                                                                      .map(message -> messageConverter.fromMessage(message, ServiceBusMessage.class))
                                                                      .toList();
        return senderAsyncClient.scheduleMessages(serviceBusMessages, scheduledEnqueueTime);
    }

    /**
     * Cancels the enqueuing of a scheduled message of the specific Service Bus entity, if it was not already enqueued.
     *
     * @param destination topic or queue name.
     * @param entityType entity type of Service Bus entity.
     * @param sequenceNumber sequence number of the scheduled message to cancel.
     *
     * @return The {@link Mono} that finishes this operation on Service Bus resource.
     *
     * @throws IllegalArgumentException if {@code sequenceNumber} is negative.
     * @throws ServiceBusException If the messages could not be cancelled.
     * @throws IllegalStateException if sender is already disposed.
     */
    public Mono<Void> cancelScheduledMessage(String destination,
                                                 ServiceBusEntityType entityType,
                                                 long sequenceNumber) {
        ServiceBusEntityType currentEntityType = entityType;
        if (entityType == null && defaultEntityType != null) {
            currentEntityType = defaultEntityType;
        }
        ServiceBusSenderAsyncClient senderAsyncClient = this.producerFactory.createProducer(destination, currentEntityType);
        return senderAsyncClient.cancelScheduledMessage(sequenceNumber);
    }

    /**
     * Cancels the enqueuing of the scheduled message of the specific Service Bus entity, if they were not already enqueued.
     *
     * @param destination topic or queue name.
     * @param entityType entity type of Service Bus entity.
     * @param sequenceNumbers sequence numbers of the scheduled messages to cancel.
     *
     * @return The {@link Mono} that finishes this operation on Service Bus resource.
     *
     * @throws NullPointerException if {@code sequenceNumbers} is null.
     * @throws IllegalStateException if sender is already disposed.
     * @throws ServiceBusException if the scheduled messages cannot cancelled.
     */
    public Mono<Void> cancelScheduledMessages(String destination,
                                              ServiceBusEntityType entityType,
                                              Iterable<Long> sequenceNumbers) {
        ServiceBusEntityType currentEntityType = entityType;
        if (entityType == null && defaultEntityType != null) {
            currentEntityType = defaultEntityType;
        }
        ServiceBusSenderAsyncClient senderAsyncClient = this.producerFactory.createProducer(destination, currentEntityType);
        return senderAsyncClient.cancelScheduledMessages(sequenceNumbers);
    }

    /**
     * Set the message converter to use.
     * @param messageConverter the message converter.
     */
    public void setMessageConverter(AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Get the message converter.
     * @return the message converter.
     */
    public AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> getMessageConverter() {
        return messageConverter;
    }

    /**
     * Set the default entity type of the destination to be sent messages to. Required when no entity type is specified
     * in {@link ServiceBusProducerFactory} via related the {@link NamespaceProperties} or producer {@link PropertiesSupplier}.
     * @param entityType the entity type.
     */
    public void setDefaultEntityType(ServiceBusEntityType entityType) {
        defaultEntityType = entityType;
    }
}
