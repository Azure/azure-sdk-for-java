// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic.support;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.servicebus.InboundServiceBusMessageConsumer;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.azure.spring.integration.core.api.PartitionSupplier;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A test implementation of {@link ServiceBusTopicTemplate}. This is used for testing.
 */
public class ServiceBusTopicTestOperation extends ServiceBusTopicTemplate {
    private final Multimap<String, ServiceBusMessage> topicsByName = ArrayListMultimap.create();


    private final Map<String, Map<String, InboundServiceBusMessageConsumer>> consumersByNameAndGroup =  new ConcurrentHashMap<>();
    private CheckpointConfig checkpointConfig;
    private ServiceBusMessageConverter serviceBusMessageConverter;

    public ServiceBusTopicTestOperation(ServiceBusTopicClientFactory clientFactory, CheckpointConfig checkpointConfig, ServiceBusMessageConverter serviceBusMessageConverter) {
        super(clientFactory, new ServiceBusMessageConverter());
        this.checkpointConfig = checkpointConfig;
        this.serviceBusMessageConverter = serviceBusMessageConverter;
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ServiceBusMessage azureMessage = getMessageConverter().fromMessage(message, ServiceBusMessage.class);

        topicsByName.put(name, azureMessage);
        consumersByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());
      //  consumersByNameAndGroup.get(name).values().forEach(c -> c.apply(azureMessage));//TODO

        future.complete(null);
        return future;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name, String consumerGroup, Consumer<Message<?>> consumer,
                                     Class<?> payloadType) {
        ServiceBusProcessorClient processorClient = null; //TODO  the logic of obtaining the instance of processor client
        InboundServiceBusMessageConsumer inboundConsumer = new InboundServiceBusMessageConsumer(name, consumerGroup, checkpointConfig, serviceBusMessageConverter,consumer, payloadType);

        processorClient.start();


        consumersByNameAndGroup.putIfAbsent(name, new ConcurrentHashMap<>());
        consumersByNameAndGroup.get(name).put(consumerGroup, inboundConsumer);
    }

    @Override
    public boolean unsubscribe(String name, String consumerGroup) {
        consumersByNameAndGroup.get(name).remove(consumerGroup);
        return true;
    }
}

