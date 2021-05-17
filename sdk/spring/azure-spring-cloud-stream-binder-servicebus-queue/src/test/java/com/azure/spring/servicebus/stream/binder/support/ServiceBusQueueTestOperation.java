// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.support;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.servicebus.DefaultServiceBusMessageProcessor;
import com.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueTemplate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A test implementation of {@link ServiceBusQueueTemplate}. This is used for testing.
 * <p>
 * The difference between this test operation and the original one is that test operation will invoke the message
 * processing immediately after sending it out.
 */
public class ServiceBusQueueTestOperation extends ServiceBusQueueTemplate {

    private final Multimap<String, ServiceBusReceivedMessageContext> queuesByName = ArrayListMultimap.create();
    private final Multimap<String, DefaultServiceBusMessageProcessor> processorsByQueue = ArrayListMultimap.create();

    public ServiceBusQueueTestOperation(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        ServiceBusMessage azureMessage = getMessageConverter().fromMessage(message, ServiceBusMessage.class);

        final ServiceBusReceivedMessageContext receivedMessageContext = mockReceivedMessageContext(azureMessage);
        queuesByName.put(name, receivedMessageContext);

        getRandom(processorsByQueue.get(name)).ifPresent(c -> c.processMessage().accept(receivedMessageContext));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {
        DefaultServiceBusMessageProcessor messageProcessor = new DefaultServiceBusMessageProcessor(
            this.checkpointConfig, payloadType, consumer, this.messageConverter);

        processorsByQueue.put(name, messageProcessor);
    }

    @Override
    public boolean unsubscribe(String name) {
        processorsByQueue.removeAll(name);
        return true;
    }

    private static <E> Optional<E> getRandom(Collection<E> e) {
        if (0 == e.size()) {
            return Optional.empty();
        }
        return e.stream().skip(new Random().nextInt(e.size())).findFirst();
    }

    private ServiceBusReceivedMessageContext mockReceivedMessageContext(@NonNull ServiceBusMessage message) {
        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getBody()).thenReturn(message.getBody());

        ServiceBusReceivedMessageContext receivedMessageContext = mock(ServiceBusReceivedMessageContext.class);
        when(receivedMessageContext.getMessage()).thenReturn(receivedMessage);
        return receivedMessageContext;
    }

}

