// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.PropertiesSupplier;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

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
