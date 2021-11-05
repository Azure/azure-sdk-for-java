// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.messaging.core.SendOperation;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Azure Service Bus template to support send {@link Message} asynchronously.
 *
 */
public class ServiceBusTemplate implements SendOperation {

    private static final ServiceBusMessageConverter DEFAULT_CONVERTER = new ServiceBusMessageConverter();
    private final ServiceBusProducerFactory producerFactory;
    private ServiceBusMessageConverter messageConverter = DEFAULT_CONVERTER;

    public ServiceBusTemplate(@NonNull ServiceBusProducerFactory producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Override
    public <U> Mono<Void> sendAsync(String destination,
                                    Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        Assert.hasText(destination, "destination can't be null or empty");
        ServiceBusSenderAsyncClient senderAsyncClient = this.producerFactory.createProducer(destination);
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

}
