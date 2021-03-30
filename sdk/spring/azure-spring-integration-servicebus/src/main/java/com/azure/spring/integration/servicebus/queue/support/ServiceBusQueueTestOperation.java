// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.queue.support;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.servicebus.InboundServiceBusMessageConsumer;
import com.azure.spring.integration.servicebus.ServiceBusMessageHandler;
import com.azure.spring.integration.servicebus.ServiceBusRuntimeException;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A test implementation of {@link ServiceBusQueueTemplate}. This is used for testing.
 */
public class ServiceBusQueueTestOperation extends ServiceBusQueueTemplate {
    private final Multimap<String, ServiceBusMessage> topicsByName = ArrayListMultimap.create();
    private final Multimap<String, InboundServiceBusMessageConsumer> consumersByQueue = ArrayListMultimap.create();
    private CheckpointConfig checkpointConfig;
    private ServiceBusMessageConverter serviceBusMessageConverter;

    public ServiceBusQueueTestOperation(ServiceBusQueueClientFactory clientFactory, CheckpointConfig checkpointConfig, ServiceBusMessageConverter serviceBusMessageConverter) {
        super(clientFactory, new ServiceBusMessageConverter());
        this.checkpointConfig = checkpointConfig;
        this.serviceBusMessageConverter = serviceBusMessageConverter;
    }

    public static <E> Optional<E> getRandom(Collection<E> e) {
        if (0 == e.size()) {
            return Optional.empty();
        }
        return e.stream().skip(new Random().nextInt(e.size())).findFirst();
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ServiceBusMessage azureMessage = getMessageConverter().fromMessage(message, ServiceBusMessage.class);

        topicsByName.put(name, azureMessage);
      //  getRandom(consumersByQueue.get(name)).ifPresent(c -> c.accept(azureMessage)); //TODO

        future.complete(null);
        return future;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {

        ServiceBusProcessorClient processorClient = null; //TODO  the logic of obtaining the instance of processor client
        InboundServiceBusMessageConsumer inboundConsumer = new InboundServiceBusMessageConsumer(name, null, checkpointConfig, serviceBusMessageConverter,consumer, payloadType);

        processorClient.start();

        consumersByQueue.put(name, inboundConsumer);
    }

    @Override
    public boolean unsubscribe(String name) {
        consumersByQueue.removeAll(name);
        return true;
    }
}

