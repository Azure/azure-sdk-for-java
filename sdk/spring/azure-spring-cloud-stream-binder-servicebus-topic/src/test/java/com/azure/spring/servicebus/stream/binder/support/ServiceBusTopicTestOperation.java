// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.support;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.servicebus.core.DefaultServiceBusMessageProcessor;
import com.azure.spring.servicebus.core.ServiceBusTopicClientFactory;
import com.azure.spring.servicebus.core.topic.ServiceBusTopicTemplate;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A test implementation of {@link ServiceBusTopicTemplate}. This is used for testing.
 */
public class ServiceBusTopicTestOperation extends ServiceBusTopicTemplate {

    private final Map<String, List<ServiceBusReceivedMessageContext>> topicsByName = new HashMap<>();
    private final Map<String, Map<String, DefaultServiceBusMessageProcessor>> processorsByTopicAndSub =
        new ConcurrentHashMap<>();

    public ServiceBusTopicTestOperation(ServiceBusTopicClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <U> CompletableFuture<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {

        ServiceBusMessage azureMessage = getMessageConverter().fromMessage(message, ServiceBusMessage.class);

        final ServiceBusReceivedMessageContext receivedMessageContext = mockReceivedMessageContext(azureMessage);

        if (topicsByName.containsKey(name)) {
            topicsByName.get(name).add(receivedMessageContext);
        } else {
            topicsByName.put(name, new ArrayList<>(Arrays.asList(receivedMessageContext)));
        }
        processorsByTopicAndSub.get(name).values().forEach(c -> c.processMessage().accept(receivedMessageContext));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean unsubscribe(String name, String consumerGroup) {
        processorsByTopicAndSub.get(name).remove(consumerGroup);
        return true;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name,
                                     String consumerGroup,
                                     Consumer<Message<?>> consumer,
                                     Class<?> payloadType) {

        // client
        DefaultServiceBusMessageProcessor messageProcessor = new DefaultServiceBusMessageProcessor(
            this.checkpointConfig, payloadType, consumer, this.messageConverter);


        processorsByTopicAndSub.computeIfAbsent(name, t -> new ConcurrentHashMap<>())
                               .put(consumerGroup, messageProcessor);
    }

    private ServiceBusReceivedMessageContext mockReceivedMessageContext(@NonNull ServiceBusMessage message) {
        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(receivedMessage.getBody()).thenReturn(message.getBody());

        ServiceBusReceivedMessageContext receivedMessageContext = mock(ServiceBusReceivedMessageContext.class);
        when(receivedMessageContext.getMessage()).thenReturn(receivedMessage);

        return receivedMessageContext;
    }

}

