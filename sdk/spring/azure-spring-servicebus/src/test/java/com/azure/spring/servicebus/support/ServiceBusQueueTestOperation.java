// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.support;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.servicebus.core.DefaultServiceBusMessageProcessor;
import com.azure.spring.servicebus.core.ServiceBusQueueClientFactory;
import com.azure.spring.servicebus.core.queue.ServiceBusQueueTemplate;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A test implementation of {@link ServiceBusQueueTemplate}. This is used for testing.
 * <p>
 * The difference between this test operation and the original one is that test operation will invoke the message
 * processing immediately after sending it out.
 */
public class ServiceBusQueueTestOperation extends ServiceBusQueueTemplate {

    private final Map<String, List<ServiceBusReceivedMessageContext>> queuesByName = new HashMap<>();
    private final Map<String, List<DefaultServiceBusMessageProcessor>> processorsByQueue = new HashMap<>();
    private final AtomicInteger abandonCalledTimes = new AtomicInteger(0);
    private final AtomicInteger completeCalledTimes = new AtomicInteger(0);
    private final AtomicInteger deadLetterCalledTimes = new AtomicInteger(0);

    public ServiceBusQueueTestOperation(ServiceBusQueueClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public <U> Mono<Void> sendAsync(String name, Message<U> message, PartitionSupplier partitionSupplier) {
        ServiceBusMessage azureMessage = getMessageConverter().fromMessage(message, ServiceBusMessage.class);

        final ServiceBusReceivedMessageContext receivedMessageContext = mockReceivedMessageContext(azureMessage);
        if (queuesByName.containsKey(name)) {
            queuesByName.get(name).add(receivedMessageContext);
        } else {
            queuesByName.put(name, new ArrayList<>(Arrays.asList(receivedMessageContext)));
        }

        getRandom(processorsByQueue.get(name)).ifPresent(c -> c.processMessage().accept(receivedMessageContext));

        return Mono.empty();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void internalSubscribe(String name, Consumer<Message<?>> consumer, Class<?> payloadType) {
        DefaultServiceBusMessageProcessor messageProcessor = new DefaultServiceBusMessageProcessor(
            this.checkpointConfig, payloadType, consumer, this.messageConverter);

        if (processorsByQueue.containsKey(name)) {
            processorsByQueue.get(name).add(messageProcessor);
        } else {
            processorsByQueue.put(name, new ArrayList<>(Arrays.asList(messageProcessor)));
        }
    }

    @Override
    public boolean unsubscribe(String name) {
        processorsByQueue.remove(name);
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

        doAnswer(invocationOnMock -> abandonCalledTimes.getAndAdd(1)).when(receivedMessageContext).abandon();
        doAnswer(invocationOnMock -> completeCalledTimes.getAndAdd(1)).when(receivedMessageContext).complete();
        doAnswer(invocationOnMock -> deadLetterCalledTimes.getAndAdd(1)).when(receivedMessageContext).deadLetter();
        return receivedMessageContext;
    }

    public int getAbandonCalledTimes() {
        return abandonCalledTimes.get();
    }

    public int getCompleteCalledTimes() {
        return completeCalledTimes.get();
    }

    public int getDeadLetterCalledTimes() {
        return deadLetterCalledTimes.get();
    }
}

